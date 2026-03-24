package com.drone.dronesystem.controller;

import com.drone.dronesystem.common.Result;
import com.drone.dronesystem.service.DroneVideoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/video")
public class DroneVideoController {

    private final DroneVideoService droneVideoService;

    public DroneVideoController(DroneVideoService droneVideoService) {
        this.droneVideoService = droneVideoService;
    }

    @GetMapping("/start")
    public Result<String> start() {
        boolean ok = droneVideoService.startStream();
        return ok ? Result.success("图传已开启") : Result.fail("图传开启失败");
    }

    @GetMapping("/stop")
    public Result<String> stop() {
        boolean ok = droneVideoService.stopStream();
        return ok ? Result.success("图传已关闭") : Result.fail("图传关闭失败");
    }

    @GetMapping("/photo")
    public Result<String> photo() {
        boolean ok = droneVideoService.takePhoto();
        return ok ? Result.success("拍照成功") : Result.fail("拍照失败");
    }

    @GetMapping("/record/start")
    public Result<String> recordStart() {
        boolean ok = droneVideoService.startRecord();
        return ok ? Result.success("录像开始") : Result.fail("录像失败");
    }

    @GetMapping("/record/stop")
    public Result<String> recordStop() {
        boolean ok = droneVideoService.stopRecord();
        return ok ? Result.success("录像已保存") : Result.fail("停止失败");
    }
}