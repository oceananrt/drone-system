package com.drone.dronesystem.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * WebSocket配置类：开启WebSocket支持，全端实时通信基础
 */
@Configuration
public class WebSocketConfig {
    /**
     * 注册WebSocket服务端端点处理器
     */
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }
}