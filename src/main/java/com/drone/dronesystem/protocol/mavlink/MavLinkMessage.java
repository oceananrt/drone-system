package com.drone.dronesystem.protocol.mavlink;

/**
 * MAVLink v1/v2 消息结构
 */
public class MavLinkMessage {

    // 起始标记
    public static final byte STX_V1 = (byte) 0xFE; // v1
    public static final byte STX_V2 = (byte) 0xFD; // v2（Pixhawk 默认）

    // 常用消息 ID
    public static final int MSG_ID_HEARTBEAT = 0;
    public static final int MSG_ID_SYS_STATUS = 1;
    public static final int MSG_ID_GPS_RAW_INT = 24;
    public static final int MSG_ID_ATTITUDE = 30;
    public static final int MSG_ID_GLOBAL_POSITION_INT = 33;
    public static final int MSG_ID_COMMAND_LONG = 76;
    public static final int MSG_ID_COMMAND_ACK = 77;
    public static final int MSG_ID_SET_POSITION_TARGET_GLOBAL_INT = 86;

    // COMMAND_LONG 的 command 字段
    public static final int CMD_NAV_TAKEOFF = 22;
    public static final int CMD_NAV_LAND = 21;
    public static final int CMD_NAV_RETURN_TO_LAUNCH = 20;
    public static final int CMD_DO_SET_MODE = 176;

    // 消息字段
    public int length;
    public int sequence;
    public int systemId;
    public int componentId;
    public int messageId;
    public byte[] payload;
    public int crc;
    public boolean isV2 = false; // 是否是 v2 帧

    public MavLinkMessage() {}

    public MavLinkMessage(int sysId, int compId, int msgId, byte[] payload) {
        this.length = payload.length;
        this.systemId = sysId;
        this.componentId = compId;
        this.messageId = msgId;
        this.payload = payload;
    }

    // payload 读取（小端序）
    public int getPayloadUint8(int offset) {
        return payload[offset] & 0xFF;
    }

    public int getPayloadInt16(int offset) {
        return (payload[offset] & 0xFF) | ((payload[offset + 1] & 0xFF) << 8);
    }

    public int getPayloadUint16(int offset) {
        return getPayloadInt16(offset) & 0xFFFF;
    }

    public int getPayloadInt32(int offset) {
        return (payload[offset] & 0xFF)
             | ((payload[offset + 1] & 0xFF) << 8)
             | ((payload[offset + 2] & 0xFF) << 16)
             | ((payload[offset + 3] & 0xFF) << 24);
    }

    public long getPayloadUint32(int offset) {
        return getPayloadInt32(offset) & 0xFFFFFFFFL;
    }
}
