package com.drone.dronesystem.controller;

import com.drone.dronesystem.common.Result;
import com.drone.dronesystem.entity.DroneRealData;
import com.drone.dronesystem.service.DroneRealService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/drone")
public class DroneRealController {

    private final DroneRealService droneRealService;

    public DroneRealController(DroneRealService droneRealService) {
        this.droneRealService = droneRealService;
    }

    // 1. 获取真机实时数据
    @GetMapping("/data")
    public Result<DroneRealData> getRealData() {
        return Result.success(droneRealService.getRealData());
    }

    // 2. 连接无人机
    @GetMapping("/connect")
    public Result<String> connect() {
        boolean ok = droneRealService.connectDrone();
        return ok ? Result.success("连接成功") : Result.fail("连接失败");
    }

    // 3. 起飞
    @GetMapping("/takeoff")
    public Result<String> takeoff() {
        boolean ok = droneRealService.takeoff();
        return ok ? Result.success("起飞指令已下发") : Result.fail("未连接无人机");
    }

    // 4. 降落
    @GetMapping("/land")
    public Result<String> land() {
        boolean ok = droneRealService.land();
        return ok ? Result.success("降落指令已下发") : Result.fail("未连接无人机");
    }

    // 5. 返航
    @GetMapping("/returnHome")
    public Result<String> returnHome() {
        boolean ok = droneRealService.returnHome();
        return ok ? Result.success("返航指令已下发") : Result.fail("未连接无人机");
    }
}