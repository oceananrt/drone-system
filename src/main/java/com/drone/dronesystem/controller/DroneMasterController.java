package com.drone.dronesystem.controller;

import com.drone.dronesystem.common.Result;
import com.drone.dronesystem.entity.DroneRealData;
import com.drone.dronesystem.protocol.DroneProtocolAdapter;
import com.drone.dronesystem.service.DroneRealService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/drone")
@CrossOrigin
public class DroneMasterController {

    private final DroneRealService droneRealService;
    private final DroneProtocolAdapter adapter;

    public DroneMasterController(DroneRealService droneRealService, DroneProtocolAdapter adapter) {
        this.droneRealService = droneRealService;
        this.adapter = adapter;
    }

    /**
     * 查看当前协议类型
     */
    @GetMapping("/protocol")
    public Result<Map<String, Object>> protocol() {
        Map<String, Object> info = new HashMap<>();
        info.put("type", adapter.getProtocolName());
        info.put("connected", adapter.isConnected());
        return Result.success(info);
    }

    @PostMapping("/connect")
    public Result<?> connect() {
        boolean ok = droneRealService.connectDrone();
        return ok ? Result.success("连接成功") : Result.fail("连接失败");
    }

    @GetMapping("/data")
    public Result<DroneRealData> data() {
        return Result.success(droneRealService.getRealData());
    }

    @PostMapping("/takeoff")
    public Result<?> takeoff() {
        boolean ok = droneRealService.takeoff();
        return ok ? Result.success() : Result.fail("起飞失败");
    }

    @PostMapping("/land")
    public Result<?> land() {
        boolean ok = droneRealService.land();
        return ok ? Result.success() : Result.fail("降落失败");
    }

    @PostMapping("/returnHome")
    public Result<?> returnHome() {
        boolean ok = droneRealService.returnHome();
        return ok ? Result.success() : Result.fail("返航失败");
    }

    @PostMapping("/hover")
    public Result<?> hover() {
        boolean ok = droneRealService.hover();
        return ok ? Result.success() : Result.fail("悬停失败");
    }
}
