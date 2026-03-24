package com.drone.dronesystem.service;

/**
 * 真机视频图传服务
 */
public interface DroneVideoService {

    // 开启图传
    boolean startStream();

    // 停止图传
    boolean stopStream();

    // 拍照
    boolean takePhoto();

    // 开始录像
    boolean startRecord();

    // 停止录像
    boolean stopRecord();
}