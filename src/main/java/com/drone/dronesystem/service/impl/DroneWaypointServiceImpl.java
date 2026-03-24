package com.drone.dronesystem.service.impl;

import com.drone.dronesystem.entity.DroneWaypoint;
import com.drone.dronesystem.service.DroneFlyService;
import com.drone.dronesystem.service.DroneWaypointService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service // 🔥 必须加这个注解，否则 Spring 找不到 Bean
public class DroneWaypointServiceImpl implements DroneWaypointService {

    private final DroneFlyService flyService;
    private final List<DroneWaypoint> waypoints = new ArrayList<>();
    private boolean missionRunning = false;

    // 🔥 构造器注入（Spring 3.6+ 推荐）
    public DroneWaypointServiceImpl(DroneFlyService flyService) {
        this.flyService = flyService;
    }

    @Override
    public boolean addWaypoint(DroneWaypoint waypoint) {
        return waypoints.add(waypoint);
    }

    @Override
    public boolean startMission() {
        if (missionRunning || waypoints.isEmpty()) return false;
        missionRunning = true;

        new Thread(() -> {
            for (DroneWaypoint wp : waypoints) {
                if (!missionRunning) break;
                // 真机飞至目标点
                flyService.goTo(wp.getLat(), wp.getLng(), wp.getHeight());
                try {
                    Thread.sleep(wp.getHoverTime() * 1000L);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            // 任务完成自动返航
            flyService.returnHome();
            missionRunning = false;
        }, "Drone-Mission-Thread").start();
        return true;
    }

    @Override
    public void stopMission() {
        missionRunning = false;
    }

    @Override
    public List<DroneWaypoint> getWaypoints() {
        return new ArrayList<>(waypoints);
    }
}