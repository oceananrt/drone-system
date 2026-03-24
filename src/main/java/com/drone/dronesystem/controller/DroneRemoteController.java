package com.drone.dronesystem.controller;

import com.drone.dronesystem.common.Result;
import com.drone.dronesystem.service.DroneRemoteService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/remote")
public class DroneRemoteController {

    private final DroneRemoteService droneRemoteService;

    public DroneRemoteController(DroneRemoteService droneRemoteService) {
        this.droneRemoteService = droneRemoteService;
    }

    @GetMapping("/connect")
    public Result<String> connect() {
        boolean ok = droneRemoteService.connectToServer();
        return ok ? Result.success("已连接至远程服务器") : Result.fail("连接失败");
    }

    @GetMapping("/disconnect")
    public Result<String> disconnect() {
        droneRemoteService.disconnectFromServer();
        return Result.success("已断开连接");
    }
}