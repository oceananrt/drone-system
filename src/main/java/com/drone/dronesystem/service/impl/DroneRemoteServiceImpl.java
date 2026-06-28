package com.drone.dronesystem.service.impl;

import com.drone.dronesystem.constant.RemoteConstant;
import com.drone.dronesystem.entity.DroneRealData;
import com.drone.dronesystem.service.DroneFlyService;
import com.drone.dronesystem.service.DroneRealService;
import com.drone.dronesystem.service.DroneRemoteService;
import com.fasterxml.jackson.databind.ObjectMapper; // 确保导入
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

@Service
public class DroneRemoteServiceImpl implements DroneRemoteService {

    private final DroneRealService droneRealService;
    private final DroneFlyService droneFlyService;
    private final ObjectMapper mapper = new ObjectMapper(); // 🔥 修复：初始化 mapper
    private Socket controlSocket;
    private Socket streamSocket; // 真机逻辑：后续可用，暂时保留
    private boolean connected = false;

    // 构造器注入
    public DroneRemoteServiceImpl(DroneRealService droneRealService, DroneFlyService droneFlyService) {
        this.droneRealService = droneRealService;
        this.droneFlyService = droneFlyService;
    }

    @Override
    public boolean connectToServer() {
        try {
            // 1. 建立控制长连接
            controlSocket = new Socket(RemoteConstant.SERVER_IP, RemoteConstant.CONTROL_PORT);
            controlSocket.setSoTimeout(RemoteConstant.TIMEOUT);
            connected = true;

            // 2. 启动线程接收指令
            new Thread(this::receiveCommandFromServer).start();

            // 3. 启动状态上报线程
            new Thread(this::sendHeartbeatLoop).start();

            System.out.println("✅ 已连接至远程服务器：" + RemoteConstant.SERVER_IP);
            return true;
        } catch (IOException e) {
            System.err.println("❌ 连接远程服务器失败：" + e.getMessage());
            return false;
        }
    }

    @Override
    public void disconnectFromServer() {
        try {
            if (controlSocket != null) controlSocket.close();
            if (streamSocket != null) streamSocket.close();
            connected = false;
            System.out.println("❌ 已断开与远程服务器的连接");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean sendStatusToServer(String statusJson) {
        if (!connected || controlSocket == null || controlSocket.isClosed()) {
            return false;
        }
        try {
            OutputStream out = controlSocket.getOutputStream();
            out.write((statusJson + "\n").getBytes());
            out.flush();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    // 🔥 修复：补全方法体
    private void sendHeartbeatLoop() {
        while (connected) {
            try {
                DroneRealData data = droneRealService.getRealData();
                String json = mapper.writeValueAsString(data); // 🔥 修复：writeValueAsString
                sendStatusToServer(json);
                Thread.sleep(1000); // 每秒上报一次
            } catch (Exception e) {
                e.printStackTrace(); // 修复：打印堆栈
                break;
            }
        }
    }

    @Override
    public void receiveCommandFromServer() {
        if (controlSocket == null) return;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(controlSocket.getInputStream()));
            String command;
            while ((command = reader.readLine()) != null) {
                handleCommand(command);
            }
        } catch (IOException e) {
            disconnectFromServer();
        }
    }

    private void handleCommand(String command) {
        switch (command.toUpperCase()) {
            case "TAKEOFF":
                droneFlyService.takeoff(10);
                break;
            case "LAND":
                droneFlyService.land();
                break;
            case "RETURN_HOME":
                droneFlyService.returnHome();
                break;
            case "HOVER":
                droneFlyService.hover();
                break;
            case "DISCONNECT":
                disconnectFromServer();
                break;
        }
    }
}