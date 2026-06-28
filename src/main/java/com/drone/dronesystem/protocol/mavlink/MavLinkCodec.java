package com.drone.dronesystem.protocol.mavlink;

/**
 * MAVLink v1/v2 编解码器
 * 支持收发 MAVLink v1 和 v2 帧，兼容所有 Pixhawk/ArduPilot/PX4 飞控
 *
 * v1 帧结构（发送用）：
 * | STX=0xFE(1) | LEN(1) | SEQ(1) | SYS(1) | COMP(1) | MSG(1) | PAYLOAD(0-255) | CRC(2) |
 *
 * v2 帧结构（接收解析）：
 * | STX=0xFD(1) | LEN(1) | INCOMPAT(1) | COMPAT(1) | SEQ(1) | SYS(1) | COMP(1) | MSG(3) | PAYLOAD(0-255) | CRC(2) | [SIGN(13)] |
 */
public class MavLinkCodec {

    // ==================== 编码（构建 v1 发送帧） ====================

    /**
     * 将 MavLinkMessage 编码为 v1 字节数组
     * 地面站发 v1 帧，飞控都能接受
     */
    public static byte[] encode(MavLinkMessage msg) {
        int len = msg.payload.length;
        byte[] frame = new byte[6 + len + 2];

        frame[0] = MavLinkMessage.STX_V1;
        frame[1] = (byte) len;
        frame[2] = (byte) msg.sequence;
        frame[3] = (byte) msg.systemId;
        frame[4] = (byte) msg.componentId;
        frame[5] = (byte) msg.messageId;

        System.arraycopy(msg.payload, 0, frame, 6, len);

        int crc = calculateCRC(frame, 1, 5 + len, msg.messageId);
        frame[6 + len] = (byte) (crc & 0xFF);
        frame[7 + len] = (byte) ((crc >> 8) & 0xFF);

        msg.crc = crc;
        return frame;
    }

    // ==================== 解码（解析 v1 或 v2 接收帧） ====================

    /**
     * 从字节缓冲区解析一个 MavLinkMessage（自动识别 v1/v2）
     * @param buffer 原始字节
     * @param offset 起始偏移
     * @return 解析后的消息，或 null（数据不完整/校验失败）
     */
    public static MavLinkMessage decode(byte[] buffer, int offset) {
        if (buffer.length - offset < 8) return null;

        int stx = buffer[offset] & 0xFF;

        if (stx == MavLinkMessage.STX_V1) {
            return decodeV1(buffer, offset);
        } else if (stx == MavLinkMessage.STX_V2) {
            return decodeV2(buffer, offset);
        }

        return null; // 未知帧头
    }

    /**
     * 解析 MAVLink v1 帧
     */
    private static MavLinkMessage decodeV1(byte[] buffer, int offset) {
        if (buffer.length - offset < 8) return null;

        MavLinkMessage msg = new MavLinkMessage();
        msg.length = buffer[offset + 1] & 0xFF;
        msg.sequence = buffer[offset + 2] & 0xFF;
        msg.systemId = buffer[offset + 3] & 0xFF;
        msg.componentId = buffer[offset + 4] & 0xFF;
        msg.messageId = buffer[offset + 5] & 0xFF;

        int frameLen = 6 + msg.length + 2;
        if (buffer.length - offset < frameLen) return null;

        msg.payload = new byte[msg.length];
        System.arraycopy(buffer, offset + 6, msg.payload, 0, msg.length);

        int receivedCrc = (buffer[offset + 6 + msg.length] & 0xFF)
                        | ((buffer[offset + 7 + msg.length] & 0xFF) << 8);

        int calcCrc = calculateCRC(buffer, offset + 1, 5 + msg.length, msg.messageId);
        if (receivedCrc != calcCrc) return null;

        msg.crc = receivedCrc;
        msg.isV2 = false;
        return msg;
    }

    /**
     * 解析 MAVLink v2 帧
     * v2 头部比 v1 多 4 字节：incompat_flags, compat_flags, msgid(3字节)
     */
    private static MavLinkMessage decodeV2(byte[] buffer, int offset) {
        if (buffer.length - offset < 12) return null; // v2 最小帧长

        MavLinkMessage msg = new MavLinkMessage();
        msg.length = buffer[offset + 1] & 0xFF;
        int incompatFlags = buffer[offset + 2] & 0xFF;
        // int compatFlags = buffer[offset + 3] & 0xFF; // 暂未使用
        msg.sequence = buffer[offset + 4] & 0xFF;
        msg.systemId = buffer[offset + 5] & 0xFF;
        msg.componentId = buffer[offset + 6] & 0xFF;

        // v2 消息 ID 是 3 字节（小端序）
        msg.messageId = (buffer[offset + 7] & 0xFF)
                      | ((buffer[offset + 8] & 0xFF) << 8)
                      | ((buffer[offset + 9] & 0xFF) << 16);

        // 是否有签名（incompat_flags bit 0）
        int signatureLen = (incompatFlags & 0x01) != 0 ? 13 : 0;

        int frameLen = 10 + msg.length + 2 + signatureLen;
        if (buffer.length - offset < frameLen) return null;

        msg.payload = new byte[msg.length];
        System.arraycopy(buffer, offset + 10, msg.payload, 0, msg.length);

        int crcOffset = offset + 10 + msg.length;
        int receivedCrc = (buffer[crcOffset] & 0xFF)
                        | ((buffer[crcOffset + 1] & 0xFF) << 8);

        // CRC 计算范围：从 LEN 到 PAYLOAD 末尾（跳过 STX）
        int calcCrc = calculateCRC(buffer, offset + 1, 9 + msg.length, msg.messageId);
        if (receivedCrc != calcCrc) return null;

        msg.crc = receivedCrc;
        msg.isV2 = true;
        return msg;
    }

    // ==================== 消息构建 ====================

    /**
     * 构建 HEARTBEAT 消息 (MSG_ID = 0)
     */
    public static MavLinkMessage buildHeartbeat(int sysId, int compId) {
        byte[] payload = new byte[9];
        payload[0] = 6;  // type: MAV_TYPE_GCS
        payload[1] = 0;  // autopilot: MAV_AUTOPILOT_GENERIC
        payload[2] = 0;  // base_mode
        payload[3] = 0; payload[4] = 0; payload[5] = 0; payload[6] = 0;
        payload[7] = 4;  // system_status: MAV_STATE_ACTIVE
        payload[8] = 3;  // mavlink_version: 3
        return new MavLinkMessage(sysId, compId, MavLinkMessage.MSG_ID_HEARTBEAT, payload);
    }

    /**
     * 构建 COMMAND_LONG 消息 (MSG_ID = 76)
     */
    public static MavLinkMessage buildCommandLong(int sysId, int compId,
                                                   int command, float... params) {
        byte[] payload = new byte[33];

        for (int i = 0; i < 7; i++) {
            float val = (i < params.length) ? params[i] : 0f;
            int bits = Float.floatToIntBits(val);
            int off = i * 4;
            payload[off]     = (byte) (bits & 0xFF);
            payload[off + 1] = (byte) ((bits >> 8) & 0xFF);
            payload[off + 2] = (byte) ((bits >> 16) & 0xFF);
            payload[off + 3] = (byte) ((bits >> 24) & 0xFF);
        }

        payload[28] = (byte) (command & 0xFF);
        payload[29] = (byte) ((command >> 8) & 0xFF);
        payload[30] = (byte) sysId;
        payload[31] = (byte) compId;
        payload[32] = 0;

        return new MavLinkMessage(sysId, compId, MavLinkMessage.MSG_ID_COMMAND_LONG, payload);
    }

    /**
     * 构建 SET_POSITION_TARGET_GLOBAL_INT 消息 (MSG_ID = 86)
     */
    public static MavLinkMessage buildSetPositionTargetGlobalInt(
            int sysId, int compId, int latE7, int lonE7, float alt) {
        byte[] payload = new byte[53];

        // time_boot_ms = 0
        payload[0] = 0; payload[1] = 0; payload[2] = 0; payload[3] = 0;

        // lat_int
        payload[4] = (byte) (latE7 & 0xFF);
        payload[5] = (byte) ((latE7 >> 8) & 0xFF);
        payload[6] = (byte) ((latE7 >> 16) & 0xFF);
        payload[7] = (byte) ((latE7 >> 24) & 0xFF);

        // lon_int
        payload[8]  = (byte) (lonE7 & 0xFF);
        payload[9]  = (byte) ((lonE7 >> 8) & 0xFF);
        payload[10] = (byte) ((lonE7 >> 16) & 0xFF);
        payload[11] = (byte) ((lonE7 >> 24) & 0xFF);

        // alt (float)
        int altBits = Float.floatToIntBits(alt);
        payload[12] = (byte) (altBits & 0xFF);
        payload[13] = (byte) ((altBits >> 8) & 0xFF);
        payload[14] = (byte) ((altBits >> 16) & 0xFF);
        payload[15] = (byte) ((altBits >> 24) & 0xFF);

        // vx, vy, vz, afx, afy, afz, yaw, yaw_rate = 0
        for (int i = 16; i <= 35; i++) payload[i] = 0;

        // type_mask: 只使用位置
        int typeMask = 0x0DF8;
        payload[36] = (byte) (typeMask & 0xFF);
        payload[37] = (byte) ((typeMask >> 8) & 0xFF);

        payload[38] = (byte) sysId;
        payload[39] = (byte) compId;
        payload[40] = 6; // MAV_FRAME_GLOBAL_RELATIVE_ALT_INT

        return new MavLinkMessage(sysId, compId,
                MavLinkMessage.MSG_ID_SET_POSITION_TARGET_GLOBAL_INT, payload);
    }

    // ==================== 消息解析 ====================

    public static int parseHeartbeatType(MavLinkMessage msg) {
        if (msg.messageId != MavLinkMessage.MSG_ID_HEARTBEAT || msg.payload.length < 1) return -1;
        return msg.getPayloadUint8(0);
    }

    /**
     * 解析 GPS_RAW_INT → {lat(度), lon(度), alt(m), sats}
     */
    public static double[] parseGpsRawInt(MavLinkMessage msg) {
        if (msg.messageId != MavLinkMessage.MSG_ID_GPS_RAW_INT || msg.payload.length < 30) return null;
        int latE7 = msg.getPayloadInt32(4);
        int lonE7 = msg.getPayloadInt32(8);
        int altMm = msg.getPayloadInt32(12);
        int sats = msg.getPayloadUint8(27);
        return new double[]{latE7 / 1e7, lonE7 / 1e7, altMm / 1000.0, sats};
    }

    /**
     * 解析 ATTITUDE → {roll, pitch, yaw}（弧度）
     */
    public static float[] parseAttitude(MavLinkMessage msg) {
        if (msg.messageId != MavLinkMessage.MSG_ID_ATTITUDE || msg.payload.length < 28) return null;
        float roll = Float.intBitsToFloat(msg.getPayloadInt32(4));
        float pitch = Float.intBitsToFloat(msg.getPayloadInt32(8));
        float yaw = Float.intBitsToFloat(msg.getPayloadInt32(12));
        return new float[]{roll, pitch, yaw};
    }

    /**
     * 解析 SYS_STATUS → {voltage_battery(mV), battery_remaining(%)}
     */
    public static int[] parseSysStatus(MavLinkMessage msg) {
        if (msg.messageId != MavLinkMessage.MSG_ID_SYS_STATUS || msg.payload.length < 31) return null;
        int voltage = msg.getPayloadUint16(12);
        int battery = (byte) msg.getPayloadUint8(28);
        return new int[]{voltage, battery};
    }

    /**
     * 解析 GLOBAL_POSITION_INT → {lat(度), lon(度), alt(m), relative_alt(m), vx, vy}
     */
    public static double[] parseGlobalPositionInt(MavLinkMessage msg) {
        if (msg.messageId != MavLinkMessage.MSG_ID_GLOBAL_POSITION_INT || msg.payload.length < 28) return null;
        int latE7 = msg.getPayloadInt32(0);
        int lonE7 = msg.getPayloadInt32(4);
        int altMm = msg.getPayloadInt32(8);
        int relAltMm = msg.getPayloadInt32(12);
        int vx = msg.getPayloadInt16(16); // cm/s
        int vy = msg.getPayloadInt16(18); // cm/s
        return new double[]{latE7 / 1e7, lonE7 / 1e7, altMm / 1000.0, relAltMm / 1000.0, vx / 100.0, vy / 100.0};
    }

    /**
     * 解析 COMMAND_ACK → result
     */
    public static int parseCommandAck(MavLinkMessage msg) {
        if (msg.messageId != MavLinkMessage.MSG_ID_COMMAND_ACK || msg.payload.length < 3) return -1;
        return msg.getPayloadUint8(2);
    }

    // ==================== CRC ====================

    /**
     * MAVLink CRC-16/MCRF4XX
     */
    public static int calculateCRC(byte[] data, int start, int length, int msgId) {
        int crc = 0xFFFF;
        for (int i = start; i < start + length; i++) {
            crc ^= (data[i] & 0xFF);
            for (int j = 0; j < 8; j++) {
                crc = (crc & 1) != 0 ? (crc >> 1) ^ 0xA001 : crc >> 1;
            }
        }

        // extra CRC byte
        int extra = getExtraCrc(msgId);
        if (extra != 0) {
            crc ^= extra;
            for (int j = 0; j < 8; j++) {
                crc = (crc & 1) != 0 ? (crc >> 1) ^ 0xA001 : crc >> 1;
            }
        }

        return crc & 0xFFFF;
    }

    /**
     * extra CRC byte — 完整列表见 mavlink 源码 checksum.h
     * 这里列出所有可能用到的消息
     */
    private static int getExtraCrc(int msgId) {
        switch (msgId) {
            case 0:   return 50;   // HEARTBEAT
            case 1:   return 156;  // SYS_STATUS
            case 24:  return 24;   // GPS_RAW_INT
            case 30:  return 39;   // ATTITUDE
            case 33:  return 104;  // GLOBAL_POSITION_INT
            case 74:  return 20;   // VFR_HUD
            case 76:  return 152;  // COMMAND_LONG
            case 77:  return 143;  // COMMAND_ACK
            case 86:  return 143;  // SET_POSITION_TARGET_GLOBAL_INT
            case 253: return 83;   // STATUSTEXT
            default:  return 0;
        }
    }
}
