package com.drone.dronesystem.protocol.dji;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DJI Cloud API HTTP 客户端
 *
 * DJI Cloud API 工作方式：
 * ┌──────────┐     HTTP/WS      ┌──────────────┐    Internet    ┌──────────┐
 * │ Java后端  │ ───────────────→ │ DJI Cloud API │ ────────────→ │ 飞手端   │
 * │ (本项目)  │ ←─────────────── │   Server      │ ←──────────── │ (RC/APP) │
 * └──────────┘                  └──────────────┘               └──────────┘
 *
 * 部署要求：
 * 1. 在 DJI 开发者平台 (https://developer.dji.com) 注册应用
 * 2. 部署 DJI Cloud API Server（Docker 或源码部署）
 * 3. 在飞手端 APP 绑定无人机到该 Cloud API 服务
 *
 * API 文档：https://developer.dji.com/doc/cloud-api-tutorial/
 */
public class DjiCloudClient {

    private static final Logger log = Logger.getLogger(DjiCloudClient.class.getName());
    private static final ObjectMapper mapper = new ObjectMapper();

    private final String serverUrl;   // DJI Cloud API Server 地址
    private final String appId;       // 应用 ID
    private final String appSecret;   // 应用密钥

    private String token;             // 登录后的 Token
    private boolean connected = false;

    public DjiCloudClient(String serverUrl, String appId, String appSecret) {
        this.serverUrl = serverUrl.endsWith("/") ? serverUrl : serverUrl + "/";
        this.appId = appId;
        this.appSecret = appSecret;
    }

    /**
     * 登录 DJI Cloud API Server 获取 Token
     * POST /api/v1/user/login
     */
    public boolean login() {
        try {
            String json = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", appId, appSecret);
            String response = doPost("api/v1/user/login", json);
            if (response == null) return false;

            JsonNode node = mapper.readTree(response);
            int code = node.path("code").asInt(-1);
            if (code == 0) {
                token = node.path("data").path("token").asText();
                connected = true;
                log.info("DJI Cloud: 登录成功");
                return true;
            } else {
                log.warning("DJI Cloud: 登录失败 - " + node.path("message").asText());
                return false;
            }
        } catch (Exception e) {
            log.log(Level.WARNING, "DJI Cloud: 登录异常", e);
            return false;
        }
    }

    /**
     * 获取已绑定的设备列表
     * GET /api/v1/devices
     */
    public String getDevices() {
        return doGet("api/v1/devices");
    }

    /**
     * 获取无人机实时遥测
     * GET /api/v1/devices/{sn}/state
     *
     * @param deviceSn 设备序列号
     * @return JSON 字符串，包含经纬度、高度、速度、电量等
     */
    public String getDeviceState(String deviceSn) {
        return doGet("api/v1/devices/" + deviceSn + "/state");
    }

    /**
     * 发送飞行控制指令
     * POST /api/v1/devices/{sn}/commands
     *
     * @param deviceSn 设备序列号
     * @param command  指令类型（takeoff/land/return/gohover）
     * @param params   指令参数
     */
    public String sendCommand(String deviceSn, String command, String params) {
        String json = String.format("{\"command\":\"%s\",\"params\":%s}", command, params);
        return doPost("api/v1/devices/" + deviceSn + "/commands", json);
    }

    /**
     * 下发航线任务
     * POST /api/v1/devices/{sn}/mission
     */
    public String uploadMission(String deviceSn, String missionJson) {
        return doPost("api/v1/devices/" + deviceSn + "/mission", missionJson);
    }

    // ==================== HTTP 工具方法 ====================

    private String doGet(String path) {
        try {
            URL url = new URL(serverUrl + path);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-Type", "application/json");
            if (token != null) {
                conn.setRequestProperty("Authorization", "Bearer " + token);
            }
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(10000);

            return readResponse(conn);
        } catch (Exception e) {
            log.log(Level.WARNING, "DJI Cloud GET 异常: " + path, e);
            return null;
        }
    }

    private String doPost(String path, String jsonBody) {
        try {
            URL url = new URL(serverUrl + path);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            if (token != null) {
                conn.setRequestProperty("Authorization", "Bearer " + token);
            }
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(10000);
            conn.setDoOutput(true);

            if (jsonBody != null) {
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
                    os.flush();
                }
            }

            return readResponse(conn);
        } catch (Exception e) {
            log.log(Level.WARNING, "DJI Cloud POST 异常: " + path, e);
            return null;
        }
    }

    private String readResponse(HttpURLConnection conn) throws IOException {
        int code = conn.getResponseCode();
        InputStream is = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();
        if (is == null) return null;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        }
    }

    // ==================== Getter ====================

    public boolean isConnected() {
        return connected && token != null;
    }

    public void disconnect() {
        token = null;
        connected = false;
    }
}
