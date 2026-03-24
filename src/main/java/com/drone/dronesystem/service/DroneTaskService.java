package com.drone.dronesystem.service;

import com.drone.dronesystem.entity.DroneTask;

public interface DroneTaskService {

    // 添加任务
    boolean addTask(DroneTask task);

    // 开始执行任务
    void startTask();

    // 停止任务
    void stopTask();
}