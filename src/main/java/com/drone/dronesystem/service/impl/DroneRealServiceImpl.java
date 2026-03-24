package com.drone.dronesystem.service.impl;

import com.drone.dronesystem.constant.DroneState;
import com.drone.dronesystem.entity.DroneRealData;
import com.drone.dronesystem.entity.DroneRawData;
import com.drone.dronesystem.protocol.*;
import com.drone.dronesystem.service.*;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;

// 🔥 只多这一个导入（WebSocket）
import com.drone.dronesystem.service.DroneWebsocketService;

@Service
public class DroneRealServiceImpl implements DroneRealService {

    @Resource
    private DroneFlyService droneFlyService;
    private final DroneSafetyService safetyService;
    private final DroneLogService logService;
    private final DroneFenceService fenceService;
    private final DroneRealData droneData = new DroneRealData();
    private DroneState currentState = DroneState.IDLE;

    public DroneRealServiceImpl(DroneSafetyService safetyService,
                                DroneLogService logService,
                                DroneFenceService fenceService) {
        this.safetyService = safetyService;
        this.logService = logService;
        this.fenceService = fenceService;
    }

    @PostConstruct
    public void startDaemonThread() {
        new Thread(() -> {
            while (true) {
                try {
                    if (droneData.isConnected()) {
                        safetyService.checkAllSafety(droneData);
                        fenceService.checkFence(droneData);
                        logService.saveFlightLog(droneData);

                        // ======================================
                        // 🔥 只加这一行：实时推送到网页/手机
                        // ======================================
                        DroneWebsocketService.broadcast(droneData);

                    }
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception ignored) {}
            }
        }, "Drone-Master-Daemon").start();
        System.out.println("✅ 无人机主守护线程已启动");
    }

    @Override
    public DroneRealData getRealData() {
        if (!droneData.isConnected()) return droneData;
        byte[] buffer = SerialPortUtil.read();
        if (buffer == null || buffer.length < 22) return droneData;
        DroneRawData raw = DroneParser.parse(buffer);
        if (raw == null) return droneData;

        droneData.setAltitude(raw.altitude / 100.0f);
        droneData.setSpeed(raw.speed / 100.0f);
        droneData.setVoltage(raw.voltage / 1000.0f);
        droneData.setBattery(raw.battery);
        droneData.setGpsNum(raw.gpsSat);
        droneData.setLat(raw.lat);
        droneData.setLng(raw.lng);

        switch (raw.flyMode) {
            case 0: droneData.setFlyStatus("STANDBY"); break;
            case 1: droneData.setFlyStatus("FLYING"); break;
            case 2: droneData.setFlyStatus("LANDING"); break;
            case 3: droneData.setFlyStatus("RETURNING"); break;
            case 4: droneData.setFlyStatus("HOVER"); break;
            default: droneData.setFlyStatus("ERROR");
        }
        return droneData;
    }

    @Override
    public boolean connectDrone() {
        boolean ok = SerialPortUtil.open();
        droneData.setConnected(ok);

        // ======================================
        // 🔥 连接成功/失败推送
        // ======================================
        if (ok) DroneWebsocketService.broadcastWarn("✅ 无人机连接成功");
        else DroneWebsocketService.broadcastWarn("❌ 无人机连接失败");

        return ok;
    }

    @Override
    public boolean takeoff() {
        if (currentState != DroneState.IDLE) return false;
        currentState = DroneState.TAKEOFF;
        boolean ok = DroneSender.send(DroneCmd.TAKEOFF);
        currentState = DroneState.FLYING;

        DroneWebsocketService.broadcastWarn("🚀 无人机已起飞");
        return ok;
    }

    @Override
    public boolean land() {
        currentState = DroneState.LANDING;
        boolean ok = DroneSender.send(DroneCmd.LAND);
        currentState = DroneState.IDLE;

        DroneWebsocketService.broadcastWarn("🛬 无人机已降落");
        return ok;
    }

    @Override
    public boolean returnHome() {
        currentState = DroneState.RETURN_HOME;
        boolean ok = DroneSender.send(DroneCmd.RETURN_HOME);
        currentState = DroneState.IDLE;

        DroneWebsocketService.broadcastWarn("📡 无人机正在返航");
        return ok;
    }

    @Override
    public boolean hover() {
        boolean ok = droneFlyService.hover();
        if (ok) DroneWebsocketService.broadcastWarn("⚡ 无人机已悬停");
        return ok;
    }
}