package com.drone.dronesystem.protocol.mavlink;

/**
 * MAVLink v1 消息结构
 *
 * 帧格式：
 * | 字段 | 长度 | 说明 |
 * |------|------|------|
 * | STX  | 1B   | 起始标记 0xFE |
 * | LEN  | 1B   | 载荷长度 |
 * | SEQ  | 1B   | 序列号 |
 * | SYS  | 1B   | 系统 ID |
 * | COMP | 1B   | 组件 ID |
 * | MSG  | 1B   | 消息 ID |
 * | PLD  | nB   | 载荷 |
 * | CRC  | 2B   | 校验和 |
 *
 * 参考：https://mavlink.io/en/guide/serialization.html
 */
public class MavLinkMessage {

    // MAVLink v1 起始标记
    public static final byte STX_V1 = (byte) 0xFE;

    // 常用消息 ID
    public static final int MSG_ID_HEARTBEAT = 0;
    public static final int MSG_ID_SYS_STATUS = 1;
    public static final int MSG_ID_GPS_RAW_INT = 24;
    public static final int MSG_ID_ATTITUDE = 30;
    public static final int MSG_ID_COMMAND_ACK = 77;
    public static final int MSG_ID_COMMAND_LONG = 76;
    public static final int MSG_ID_SET_POSITION_TARGET_GLOBAL_INT = 86;

    // 常用命令 ID（COMMAND_LONG 的 command 字段）
    public static final int CMD_NAV_TAKEOFF = 22;
    public static final int CMD_NAV_LAND = 21;
    public static final int CMD_NAV_RETURN_TO_LAUNCH = 20;
    public static final int CMD_DO_SET_MODE = 176;

    // MAVLink 自定义 CRC 种子（每个消息 ID 对应一个额外字节）
    // 这里只列出需要用到的
    public static final int EXTRA_CRC_HEARTBEAT = 50;
    public static final int EXTRA_CRC_COMMAND_LONG = 152;
    public static final int EXTRA_CRC_SET_POSITION_TARGET_GLOBAL_INT = 143;
    public static final int EXTRA_CRC_GPS_RAW_INT = 24;
    public static final int EXTRA_CRC_ATTITUDE = 39;
    public static final int EXTRA_CRC_SYS_STATUS = 156;

    // 消息字段
    public int length;       // 载荷长度
    public int sequence;     // 序列号
    public int systemId;     // 系统 ID
    public int componentId;  // 组件 ID
    public int messageId;    // 消息 ID
    public byte[] payload;   // 载荷
    public int crc;          // 校验和

    public MavLinkMessage() {}

    public MavLinkMessage(int sysId, int compId, int msgId, byte[] payload) {
        this.length = payload.length;
        this.systemId = sysId;
        this.componentId = compId;
        this.messageId = msgId;
        this.payload = payload;
    }

    /**
     * 获取指定字节序的 payload 字段值（小端序）
     */
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
