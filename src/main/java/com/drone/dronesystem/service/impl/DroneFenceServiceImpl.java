package com.drone.dronesystem.service.impl;

import com.drone.dronesystem.entity.DroneRealData;
import com.drone.dronesystem.service.DroneFenceService;
import com.drone.dronesystem.service.DroneFlyService;
import org.springframework.stereotype.Service;

/**
 * 电子围栏服务实现
 * 负责检查无人机是否在禁飞区/限高范围内
 */
@Service
public class DroneFenceServiceImpl implements DroneFenceService {

    // 可根据实际区域修改经纬度与限高
    private static final double MIN_LAT = 20.00;
    private static final double MAX_LAT = 21.00;
    private static final double MIN_LNG = 110.00;
    private static final double MAX_LNG = 111.00;
    private static final int MAX_HEIGHT = 150;

    private final DroneFlyService flyService;

    // 构造器注入（Spring 4.3+ 推荐）
    public DroneFenceServiceImpl(DroneFlyService flyService) {
        this.flyService = flyService;
    }

    /**
     * 检查围栏是否合法
     * @param data 无人机实时数据
     * @return true 合法，false 违规触发保护
     */
    @Override
    public boolean checkFence(DroneRealData data) {
        // 1. 检查经纬度是否在围栏内
        if (data.getLat() < MIN_LAT || data.getLat() > MAX_LAT ||
                data.getLng() < MIN_LNG || data.getLng() > MAX_LNG) {
            flyService.hover(); // 超出范围 → 强制悬停
            return false;
        }

        // 2. 检查高度是否超限
        if (data.getAltitude() > MAX_HEIGHT) {
            flyService.setHeight(120); // 超高压降
            return false;
        }

        return true;
    }
}