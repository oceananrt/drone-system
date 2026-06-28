# 🛩️ Drone System — 无人机地面站后端系统

基于 Spring Boot 的无人机地面站后端，支持真机串口通信、飞行控制、电子围栏、航线任务、远程控制、视频图传和 WebSocket 实时数据推送。

---

## 📋 目录

- [技术栈](#技术栈)
- [项目架构](#项目架构)
- [功能模块](#功能模块)
- [快速开始](#快速开始)
- [前端地面站](#前端地面站)
- [API 接口文档](#api-接口文档)
- [通信协议](#通信协议)
- [安全机制](#安全机制)
- [部署教程](#部署教程)
- [常见问题](#常见问题)
- [项目结构](#项目结构)

---

## 技术栈

| 类别 | 技术 | 版本 |
|------|------|------|
| 语言 | Java | 1.8 (Java 8) |
| 框架 | Spring Boot | 2.7.18 |
| ORM | Spring Data JPA + Hibernate | 5.6.15 |
| 数据库 | H2（内存库，开发用） | - |
| WebSocket | JSR 356 (javax.websocket) | - |
| 串口通信 | jSerialComm | 2.10.0 |
| JSON | Fastjson2 + Jackson | 2.0.41 |
| 构建 | Maven | 3.9.x |
| 嵌入式服务器 | Tomcat | 9.0.83 |

---

## 项目架构

```
┌─────────────────────────────────────────────────────────────────┐
│                        前端地面站 (index.html)                    │
│                   WebSocket 实时数据 + HTTP POST 指令              │
└──────────────┬──────────────────────────────────┬────────────────┘
               │ WebSocket                        │ HTTP POST
               ▼                                  ▼
┌──────────────────────────────────────────────────────────────────┐
│                    Controller 层 (8 个)                           │
│  /api/drone/*    飞行控制        /api/fly/*     飞行指令          │
│  /api/map/*      地图轨迹        /api/mission/* 航线任务          │
│  /api/task/*     任务队列        /api/video/*   图传拍照          │
│  /api/remote/*   远程控制        /ws/drone      WebSocket         │
└──────────────────────────────┬───────────────────────────────────┘
                               │
┌──────────────────────────────▼───────────────────────────────────┐
│                    Service 层 (12 个)                             │
│  DroneRealService    ← 核心守护线程：轮询串口 → 安全/围栏/日志    │
│  DroneFlyService     ← 起飞/降落/悬停/返航/设高/飞到GPS          │
│  DroneSafetyService  ← 电量/GPS/高度 三级安全检查                 │
│  DroneFenceService   ← 电子围栏（经纬度+限高）                    │
│  DroneWaypointService← 航线任务（多航点顺序执行）                 │
│  DroneTaskService    ← 通用任务队列                               │
│  DroneVideoService   ← 图传/拍照/录像                             │
│  DroneRemoteService  ← TCP Socket 远程控制（4G/5G）              │
│  DroneMapService     ← 当前位置 + 历史轨迹                       │
│  DroneLogService     ← 飞行日志持久化                             │
│  DroneWebsocketService ← WebSocket 广播                          │
└──────────────────────────────┬───────────────────────────────────┘
                               │
┌──────────────────────────────▼───────────────────────────────────┐
│                    Protocol 层                                    │
│  SerialPortUtil   ← jSerialComm 串口读写 (115200/8N1)            │
│  DroneParser      ← 二进制协议解析器                              │
│  DroneSender      ← 指令组帧 + 发送 (帧头AA55 + 帧尾0D0A)        │
│  DroneCmd         ← 指令枚举 (CONNECT/TAKEOFF/LAND/...)          │
│  DroneHeartbeat   ← 心跳包                                       │
│  MavLinkUtil      ← MAVLink 协议（待对接真机飞控）               │
└──────────────────────────────┬───────────────────────────────────┘
                               │
                    ┌──────────▼──────────┐
                    │   飞控 / 数传电台     │
                    │  (串口 / MAVLink)    │
                    └─────────────────────┘
```

### 核心数据流

```
飞控 ──串口──→ SerialPortUtil.read()
                    ↓
              DroneParser.parse() → DroneRawData
                    ↓
         DroneRealServiceImpl.getRealData() → DroneRealData
                    ↓
         守护线程（1秒轮询）
              ├── SafetyService  → 电量低? 强制返航/降落
              ├── FenceService   → 超出围栏? 强制悬停
              ├── LogService     → 保存飞行日志到数据库
              └── WebSocket      → 实时推送到前端页面
```

---

## 功能模块

### 1. 飞行控制
- 起飞 / 降落 / 悬停 / 一键返航
- 设置目标高度
- 飞至指定 GPS 坐标点

### 2. 安全保护
- **电量保护**：电量 ≤20% 自动返航，≤10% 强制降落
- **GPS 保护**：卫星数 <6 颗时强制悬停
- **高度保护**：超过 150m 自动降高

### 3. 电子围栏
- 经纬度边界限制（可配置）
- 超出围栏自动悬停
- 超限高自动降至安全高度

### 4. 航线任务
- 添加多个航点（经纬度 + 高度 + 悬停时间）
- 按顺序自动执行航点飞行
- 任务完成自动返航
- 支持中途停止任务

### 5. 通用任务队列
- 支持任务类型：起飞、降落、悬停、返航、飞至GPS、拍照、录像
- 可设置任务间延时
- 队列式顺序执行

### 6. 远程控制
- TCP Socket 连接远程服务器（4G/5G 公网）
- 实时上报无人机状态（JSON 格式）
- 接收远程指令（TAKEOFF / LAND / RETURN_HOME / HOVER）

### 7. 视频图传
- 开启/关闭图传（RTSP 流）
- 拍照保存
- 录像开始/停止

### 8. 实时数据推送
- WebSocket 端点 `/ws/drone`
- 1 秒间隔广播无人机实时数据
- 支持警告消息推送

### 9. 飞行日志
- 自动记录飞行轨迹（经纬度、高度、电量、状态）
- JPA 持久化到数据库
- 支持查询历史轨迹

---

## 快速开始

### 环境要求

| 要求 | 版本 |
|------|------|
| JDK | 1.8 及以上 |
| Maven | 3.6 及以上（或使用项目自带 mvnw） |

### 1. 克隆项目

```bash
git clone <仓库地址>
cd drone-system
```

### 2. 编译

```bash
# Windows
mvnw.cmd clean compile

# Linux/Mac
./mvnw clean compile
```

### 3. 运行测试

```bash
mvnw.cmd test
```

### 4. 启动应用

```bash
# 方式一：Maven 启动
mvnw.cmd spring-boot:run

# 方式二：打包后运行
mvnw.cmd clean package
java -jar target/drone-system-0.0.1-SNAPSHOT.jar
```

### 5. 访问

启动成功后（看到 `Started DroneSystemApplication`），打开浏览器：

```
http://localhost:8080
```

---

## 前端地面站

打开 http://localhost:8080 即可看到地面站界面：

### 界面布局

```
┌────────────────────┬──────────────────────────────┐
│  📡 无人机地面站     │                              │
│                    │       实时图传画面             │
│  ● WebSocket：已连接│                              │
│  连接状态：已连接    ├──────────────────────────────┤
│  电量：85%         │                              │
│  高度：50.0m       │                              │
│  速度：5.2m/s      │       地图区域                │
│  GPS：12颗         │                              │
│  状态：飞行中        │                              │
│                    │                              │
│  [连接无人机]       │                              │
│  [起飞]            │                              │
│  [降落]            │                              │
│  [返航]            │                              │
│  [紧急停止]         │                              │
│  [开始航线任务]     │                              │
│                    │                              │
│  ┌─ 日志窗口 ────┐ │                              │
│  │ 17:20:01 地面站 │ │                              │
│  │ 17:20:02 已连接 │ │                              │
│  └────────────────┘ │                              │
└────────────────────┴──────────────────────────────┘
```

### 特性

- **WebSocket 实时数据**：页面打开后自动连接 WebSocket，每秒接收无人机状态更新
- **自动重连**：WebSocket 断开后指数退避重连（最多 10 次）
- **电量颜色**：>20% 绿色，10%-20% 黄色，<10% 红色
- **POST 指令**：所有控制按钮通过 POST 请求发送，符合 RESTful 规范

---

## API 接口文档

### 无人机控制 `/api/drone`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/drone/connect` | 连接无人机（串口） |
| GET | `/api/drone/data` | 获取实时数据 |
| POST | `/api/drone/takeoff` | 起飞 |
| POST | `/api/drone/land` | 降落 |
| POST | `/api/drone/returnHome` | 一键返航 |
| POST | `/api/drone/hover` | 悬停 |

**实时数据返回示例：**
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "droneId": 1,
    "droneName": "真机-01",
    "altitude": 50.0,
    "speed": 5.2,
    "voltage": 12.4,
    "battery": 85,
    "lat": 23.1234567,
    "lng": 113.6543210,
    "gpsNum": 12,
    "flyStatus": "FLYING",
    "connected": true
  }
}
```

### 飞行指令 `/api/fly`

| 方法 | 路径 | 参数 | 说明 |
|------|------|------|------|
| POST | `/api/fly/takeoff` | `height` (默认10) | 起飞到指定高度 |
| POST | `/api/fly/land` | - | 降落 |
| POST | `/api/fly/hover` | - | 悬停 |
| POST | `/api/fly/returnHome` | - | 返航 |
| POST | `/api/fly/setHeight` | `height` | 设置目标高度 |

### 航线任务 `/api/mission`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/mission/add` | 添加航点（JSON Body） |
| POST | `/api/mission/start` | 开始执行航线 |
| POST | `/api/mission/stop` | 停止航线任务 |

**添加航点请求体：**
```json
{
  "lat": 23.1234567,
  "lng": 113.6543210,
  "height": 50,
  "speed": 5,
  "hoverTime": 10
}
```

### 任务队列 `/api/task`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/task/add` | 添加任务（JSON Body） |
| POST | `/api/task/start` | 开始执行队列 |
| POST | `/api/task/stop` | 停止任务 |

**添加任务请求体：**
```json
{
  "type": "TAKEOFF",
  "lat": 0,
  "lng": 0,
  "height": 20,
  "delaySeconds": 5
}
```

**任务类型枚举：** `TAKEOFF` / `LAND` / `HOVER` / `RETURN_HOME` / `GO_TO_GPS` / `TAKE_PHOTO` / `RECORD_VIDEO`

### 地图 `/api/map`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/map/location` | 获取当前 GPS 位置 |
| GET | `/api/map/path` | 获取历史飞行轨迹 |
| POST | `/api/map/clear` | 清空轨迹 |

### 视频图传 `/api/video`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/video/start` | 开启图传 |
| POST | `/api/video/stop` | 关闭图传 |
| POST | `/api/video/photo` | 拍照 |
| POST | `/api/video/record/start` | 开始录像 |
| POST | `/api/video/record/stop` | 停止录像 |

### 远程控制 `/api/remote`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/remote/connect` | 连接远程服务器 |
| POST | `/api/remote/disconnect` | 断开远程连接 |

### WebSocket `/ws/drone`

```
ws://localhost:8080/ws/drone
```

**推送数据格式：**
```json
// 实时数据
{"droneId":1,"altitude":50.0,"speed":5.2,"battery":85,...}

// 警告消息
{"warn":"⚠️ 电量过低，自动返航"}
```

---

## 通信协议

### 串口配置

| 参数 | 值 |
|------|------|
| 波特率 | 115200 |
| 数据位 | 8 |
| 停止位 | 1 |
| 校验 | 无 |

### 指令帧格式

```
帧头(2B) + 指令(1B) + 数据(4B) + 帧尾(2B)
AA 55    + CMD     + DATA     + 0D 0A
```

| 指令 | 代码 | 说明 |
|------|------|------|
| CONNECT | 0x01 | 连接飞控 |
| TAKEOFF | 0x02 | 起飞 |
| LAND | 0x03 | 降落 |
| RETURN_HOME | 0x04 | 一键返航 |
| HOVER | 0x05 | 悬停 |
| SET_ALT | 0x06 | 设置目标高度 |
| SET_SPEED | 0x07 | 设置速度 |
| SET_GPS | 0x08 | 设置 GPS 坐标 |

### GPS 指令帧格式

```
AA 55 08 [lat:4字节] [lng:4字节] [height:2字节] 0D 0A
```

经纬度以 1e7 度为单位（整数传输），与解析端一致。

### 上行数据帧格式

```
帧头(2B) + 高度(2B) + 速度(2B) + 电压(2B) + 电量(1B) +
飞行模式(1B) + GPS星数(1B) + 纬度(4B) + 经度(4B) +
校验(1B) + 帧尾(2B)

共 22 字节
```

### 心跳包

```
AA 55 00 0D 0A
```

每秒发送一次，保持连接。

---

## 安全机制

### 三级电量保护

| 电量 | 动作 |
|------|------|
| ≤ 20% | 自动返航 |
| ≤ 10% | 强制降落 |

### GPS 保护

| 条件 | 动作 |
|------|------|
| 卫星数 < 6 颗 | 强制悬停 |

### 高度保护

| 条件 | 动作 |
|------|------|
| 高度 > 150m | 自动降至 120m |

### 电子围栏

| 参数 | 默认值 |
|------|------|
| 纬度范围 | 20.00 ~ 21.00 |
| 经度范围 | 110.00 ~ 111.00 |
| 限高 | 150m |

超出围栏自动悬停，超限高自动降至 120m。

> ⚠️ 围栏参数目前硬编码在 `DroneFenceServiceImpl.java` 中，生产环境应改为外部配置。

---

## 部署教程

### 方式一：本地开发运行

```bash
# 克隆项目
git clone <仓库地址>
cd drone-system

# 编译运行
mvnw.cmd spring-boot:run

# 浏览器打开
# http://localhost:8080
```

### 方式二：打包 JAR 部署

```bash
# 打包
mvnw.cmd clean package -DskipTests

# 运行（target 目录下生成 jar 文件）
java -jar target/drone-system-0.0.1-SNAPSHOT.jar

# 自定义端口
java -jar target/drone-system-0.0.1-SNAPSHOT.jar --server.port=9090
```

### 方式三：Linux 服务器部署

```bash
# 1. 上传 jar 到服务器
scp target/drone-system-0.0.1-SNAPSHOT.jar user@server:/opt/drone/

# 2. SSH 登录服务器
ssh user@server

# 3. 后台运行
nohup java -jar /opt/drone/drone-system-0.0.1-SNAPSHOT.jar \
  --server.port=8080 \
  > /opt/drone/log.txt 2>&1 &

# 4. 查看日志
tail -f /opt/drone/log.txt

# 5. 停止服务
kill $(pgrep -f drone-system)
```

### 方式四：Docker 部署

**创建 Dockerfile：**

```dockerfile
FROM openjdk:8-jre-slim
WORKDIR /app
COPY target/drone-system-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**构建并运行：**

```bash
# 打包
mvnw.cmd clean package -DskipTests

# 构建镜像
docker build -t drone-system .

# 运行容器
docker run -d -p 8080:8080 --name drone drone-system

# 查看日志
docker logs -f drone
```

### 方式五：IDEA 中运行

1. 用 IDEA 打开项目
2. 等待 Maven 依赖下载完成
3. 找到 `DroneSystemApplication.java`
4. 右键 → Run

### 生产环境配置

在 `application.properties` 中添加：

```properties
# 切换为 MySQL 数据库
spring.datasource.url=jdbc:mysql://localhost:3306/drone?useSSL=false&serverTimezone=Asia/Shanghai
spring.datasource.username=root
spring.datasource.password=your_password
spring.datasource.driverClassName=com.mysql.cj.jdbc.Driver
spring.jpa.database-platform=org.hibernate.dialect.MySQL8Dialect

# 端口
server.port=8080

# 关闭 H2 控制台
spring.h2.console.enabled=false
```

需额外添加 MySQL 驱动依赖：

```xml
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <version>8.0.33</version>
</dependency>
```

---

## 常见问题

### Q: 编译报错 "The version cannot be empty"
**A:** Lombok 版本未指定，确认 `pom.xml` 中有：
```xml
<properties>
    <lombok.version>1.18.30</lombok.version>
</properties>
```

### Q: 端口 8080 被占用
**A:** 修改端口：
```bash
java -jar app.jar --server.port=9090
```
或在 `application.properties` 中：
```properties
server.port=9090
```

### Q: 前端页面打不开
**A:** 确认：
1. 应用已启动（看到 `Started DroneSystemApplication`）
2. 访问地址是 `http://localhost:8080`（不是 `https`）
3. 浏览器控制台无报错

### Q: WebSocket 连接失败
**A:** 确认：
1. 页面地址是 `http://localhost:8080`（同源）
2. 没有代理拦截 WebSocket 请求
3. 浏览器支持 WebSocket

### Q: 串口连接失败
**A:** 
1. 确认无人机已通过 USB/数传连接电脑
2. 设备管理器中能看到串口（Windows 为 COM 口）
3. 串口未被其他程序占用
4. Linux 下当前用户有串口权限：`sudo usermod -aG dialout $USER`

### Q: Java 版本不对
**A:** 项目需要 JDK 1.8 及以上。检查版本：
```bash
java -version
```
如需切换版本，修改 `JAVA_HOME` 环境变量。

---

## 项目结构

```
drone-system/
├── pom.xml                              # Maven 构建配置
├── mvnw / mvnw.cmd                      # Maven Wrapper
├── README.md                            # 项目说明
├── HELP.md                              # Spring Boot 参考文档
└── src/
    ├── main/
    │   ├── java/com/drone/dronesystem/
    │   │   ├── DroneSystemApplication.java    # 启动类
    │   │   ├── common/                        # 公共模块
    │   │   │   ├── Result.java                #   统一返回格式
    │   │   │   ├── DroneConstant.java         #   常量定义
    │   │   │   ├── DroneException.java        #   业务异常
    │   │   │   ├── DroneStatus.java           #   状态枚举
    │   │   │   └── GlobalExceptionHandler.java#   全局异常处理
    │   │   ├── config/                        # 配置
    │   │   │   └── WebSocketConfig.java       #   WebSocket 配置
    │   │   ├── constant/                      # 常量
    │   │   │   ├── DroneSafetyConstant.java   #   安全参数
    │   │   │   ├── DroneState.java            #   飞行状态枚举
    │   │   │   ├── RemoteConstant.java        #   远程控制参数
    │   │   │   ├── TaskType.java              #   任务类型枚举
    │   │   │   └── VideoConstant.java         #   视频参数
    │   │   ├── controller/                    # 控制器 (8个)
    │   │   │   ├── DroneMasterController.java #   真机主控 API
    │   │   │   ├── DroneFlyController.java    #   飞行指令 API
    │   │   │   ├── DroneMapController.java    #   地图 API
    │   │   │   ├── DroneMissionController.java#   航线任务 API
    │   │   │   ├── DroneTaskController.java   #   任务队列 API
    │   │   │   ├── DroneVideoController.java  #   视频图传 API
    │   │   │   └── DroneRemoteController.java #   远程控制 API
    │   │   ├── entity/                        # 实体 (6个)
    │   │   │   ├── DroneRealData.java         #   实时数据
    │   │   │   ├── DroneRawData.java          #   原始帧数据
    │   │   │   ├── DroneFlightLog.java        #   飞行日志 (JPA)
    │   │   │   ├── DroneLocation.java         #   GPS 位置
    │   │   │   ├── DroneTask.java             #   任务
    │   │   │   └── DroneWaypoint.java         #   航点
    │   │   ├── protocol/                      # 通信协议
    │   │   │   ├── SerialPortUtil.java        #   串口工具
    │   │   │   ├── DroneParser.java           #   协议解析器
    │   │   │   ├── DroneSender.java           #   指令发送器
    │   │   │   ├── DroneCmd.java              #   指令枚举
    │   │   │   ├── DroneHeartbeat.java        #   心跳包
    │   │   │   └── MavLinkUtil.java           #   MAVLink (待实现)
    │   │   ├── repository/                    # 数据访问
    │   │   │   └── DroneFlightLogRepository.java
    │   │   └── service/                       # 服务层
    │   │       ├── DroneWebsocketService.java #   WebSocket 端点
    │   │       ├── impl/                      #   服务实现 (10个)
    │   │       │   ├── DroneRealServiceImpl.java
    │   │       │   ├── DroneFlyServiceImpl.java
    │   │       │   ├── DroneSafetyServiceImpl.java
    │   │       │   ├── DroneFenceServiceImpl.java
    │   │       │   ├── DroneMapServiceImpl.java
    │   │       │   ├── DroneWaypointServiceImpl.java
    │   │       │   ├── DroneTaskServiceImpl.java
    │   │       │   ├── DroneVideoServiceImpl.java
    │   │       │   ├── DroneRemoteServiceImpl.java
    │   │       │   └── DroneLogServiceImpl.java
    │   │       └── (10个接口文件)
    │   └── resources/
    │       ├── application.properties         # 应用配置
    │       └── static/
    │           └── index.html                 # 前端地面站页面
    └── test/
        └── java/com/drone/dronesystem/
            └── DroneSystemApplicationTests.java
```

---

## 开发计划

- [ ] MAVLink 协议完整对接（Pixhawk / ArduPilot）
- [ ] 围栏参数外部配置化
- [ ] 用户认证与权限控制
- [ ] MySQL / PostgreSQL 生产数据库支持
- [ ] 前端升级为 Vue/React 独立项目
- [ ] 视频流接入（RTSP → WebSocket 推流）
- [ ] 多无人机管理
- [ ] 飞行数据可视化分析

---

## 许可证

本项目仅供学习和研究使用。
