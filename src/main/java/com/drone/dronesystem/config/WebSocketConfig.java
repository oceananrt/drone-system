package com.drone.dronesystem.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * WebSocket配置类：开启WebSocket支持，全端实时通信基础
 * 仅在Web应用环境下生效，避免测试环境报错
 */
@Configuration
@ConditionalOnWebApplication
public class WebSocketConfig {
    /**
     * 注册WebSocket服务端端点处理器
     */
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }
}
