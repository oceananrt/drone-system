package com.drone.dronesystem.service.impl;

import com.drone.dronesystem.constant.DroneState;
import com.drone.dronesystem.entity.DroneRealData;
import com.drone.dronesystem.protocol.DroneProtocolAdapter;
import com.drone.dronesystem.service.*;
import com.drone.dronesystem.service.DroneWebsocketService;
import javax.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicReference;

@Service
public class DroneRealServiceImpl implements DroneRealService {

    private final DroneProtocolAdapter adapter;
    private final DroneSafetyService safetyService;
    private final DroneLogService logService;
    private final DroneFenceService fenceService;
    private final AtomicReference<DroneState> currentState = new AtomicReference<>(DroneState.IDLE);

    public DroneRealServiceImpl(DroneProtocolAdapter adapter,
                                DroneSafetyService safetyService,
                                DroneLogService logService,
                                DroneFenceService fenceService) {
        this.adapter = adapter;
        this.safetyService = safetyService;
        this.logService = logService;
        this.fenceService = fenceService;
    }

    @PostConstruct
    public void startDaemonThread() {
        new Thread(() -> {
            while (true) {
                try {
                    if (adapter.isConnected()) {
                        DroneRealData data = adapter.readRealData();
                        safetyService.checkAllSafety(data);
                        fenceService.checkFence(data);
                        logService.saveFlightLog(data);
                        DroneWebsocketService.broadcast(data);

                        // 定期发送心跳
                        adapter.sendHeartbeat();
                    }
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception ignored) {}
            }
        }, "Drone-Master-Daemon").start();
        System.out.println("✅ 无人机主守护线程已启动（协议: " + adapter.getProtocolName() + "）");
    }

    @Override
    public DroneRealData getRealData() {
        return adapter.readRealData();
    }

    @Override
    public boolean connectDrone() {
        boolean ok = adapter.connect();

        if (ok) DroneWebsocketService.broadcastWarn("✅ 无人机连接成功（" + adapter.getProtocolName() + "）");
        else DroneWebsocketService.broadcastWarn("❌ 无人机连接失败");

        return ok;
    }

    @Override
    public boolean takeoff() {
        if (!currentState.compareAndSet(DroneState.IDLE, DroneState.TAKEOFF)) return false;
        boolean ok = adapter.takeoff(10);
        currentState.set(DroneState.FLYING);

        DroneWebsocketService.broadcastWarn("🚀 无人机已起飞");
        return ok;
    }

    @Override
    public boolean land() {
        currentState.set(DroneState.LANDING);
        boolean ok = adapter.land();
        currentState.set(DroneState.IDLE);

        DroneWebsocketService.broadcastWarn("🛬 无人机已降落");
        return ok;
    }

    @Override
    public boolean returnHome() {
        currentState.set(DroneState.RETURN_HOME);
        boolean ok = adapter.returnHome();
        currentState.set(DroneState.IDLE);

        DroneWebsocketService.broadcastWarn("📡 无人机正在返航");
        return ok;
    }

    @Override
    public boolean hover() {
        boolean ok = adapter.hover();
        if (ok) DroneWebsocketService.broadcastWarn("⚡ 无人机已悬停");
        return ok;
    }
}
