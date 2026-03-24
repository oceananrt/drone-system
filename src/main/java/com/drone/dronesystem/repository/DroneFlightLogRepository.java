package com.drone.dronesystem.repository;

import com.drone.dronesystem.entity.DroneFlightLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DroneFlightLogRepository extends JpaRepository<DroneFlightLog, Long> {
}