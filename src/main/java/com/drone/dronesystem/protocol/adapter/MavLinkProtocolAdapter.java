package com.drone.dronesystem.protocol.adapter;

import com.drone.dronesystem.entity.DroneRealData;
import com.drone.dronesystem.protocol.DroneProtocolAdapter;
import com.drone.dronesystem.protocol.SerialPortUtil;
import com.drone.dronesystem.protocol.mavlink.MavLinkCodec;
import com.drone.dronesystem.protocol.mavlink.MavLinkMessage;

import java.util.Arrays;
import java.util.logging.Logger;

/**
 * MAVLink v1 协议适配器
 * 对接 Pixhawk / ArduPilot / PX4 等开源飞控
 * 通过串口（数传电台）与飞控通信
 *
 * 通信流程：
 * 1. connect() → 打开串口 + 发送 HEARTBEAT 握手
 * 2. 守护线程调用 readRealData() → 读取 GPS/ATTITUDE/SYS_STATUS 消息
 * 3. 发送指令 → COMMAND_LONG (起飞/降落/返航) 或 SET_POSITION_TARGET_GLOBAL_INT (GPS飞行)
 */
public class MavLinkProtocolAdapter implements DroneProtocolAdapter {

    private static final Logger log = Logger.getLogger(MavLinkProtocolAdapter.class.getName());

    // 配置
    private final String portName;      // 串口名，null 表示自动搜索
    private final int baudRate;         // 波特率
    private final int systemId;         // 本机系统 ID（地面站通常用 255）
    private final int componentId;      // 本机组件 ID（通常用 190 = MAV_COMP_ID_MISSIONPLANNER）

    // 飞控系统 ID（连接后从心跳包获取，默认 1）
    private int targetSystemId = 1;
    private int targetComponentId = 1;

    // 状态
    private final DroneRealData droneData = new DroneRealData();
    private int sequence = 0;
    private long lastHeartbeatTime = 0;

    // 接收缓冲区
    private final byte[] rxBuffer = new byte[1024];
    private int rxOffset = 0;

    /**
     * @param portName   串口名（null=自动搜索）
     * @param baudRate   波特率（MAVLink 默认 57600）
     * @param systemId   本机系统 ID
     * @param componentId 本机组件 ID
     */
    public MavLinkProtocolAdapter(String portName, int baudRate, int systemId, int componentId) {
        this.portName = portName;
        this.baudRate = baudRate;
        this.systemId = systemId;
        this.componentId = componentId;
    }

    @Override
    public String getProtocolName() {
        return "mavlink";
    }

    @Override
    public boolean connect() {
        // 打开串口
        boolean ok;
        if (portName != null && !portName.isEmpty()) {
            ok = SerialPortUtil.openPort(portName, baudRate);
        } else {
            ok = SerialPortUtil.open(); // 自动搜索
        }

        if (!ok) {
            log.warning("MAVLink: 串口打开失败");
            droneData.setConnected(false);
            return false;
        }

        // 发送 HEARTBEAT 握手
        sendHeartbeat();
        droneData.setConnected(true);
        log.info("MAVLink: 已连接，系统ID=" + systemId);
        return true;
    }

    @Override
    public void disconnect() {
        SerialPortUtil.close();
        droneData.setConnected(false);
        log.info("MAVLink: 已断开");
    }

    @Override
    public boolean isConnected() {
        return SerialPortUtil.isConnected();
    }

    @Override
    public DroneRealData readRealData() {
        if (!isConnected()) {
            droneData.setConnected(false);
            return droneData;
        }

        // 读取串口数据到缓冲区
        byte[] raw = SerialPortUtil.read();
        if (raw != null && raw.length > 0) {
            // 追加到接收缓冲区
            int copyLen = Math.min(raw.length, rxBuffer.length - rxOffset);
            System.arraycopy(raw, 0, rxBuffer, rxOffset, copyLen);
            rxOffset += copyLen;
        }

        // 从缓冲区解析所有完整的 MAVLink 消息
        int parsePos = 0;
        while (parsePos < rxOffset) {
            // 找到起始标记
            if ((rxBuffer[parsePos] & 0xFF) != MavLinkMessage.STX_V1) {
                parsePos++;
                continue;
            }

            MavLinkMessage msg = MavLinkCodec.decode(rxBuffer, parsePos);
            if (msg == null) break; // 数据不完整，等待更多数据

            // 处理消息
            handleMessage(msg);
            parsePos += 6 + msg.length + 2; // 跳过已解析的帧
        }

        // 移除已解析的数据
        if (parsePos > 0 && parsePos < rxOffset) {
            int remaining = rxOffset - parsePos;
            System.arraycopy(rxBuffer, parsePos, rxBuffer, 0, remaining);
            rxOffset = remaining;
        } else if (parsePos >= rxOffset) {
            rxOffset = 0;
        }

        droneData.setConnected(true);
        return droneData;
    }

    /**
     * 处理收到的 MAVLink 消息
     */
    private void handleMessage(MavLinkMessage msg) {
        switch (msg.messageId) {
            case MavLinkMessage.MSG_ID_HEARTBEAT:
                // 从心跳包获取飞控系统 ID
                int type = MavLinkCodec.parseHeartbeatType(msg);
                if (type >= 0 && type <= 20) {
                    // 是飞控的心跳（type 0-20 是飞机类型）
                    targetSystemId = msg.systemId;
                    targetComponentId = msg.componentId;
                }
                break;

            case MavLinkMessage.MSG_ID_GPS_RAW_INT:
                double[] gps = MavLinkCodec.parseGpsRawInt(msg);
                if (gps != null) {
                    droneData.setLat(gps[0]);
                    droneData.setLng(gps[1]);
                    droneData.setAltitude((float) gps[2]);
                    droneData.setGpsNum((float) gps[3]);
                }
                break;

            case MavLinkMessage.MSG_ID_ATTITUDE:
                float[] att = MavLinkCodec.parseAttitude(msg);
                if (att != null) {
                    // 可以存储姿态数据，DroneRealData 暂无对应字段
                    // att[0]=roll, att[1]=pitch, att[2]=yaw
                }
                break;

            case MavLinkMessage.MSG_ID_SYS_STATUS:
                int[] sys = MavLinkCodec.parseSysStatus(msg);
                if (sys != null) {
                    droneData.setVoltage(sys[0] / 1000.0f); // mV → V
                    if (sys[1] >= 0) { // -1 表示未知
                        droneData.setBattery(sys[1]);
                    }
                }
                break;

            case MavLinkMessage.MSG_ID_COMMAND_ACK:
                int result = MavLinkCodec.parseCommandAck(msg);
                log.info("MAVLink: 指令确认 result=" + result);
                break;
        }
    }

    @Override
    public boolean takeoff(int height) {
        // COMMAND_LONG: MAV_CMD_NAV_TAKEOFF (22)
        // param7 = 目标高度
        MavLinkMessage msg = MavLinkCodec.buildCommandLong(
                targetSystemId, targetComponentId,
                MavLinkMessage.CMD_NAV_TAKEOFF,
                0, 0, 0, 0, 0, 0, height);
        return send(msg);
    }

    @Override
    public boolean land() {
        // COMMAND_LONG: MAV_CMD_NAV_LAND (21)
        MavLinkMessage msg = MavLinkCodec.buildCommandLong(
                targetSystemId, targetComponentId,
                MavLinkMessage.CMD_NAV_LAND);
        return send(msg);
    }

    @Override
    public boolean hover() {
        // 切换到 GUIDED 模式并发送当前位置的目标
        // 简化实现：发送 LOITER 指令
        MavLinkMessage msg = MavLinkCodec.buildCommandLong(
                targetSystemId, targetComponentId,
                MavLinkMessage.CMD_DO_SET_MODE,
                1, 4); // base_mode=1(CUSTOM), custom_mode=4(LOITER for ArduCopter)
        return send(msg);
    }

    @Override
    public boolean returnHome() {
        // COMMAND_LONG: MAV_CMD_NAV_RETURN_TO_LAUNCH (20)
        MavLinkMessage msg = MavLinkCodec.buildCommandLong(
                targetSystemId, targetComponentId,
                MavLinkMessage.CMD_NAV_RETURN_TO_LAUNCH);
        return send(msg);
    }

    @Override
    public boolean setAltitude(int height) {
        // SET_POSITION_TARGET_GLOBAL_INT: 使用当前 GPS + 新高度
        int latE7 = (int) (droneData.getLat() * 1e7);
        int lonE7 = (int) (droneData.getLng() * 1e7);
        MavLinkMessage msg = MavLinkCodec.buildSetPositionTargetGlobalInt(
                targetSystemId, targetComponentId, latE7, lonE7, height);
        return send(msg);
    }

    @Override
    public boolean goTo(double lat, double lng, int height) {
        int latE7 = (int) (lat * 1e7);
        int lonE7 = (int) (lng * 1e7);
        MavLinkMessage msg = MavLinkCodec.buildSetPositionTargetGlobalInt(
                targetSystemId, targetComponentId, latE7, lonE7, height);
        return send(msg);
    }

    @Override
    public void sendHeartbeat() {
        MavLinkMessage msg = MavLinkCodec.buildHeartbeat(systemId, componentId);
        send(msg);
        lastHeartbeatTime = System.currentTimeMillis();
    }

    /**
     * 发送 MAVLink 消息
     */
    private boolean send(MavLinkMessage msg) {
        msg.sequence = sequence++ & 0xFF;
        byte[] frame = MavLinkCodec.encode(msg);
        return SerialPortUtil.send(frame);
    }
}
