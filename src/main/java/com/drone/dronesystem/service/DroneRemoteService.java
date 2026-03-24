package com.drone.dronesystem.service;

public interface DroneRemoteService {

    // 连接远程服务器（4G/5G 公网）
    boolean connectToServer();

    // 断开远程连接
    void disconnectFromServer();

    // 发送状态到服务器
    boolean sendStatusToServer(String statusJson);

    // 接收远程指令（起飞、降落等）
    void receiveCommandFromServer();
}