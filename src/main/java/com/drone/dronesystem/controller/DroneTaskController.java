package com.drone.dronesystem.controller;

import com.drone.dronesystem.common.Result;
import com.drone.dronesystem.entity.DroneTask;
import com.drone.dronesystem.service.DroneTaskService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/task")
public class DroneTaskController {

    private final DroneTaskService droneTaskService;

    public DroneTaskController(DroneTaskService droneTaskService) {
        this.droneTaskService = droneTaskService;
    }

    @PostMapping("/add")
    public Result<String> addTask(@RequestBody DroneTask task) {
        boolean ok = droneTaskService.addTask(task);
        return ok ? Result.success("任务已添加") : Result.fail("添加失败");
    }

    @PostMapping("/start")
    public Result<String> start() {
        droneTaskService.startTask();
        return Result.success("任务开始执行");
    }

    @PostMapping("/stop")
    public Result<String> stop() {
        droneTaskService.stopTask();
        return Result.success("任务已停止");
    }
}