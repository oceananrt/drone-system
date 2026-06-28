package com.drone.dronesystem.config;

import com.drone.dronesystem.protocol.DroneProtocolAdapter;
import com.drone.dronesystem.protocol.adapter.CustomProtocolAdapter;
import com.drone.dronesystem.protocol.adapter.DjiCloudProtocolAdapter;
import com.drone.dronesystem.protocol.adapter.MavLinkProtocolAdapter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 协议适配器配置
 * 通过 application.properties 中的 drone.protocol.type 选择协议
 *
 * 可选值：custom / mavlink / dji
 */
@Configuration
public class DroneProtocolConfig {

    // ==================== 自定义协议 ====================
    @Bean
    @ConditionalOnProperty(name = "drone.protocol.type", havingValue = "custom", matchIfMissing = true)
    public DroneProtocolAdapter customProtocolAdapter() {
        return new CustomProtocolAdapter();
    }

    // ==================== MAVLink 协议 ====================
    @Bean
    @ConditionalOnProperty(name = "drone.protocol.type", havingValue = "mavlink")
    public DroneProtocolAdapter mavlinkProtocolAdapter(
            @Value("${drone.mavlink.port:}") String port,
            @Value("${drone.mavlink.baud:57600}") int baud,
            @Value("${drone.mavlink.system-id:255}") int sysId,
            @Value("${drone.mavlink.component-id:190}") int compId) {
        return new MavLinkProtocolAdapter(
                port.isEmpty() ? null : port, baud, sysId, compId);
    }

    // ==================== DJI Cloud API ====================
    @Bean
    @ConditionalOnProperty(name = "drone.protocol.type", havingValue = "dji")
    public DroneProtocolAdapter djiCloudProtocolAdapter(
            @Value("${drone.dji.server-url}") String serverUrl,
            @Value("${drone.dji.app-id}") String appId,
            @Value("${drone.dji.app-secret}") String appSecret) {
        return new DjiCloudProtocolAdapter(serverUrl, appId, appSecret);
    }
}
