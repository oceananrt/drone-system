package com.drone.dronesystem.controller;

import com.drone.dronesystem.common.Result;
import com.drone.dronesystem.service.DroneFlyService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/fly")
public class DroneFlyController {

    private final DroneFlyService droneFlyService;

    public DroneFlyController(DroneFlyService droneFlyService) {
        this.droneFlyService = droneFlyService;
    }

    // 起飞
    @GetMapping("/takeoff")
    public Result<String> takeoff(@RequestParam(defaultValue = "10") int height) {
        boolean ok = droneFlyService.takeoff(height);
        return ok ? Result.success("已下发起飞指令") : Result.fail("控制失败");
    }

    // 降落
    @GetMapping("/land")
    public Result<String> land() {
        boolean ok = droneFlyService.land();
        return ok ? Result.success("已下发降落指令") : Result.fail("控制失败");
    }

    // 悬停
    @GetMapping("/hover")
    public Result<String> hover() {
        boolean ok = droneFlyService.hover();
        return ok ? Result.success("已悬停") : Result.fail("控制失败");
    }

    // 返航
    @GetMapping("/returnHome")
    public Result<String> returnHome() {
        boolean ok = droneFlyService.returnHome();
        return ok ? Result.success("已执行返航") : Result.fail("控制失败");
    }

    // 设置高度
    @GetMapping("/setHeight")
    public Result<String> setHeight(int height) {
        boolean ok = droneFlyService.setHeight(height);
        return ok ? Result.success("已设置高度：" + height) : Result.fail("设置失败");
    }
}