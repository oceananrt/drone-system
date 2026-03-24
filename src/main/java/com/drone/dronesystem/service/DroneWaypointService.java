package com.drone.dronesystem.service;

import com.drone.dronesystem.entity.DroneWaypoint;
import java.util.List;

public interface DroneWaypointService {
    boolean addWaypoint(DroneWaypoint waypoint);
    boolean startMission();
    void stopMission();
    List<DroneWaypoint> getWaypoints();
}