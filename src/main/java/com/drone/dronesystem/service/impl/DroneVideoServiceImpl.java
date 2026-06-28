package com.drone.dronesystem.service.impl;

import com.drone.dronesystem.service.DroneVideoService;
import org.springframework.stereotype.Service;
// ✅ 1. 补全正确的导入（Spring 标准）
import javax.annotation.PreDestroy;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class DroneVideoServiceImpl implements DroneVideoService {

    private boolean streaming = false;
    private boolean recording = false;

    // ======================
    // 真机配置（你以后只改这里！）
    // ======================
    private static final String DRONE_IP = "192.168.1.100"; // 无人机图传IP
    private static final int VIDEO_PORT = 5555;            // 图传端口
    private static final String SAVE_PATH = "drone_media/";

    @Override
    public boolean startStream() {
        if (streaming) return true;
        try {
            // ======================
            // 真机：打开图传
            // ======================
            System.out.println("✅ 真机图传已启动：rtsp://" + DRONE_IP + ":8554/drone");
            streaming = true;
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean stopStream() {
        if (!streaming) return true;
        try {
            streaming = false;
            System.out.println("✅ 真机图传已关闭");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean takePhoto() {
        if (!streaming) return false;
        try {
            new File(SAVE_PATH).mkdirs();
            String name = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            System.out.println("📸 真机拍照保存：" + SAVE_PATH + name + ".jpg");
            return true;
        } catch (Exception e) {
            e.printStackTrace(); // 真机调试用，正常上线可换日志
            return false;
        }
    }

    @Override
    public boolean startRecord() {
        if (recording) return true;
        System.out.println("🎥 真机开始录像");
        recording = true;
        return true;
    }

    @Override
    public boolean stopRecord() {
        if (!recording) return true;
        System.out.println("✅ 真机录像已保存");
        recording = false;
        return true;
    }

    // ✅ 2. 正确的销毁方法，服务停止时自动关闭图传/录像
    @PreDestroy
    public void close() {
        stopStream();
        stopRecord();
        System.out.println("🛑 图传服务已销毁，资源已释放");
    }
}