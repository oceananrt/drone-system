package com.drone.dronesystem.controller;

import com.drone.dronesystem.common.Result;
import com.drone.dronesystem.entity.DroneWaypoint;
import com.drone.dronesystem.service.DroneWaypointService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mission")
public class DroneMissionController {

    private final DroneWaypointService waypointService;

    public DroneMissionController(DroneWaypointService waypointService) {
        this.waypointService = waypointService;
    }

    @PostMapping("/add")
    public Result<String> addWaypoint(@RequestBody DroneWaypoint waypoint) {
        waypointService.addWaypoint(waypoint);
        return Result.success("航点添加成功");
    }

    @PostMapping("/start")
    public Result<String> startMission() {
        boolean success = waypointService.startMission();
        return success ? Result.success("航线任务已启动") : Result.fail("任务启动失败");
    }

    @PostMapping("/stop")
    public Result<String> stopMission() {
        waypointService.stopMission();
        return Result.success("任务已停止");
    }
}