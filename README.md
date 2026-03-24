# 无人机实时操控与图传系统
基于 SpringBoot + WebSocket 的无人机地面站控制系统，支持真机开发、毕业设计与工程实践。

## 🔧 技术栈
- **后端框架**：SpringBoot 3.x
- **实时通信**：WebSocket
- **硬件通信**：RXTX 串口通信（对接真机飞控）
- **视频传输**：RTSP 视频流
- **构建工具**：Maven

## 🚀 系统功能
1. **飞行控制**：支持串口连接真机，实现一键起飞、降落、返航、悬停。
2. **实时监控**：通过 WebSocket 低延迟推送电量、高度、速度、GPS 等状态数据。
3. **图传系统**：支持 RTSP 视频流实时显示，支持在线拍照、本地录像。
4. **B/S 架构**：浏览器直接访问，无需安装客户端，远程操控。

## 📦 运行说明
1. 环境：JDK 17+
2. 配置：修改 `SerialPortUtil` 串口号与 `DroneVideoServiceImpl` 图传 IP。
3. 启动：运行 `DroneSystemApplication` 主类。
4. 访问：浏览器打开 `http://localhost:8080`。

## 📁 仓库结构
- `/src`：完整 Java 源码
- `/src/main/resources/static`：前端 HTML 操控页面
- `pom.xml`：Maven 依赖配置

##作者的话
-这个项目只是一个半成品，它的后端基本功能已经搞定了，前端页面也写了，略微简陋，有兴趣的童鞋可以自己试试
