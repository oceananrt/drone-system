package com.drone.dronesystem.service;

import com.drone.dronesystem.entity.DroneRealData;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import org.springframework.stereotype.Component;
import com.alibaba.fastjson2.JSON;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Logger;

@ServerEndpoint("/ws/drone")
@Component
public class DroneWebsocketService {

    private static final CopyOnWriteArraySet<DroneWebsocketService> webSocketSet = new CopyOnWriteArraySet<>();
    private Session session;
    private static final Logger log = Logger.getLogger(DroneWebsocketService.class.getName());

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        webSocketSet.add(this);
        log.info("新客户端连接成功");
    }

    @OnClose
    public void onClose() {
        webSocketSet.remove(this);
    }

    @OnMessage
    public void onMessage(String message, Session session) {}

    @OnError
    public void onError(Throwable error) {}

    public void sendMessage(String message) throws IOException {
        this.session.getBasicRemote().sendText(message);
    }

    public static void broadcast(DroneRealData droneData) {
        for (DroneWebsocketService service : webSocketSet) {
            try {
                service.sendMessage(JSON.toJSONString(droneData));
            } catch (IOException ignored) {}
        }
    }

    public static void broadcastWarn(String warnMsg) {
        for (DroneWebsocketService service : webSocketSet) {
            try {
                service.sendMessage("{\"warn\":\"" + warnMsg + "\"}");
            } catch (IOException ignored) {}
        }
    }
}