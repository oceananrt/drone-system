package com.drone.dronesystem.service;

import com.drone.dronesystem.entity.DroneRealData;

public interface DroneFenceService {
    boolean checkFence(DroneRealData data);
}