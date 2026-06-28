package com.drone.dronesystem.protocol.mavlink;

/**
 * MAVLink v1 编解码器
 * 实现帧构建、CRC 校验、帧解析
 *
 * MAVLink v1 帧结构：
 * | STX(1) | LEN(1) | SEQ(1) | SYS(1) | COMP(1) | MSG(1) | PAYLOAD(0-255) | CRC(2) |
 */
public class MavLinkCodec {

    // ==================== 编码（构建发送帧） ====================

    /**
     * 将 MavLinkMessage 编码为可发送的字节数组
     */
    public static byte[] encode(MavLinkMessage msg) {
        int len = msg.payload.length;
        // 帧长度 = 6字节头 + 载荷 + 2字节CRC
        byte[] frame = new byte[6 + len + 2];

        // 帧头
        frame[0] = MavLinkMessage.STX_V1;
        frame[1] = (byte) len;
        frame[2] = (byte) msg.sequence;
        frame[3] = (byte) msg.systemId;
        frame[4] = (byte) msg.componentId;
        frame[5] = (byte) msg.messageId;

        // 载荷
        System.arraycopy(msg.payload, 0, frame, 6, len);

        // CRC（覆盖 STX 之外的所有字节）
        int crc = calculateCRC(frame, 1, 5 + len, msg.messageId);
        frame[6 + len] = (byte) (crc & 0xFF);
        frame[7 + len] = (byte) ((crc >> 8) & 0xFF);

        msg.crc = crc;
        return frame;
    }

    /**
     * 构建 HEARTBEAT 消息 (MSG_ID = 0)
     *
     * 载荷格式 (9 字节)：
     * type(1) autopilot(1) base_mode(1) custom_mode(4) system_status(1) mavlink_version(1)
     */
    public static MavLinkMessage buildHeartbeat(int sysId, int compId) {
        byte[] payload = new byte[9];
        payload[0] = 6;  // type: MAV_TYPE_GCS (地面站)
        payload[1] = 0;  // autopilot: MAV_AUTOPILOT_GENERIC
        payload[2] = 0;  // base_mode
        payload[3] = 0; payload[4] = 0; payload[5] = 0; payload[6] = 0; // custom_mode
        payload[7] = 4;  // system_status: MAV_STATE_ACTIVE
        payload[8] = 3;  // mavlink_version: 3 (兼容v1用3)

        return new MavLinkMessage(sysId, compId, MavLinkMessage.MSG_ID_HEARTBEAT, payload);
    }

    /**
     * 构建 COMMAND_LONG 消息 (MSG_ID = 76)
     *
     * 载荷格式 (33 字节)：
     * param1(4) param2(4) param3(4) param4(4) param5(4) param6(4) param7(4)
     * command(2) target_system(1) target_component(1) confirmation(1)
     */
    public static MavLinkMessage buildCommandLong(int sysId, int compId,
                                                   int command, float... params) {
        byte[] payload = new byte[33];

        // param1 ~ param7（小端序 float）
        for (int i = 0; i < 7; i++) {
            float val = (i < params.length) ? params[i] : 0f;
            int bits = Float.floatToIntBits(val);
            int offset = i * 4;
            payload[offset]     = (byte) (bits & 0xFF);
            payload[offset + 1] = (byte) ((bits >> 8) & 0xFF);
            payload[offset + 2] = (byte) ((bits >> 16) & 0xFF);
            payload[offset + 3] = (byte) ((bits >> 24) & 0xFF);
        }

        // command (uint16)
        payload[28] = (byte) (command & 0xFF);
        payload[29] = (byte) ((command >> 8) & 0xFF);

        // target_system, target_component, confirmation
        payload[30] = (byte) sysId;
        payload[31] = (byte) compId;
        payload[32] = 0;  // confirmation

        return new MavLinkMessage(sysId, compId, MavLinkMessage.MSG_ID_COMMAND_LONG, payload);
    }

    /**
     * 构建 SET_POSITION_TARGET_GLOBAL_INT 消息 (MSG_ID = 86)
     *
     * 载荷格式 (53 字节)：
     * time_boot_ms(4) lat_int(4) lon_int(4) alt(4)
     * vx(2) vy(2) vz(2) afx(2) afy(2) afz(2)
     * yaw(2) yaw_rate(4) type_mask(2)
     * target_system(1) target_component(1) coordinate_frame(1)
     */
    public static MavLinkMessage buildSetPositionTargetGlobalInt(
            int sysId, int compId, int latE7, int lonE7, float alt) {
        byte[] payload = new byte[53];

        // time_boot_ms = 0
        payload[0] = 0; payload[1] = 0; payload[2] = 0; payload[3] = 0;

        // lat_int (int32)
        payload[4] = (byte) (latE7 & 0xFF);
        payload[5] = (byte) ((latE7 >> 8) & 0xFF);
        payload[6] = (byte) ((latE7 >> 16) & 0xFF);
        payload[7] = (byte) ((latE7 >> 24) & 0xFF);

        // lon_int (int32)
        payload[8]  = (byte) (lonE7 & 0xFF);
        payload[9]  = (byte) ((lonE7 >> 8) & 0xFF);
        payload[10] = (byte) ((lonE7 >> 16) & 0xFF);
        payload[11] = (byte) ((lonE7 >> 24) & 0xFF);

        // alt (float, 小端序)
        int altBits = Float.floatToIntBits(alt);
        payload[12] = (byte) (altBits & 0xFF);
        payload[13] = (byte) ((altBits >> 8) & 0xFF);
        payload[14] = (byte) ((altBits >> 16) & 0xFF);
        payload[15] = (byte) ((altBits >> 24) & 0xFF);

        // vx, vy, vz = 0 (int16)
        for (int i = 16; i <= 21; i++) payload[i] = 0;

        // afx, afy, afz = 0 (int16)
        for (int i = 22; i <= 27; i++) payload[i] = 0;

        // yaw, yaw_rate = 0
        for (int i = 28; i <= 35; i++) payload[i] = 0;

        // type_mask: 只使用位置，忽略速度/加速度/偏航
        int typeMask = 0x0DF8; // 忽略 vx,vy,vz,afx,afy,afz,yaw,yaw_rate
        payload[36] = (byte) (typeMask & 0xFF);
        payload[37] = (byte) ((typeMask >> 8) & 0xFF);

        // target_system, target_component
        payload[38] = (byte) sysId;
        payload[39] = (byte) compId;

        // coordinate_frame: MAV_FRAME_GLOBAL_RELATIVE_ALT_INT (6)
        payload[40] = 6;

        return new MavLinkMessage(sysId, compId,
                MavLinkMessage.MSG_ID_SET_POSITION_TARGET_GLOBAL_INT, payload);
    }

    // ==================== 解码（解析接收帧） ====================

    /**
     * 从字节缓冲区解析一个 MavLinkMessage
     * @param buffer 原始字节
     * @param offset 起始偏移
     * @return 解析后的消息，或 null（数据不完整/校验失败）
     */
    public static MavLinkMessage decode(byte[] buffer, int offset) {
        if (buffer.length - offset < 8) return null; // 最小帧长度

        if ((buffer[offset] & 0xFF) != MavLinkMessage.STX_V1) return null;

        MavLinkMessage msg = new MavLinkMessage();
        msg.length = buffer[offset + 1] & 0xFF;
        msg.sequence = buffer[offset + 2] & 0xFF;
        msg.systemId = buffer[offset + 3] & 0xFF;
        msg.componentId = buffer[offset + 4] & 0xFF;
        msg.messageId = buffer[offset + 5] & 0xFF;

        // 检查数据是否足够
        int frameLen = 6 + msg.length + 2;
        if (buffer.length - offset < frameLen) return null;

        // 载荷
        msg.payload = new byte[msg.length];
        System.arraycopy(buffer, offset + 6, msg.payload, 0, msg.length);

        // CRC
        int receivedCrc = (buffer[offset + 6 + msg.length] & 0xFF)
                        | ((buffer[offset + 7 + msg.length] & 0xFF) << 8);

        // 验证 CRC
        int calcCrc = calculateCRC(buffer, offset + 1, 5 + msg.length, msg.messageId);
        if (receivedCrc != calcCrc) return null;

        msg.crc = receivedCrc;
        return msg;
    }

    /**
     * 解析 HEARTBEAT 消息，返回系统类型
     */
    public static int parseHeartbeatType(MavLinkMessage msg) {
        if (msg.messageId != MavLinkMessage.MSG_ID_HEARTBEAT || msg.payload.length < 1) return -1;
        return msg.getPayloadUint8(0);
    }

    /**
     * 解析 GPS_RAW_INT 消息
     * @return double[] {lat(度), lon(度), alt(m), satellites_visible}
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
     * 解析 ATTITUDE 消息
     * @return float[] {roll(弧度), pitch(弧度), yaw(弧度)}
     */
    public static float[] parseAttitude(MavLinkMessage msg) {
        if (msg.messageId != MavLinkMessage.MSG_ID_ATTITUDE || msg.payload.length < 28) return null;
        float roll = Float.intBitsToFloat(msg.getPayloadInt32(4));
        float pitch = Float.intBitsToFloat(msg.getPayloadInt32(8));
        float yaw = Float.intBitsToFloat(msg.getPayloadInt32(12));
        return new float[]{roll, pitch, yaw};
    }

    /**
     * 解析 SYS_STATUS 消息
     * @return int[] {voltage_battery(mV), battery_remaining(%)}
     */
    public static int[] parseSysStatus(MavLinkMessage msg) {
        if (msg.messageId != MavLinkMessage.MSG_ID_SYS_STATUS || msg.payload.length < 31) return null;
        int voltage = msg.getPayloadUint16(12);   // mV
        int battery = (byte) msg.getPayloadUint8(28); // % (-1 表示未知，需要有符号)
        return new int[]{voltage, battery};
    }

    /**
     * 解析 COMMAND_ACK 消息
     * @return int result (0=ACCEPTED, 1=TEMP_REJECTED, 2=DENIED, 3=UNSUPPORTED, 4=FAILED, 5=IN_PROGRESS)
     */
    public static int parseCommandAck(MavLinkMessage msg) {
        if (msg.messageId != MavLinkMessage.MSG_ID_COMMAND_ACK || msg.payload.length < 3) return -1;
        return msg.getPayloadUint8(2);
    }

    // ==================== CRC 计算 ====================

    /**
     * MAVLink CRC-16/MCRF4XX 校验
     * @param data 数据
     * @param start 起始位置（跳过 STX）
     * @param length 数据长度
     * @param msgId 消息 ID（用于 extra CRC byte）
     */
    public static int calculateCRC(byte[] data, int start, int length, int msgId) {
        int crc = 0xFFFF;
        for (int i = start; i < start + length; i++) {
            crc ^= (data[i] & 0xFF);
            for (int j = 0; j < 8; j++) {
                if ((crc & 1) != 0) {
                    crc = (crc >> 1) ^ 0xA001; // CRC-16/MCRF4XX 多项式
                } else {
                    crc >>= 1;
                }
            }
        }

        // 加入 extra CRC byte（每个消息 ID 对应一个额外字节）
        int extraCrc = getExtraCrc(msgId);
        crc ^= extraCrc;
        for (int j = 0; j < 8; j++) {
            if ((crc & 1) != 0) {
                crc = (crc >> 1) ^ 0xA001;
            } else {
                crc >>= 1;
            }
        }

        return crc & 0xFFFF;
    }

    /**
     * 获取消息 ID 对应的 extra CRC byte
     * 完整列表见：https://github.com/mavlink/c_library_v2/blob/master/checksum.h
     * 这里只列出本项目用到的
     */
    private static int getExtraCrc(int msgId) {
        switch (msgId) {
            case MavLinkMessage.MSG_ID_HEARTBEAT:                    return 50;
            case MavLinkMessage.MSG_ID_SYS_STATUS:                   return 156;
            case MavLinkMessage.MSG_ID_GPS_RAW_INT:                  return 24;
            case MavLinkMessage.MSG_ID_ATTITUDE:                     return 39;
            case MavLinkMessage.MSG_ID_COMMAND_LONG:                 return 152;
            case MavLinkMessage.MSG_ID_COMMAND_ACK:                  return 143;
            case MavLinkMessage.MSG_ID_SET_POSITION_TARGET_GLOBAL_INT: return 143;
            default: return 0;
        }
    }
}
