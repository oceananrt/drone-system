package com.drone.dronesystem.controller;

import com.drone.dronesystem.common.Result;
import com.drone.dronesystem.entity.DroneLocation;
import com.drone.dronesystem.service.DroneMapService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/map")
public class DroneMapController {

    private final DroneMapService droneMapService;

    public DroneMapController(DroneMapService droneMapService) {
        this.droneMapService = droneMapService;
    }

    // 获取当前GPS位置
    @GetMapping("/location")
    public Result<DroneLocation> getLocation() {
        return Result.success(droneMapService.getCurrentLocation());
    }

    // 获取飞行轨迹
    @GetMapping("/path")
    public Result<List<DroneLocation>> getPath() {
        return Result.success(droneMapService.getHistoryPath());
    }

    // 清空轨迹
    @GetMapping("/clear")
    public Result<String> clear() {
        droneMapService.clearPath();
        return Result.success("轨迹已清空");
    }
}