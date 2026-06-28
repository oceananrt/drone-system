package com.drone.dronesystem.protocol.adapter;

import com.drone.dronesystem.entity.DroneRealData;
import com.drone.dronesystem.protocol.DroneProtocolAdapter;
import com.drone.dronesystem.protocol.dji.DjiCloudClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DJI Cloud API 协议适配器
 *
 * 通过 DJI Cloud API Server 与 DJI 无人机通信
 * 适用于 DJI Mavic / Matrice / Phantom 等系列
 *
 * 架构：
 * ┌──────────┐   HTTP REST   ┌──────────────┐   Internet   ┌──────┐   RC   ┌──────┐
 * │ 本项目    │ ────────────→ │ DJI Cloud API │ ──────────→ │ 飞手 │ ────→ │ 无人机│
 * │ (后端)    │ ←──────────── │   Server      │ ←────────── │ 端APP│ ←──── │      │
 * └──────────┘               └──────────────┘              └──────┘       └──────┘
 *
 * 使用前需要：
 * 1. 注册 DJI 开发者账号：https://developer.dji.com
 * 2. 创建应用获取 App ID 和 App Secret
 * 3. 部署 DJI Cloud API Server（Docker 推荐）
 * 4. 飞手端 APP 绑定到 Cloud API Server
 * 5. 在 application.properties 配置 server-url、app-id、app-secret
 */
public class DjiCloudProtocolAdapter implements DroneProtocolAdapter {

    private static final Logger log = Logger.getLogger(DjiCloudProtocolAdapter.class.getName());
    private static final ObjectMapper mapper = new ObjectMapper();

    private final DjiCloudClient client;
    private final DroneRealData droneData = new DroneRealData();
    private String deviceSn;  // 无人机序列号（连接后自动获取）

    /**
     * @param serverUrl DJI Cloud API Server 地址
     * @param appId     应用 ID
     * @param appSecret 应用密钥
     */
    public DjiCloudProtocolAdapter(String serverUrl, String appId, String appSecret) {
        this.client = new DjiCloudClient(serverUrl, appId, appSecret);
    }

    @Override
    public String getProtocolName() {
        return "dji";
    }

    @Override
    public boolean connect() {
        // 1. 登录 Cloud API Server
        if (!client.login()) {
            droneData.setConnected(false);
            return false;
        }

        // 2. 获取设备列表，取第一个设备
        try {
            String devicesJson = client.getDevices();
            if (devicesJson == null) {
                log.warning("DJI Cloud: 获取设备列表失败");
                droneData.setConnected(false);
                return false;
            }

            JsonNode root = mapper.readTree(devicesJson);
            JsonNode devices = root.path("data");
            if (devices.isArray() && devices.size() > 0) {
                deviceSn = devices.get(0).path("sn").asText();
                droneData.setDroneName(devices.get(0).path("name").asText(deviceSn));
                droneData.setConnected(true);
                log.info("DJI Cloud: 已连接设备 " + deviceSn);
                return true;
            } else {
                log.warning("DJI Cloud: 未找到已绑定的无人机设备");
                droneData.setConnected(false);
                return false;
            }
        } catch (Exception e) {
            log.log(Level.WARNING, "DJI Cloud: 连接异常", e);
            droneData.setConnected(false);
            return false;
        }
    }

    @Override
    public void disconnect() {
        client.disconnect();
        droneData.setConnected(false);
        deviceSn = null;
        log.info("DJI Cloud: 已断开");
    }

    @Override
    public boolean isConnected() {
        return client.isConnected() && deviceSn != null;
    }

    @Override
    public DroneRealData readRealData() {
        if (!isConnected()) {
            droneData.setConnected(false);
            return droneData;
        }

        try {
            String stateJson = client.getDeviceState(deviceSn);
            if (stateJson == null) return droneData;

            JsonNode root = mapper.readTree(stateJson);
            JsonNode data = root.path("data");

            if (data.isMissingNode()) return droneData;

            // 解析遥测数据
            droneData.setLat(data.path("latitude").asDouble());
            droneData.setLng(data.path("longitude").asDouble());
            droneData.setAltitude((float) data.path("height").asDouble());
            droneData.setSpeed((float) data.path("horizontal_speed").asDouble());
            droneData.setBattery(data.path("battery_percent").asInt());
            droneData.setVoltage((float) data.path("battery_voltage").asDouble());
            droneData.setGpsNum((float) data.path("gps_number").asInt());
            droneData.setConnected(true);

            // 飞行状态
            int mode = data.path("flight_status").asInt(0);
            switch (mode) {
                case 0: droneData.setFlyStatus("STANDBY"); break;
                case 1: droneData.setFlyStatus("FLYING"); break;
                case 2: droneData.setFlyStatus("LANDING"); break;
                case 3: droneData.setFlyStatus("RETURNING"); break;
                case 4: droneData.setFlyStatus("HOVER"); break;
                default: droneData.setFlyStatus("UNKNOWN"); break;
            }

        } catch (Exception e) {
            log.log(Level.WARNING, "DJI Cloud: 读取遥测异常", e);
        }

        return droneData;
    }

    @Override
    public boolean takeoff(int height) {
        String params = String.format("{\"height\":%d}", height);
        String result = client.sendCommand(deviceSn, "takeoff", params);
        return isSuccess(result);
    }

    @Override
    public boolean land() {
        String result = client.sendCommand(deviceSn, "land", "{}");
        return isSuccess(result);
    }

    @Override
    public boolean hover() {
        String result = client.sendCommand(deviceSn, "gohover", "{}");
        return isSuccess(result);
    }

    @Override
    public boolean returnHome() {
        String result = client.sendCommand(deviceSn, "return", "{}");
        return isSuccess(result);
    }

    @Override
    public boolean setAltitude(int height) {
        String params = String.format("{\"height\":%d}", height);
        String result = client.sendCommand(deviceSn, "set_height", params);
        return isSuccess(result);
    }

    @Override
    public boolean goTo(double lat, double lng, int height) {
        String params = String.format(
                "{\"latitude\":%.7f,\"longitude\":%.7f,\"height\":%d}", lat, lng, height);
        String result = client.sendCommand(deviceSn, "go_to", params);
        return isSuccess(result);
    }

    /**
     * 检查 API 响应是否成功
     */
    private boolean isSuccess(String response) {
        if (response == null) return false;
        try {
            JsonNode root = mapper.readTree(response);
            return root.path("code").asInt(-1) == 0;
        } catch (Exception e) {
            return false;
        }
    }
}
