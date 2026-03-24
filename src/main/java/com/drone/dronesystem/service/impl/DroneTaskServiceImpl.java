package com.drone.dronesystem.service.impl;

import com.drone.dronesystem.constant.TaskType; // 导入
import com.drone.dronesystem.entity.DroneTask;
import com.drone.dronesystem.service.DroneFlyService;
import com.drone.dronesystem.service.DroneTaskService;
import org.springframework.stereotype.Service;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
public class DroneTaskServiceImpl implements DroneTaskService {

    private final Queue<DroneTask> taskQueue = new ConcurrentLinkedQueue<>();
    private final DroneFlyService flyService;
    private boolean taskRunning = false;

    public DroneTaskServiceImpl(DroneFlyService flyService) {
        this.flyService = flyService;
    }

    @Override
    public boolean addTask(DroneTask task) {
        return taskQueue.offer(task);
    }

    @Override
    public void startTask() {
        if (taskRunning) return;
        taskRunning = true;

        new Thread(() -> {
            while (taskRunning && !taskQueue.isEmpty()) {
                DroneTask task = taskQueue.poll();
                executeTask(task);

                try {
                    Thread.sleep(task.getDelaySeconds() * 1000L);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // 恢复中断
                    break;
                }
            }
            taskRunning = false;
        }, "Drone-Task-Thread").start();
    }

    private void executeTask(DroneTask task) {
        // ======================
        // 🔥 这里必须用枚举常量（TaskType.TAKEOFF 等）
        // ======================
        switch (task.getType()) {
            case TAKEOFF:
                flyService.takeoff(task.getHeight());
                break;
            case LAND:
                flyService.land();
                break;
            case RETURN_HOME:
                flyService.returnHome();
                break;
            case GO_TO_GPS:
                flyService.goTo(task.getLat(), task.getLng(), task.getHeight());
                break;
            case HOVER:
                flyService.hover();
                break;
            case TAKE_PHOTO:
                // TODO 真机：调用相机拍照接口
                break;
            case RECORD_VIDEO:
                // TODO 真机：调用相机录像接口
                break;
        }
    }

    @Override
    public void stopTask() {
        taskRunning = false;
        taskQueue.clear();
    }
}