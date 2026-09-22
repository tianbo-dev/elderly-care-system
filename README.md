# 乐康养老系统

养老院管理系统，实现登录注册、入住登记管理、跌倒告警监测三大功能。

## 快速启动

```bash
# 1. 初始化数据库
mysql -u root -p < sql/lekang_elderly.sql

# 2. 启动后端
cd elderly-care-backend
mvn spring-boot:run          # http://localhost:8080/api

# 3. 启动 Web 前端
cd elderly-care-web
npm install && npm run dev   # http://localhost:5173

# 4. 小程序（HBuilderX 打开 elderly-care-mp 目录运行）

# 5. MQTTX 设备模拟（连接 broker.emqx.io:1883）
```

## 测试账号

| 角色 | 账号 | 密码 |
|------|------|------|
| 管理员 | admin | 123456 |
| 家属 | family01 | 123456 |

## 技术栈

- 后端：Spring Boot 3.2 + MyBatis-Plus + Spring Security + JWT（Java 17）
- Web 前端：Vue 3 + Vite + Element Plus
- 小程序：uni-app（构建为微信小程序 mp-weixin）
- 数据库：MySQL 8.x
- 设备模拟：MQTTX / Node.js 脚本 + EMQX 公共 Broker

## 配置说明（环境变量）

后端配置位于 `elderly-care-backend/src/main/resources/application.yml`，所有环境相关项均支持通过环境变量覆盖，不配置时使用本地开发默认值，开箱即跑：

| 环境变量 | 默认值 | 说明 |
|----------|--------|------|
| `DB_URL` | jdbc:mysql://localhost:3306/lekang_elderly?... | MySQL 连接地址 |
| `DB_USERNAME` | root | 数据库账号 |
| `DB_PASSWORD` | root | 数据库密码 |
| `JWT_SECRET` | dev-only-...（仅本地调试用） | JWT 签名密钥，HS256 要求 ≥ 32 字节，生产必须更换 |
| `MQTT_BROKER_URL` | tcp://broker.emqx.io:1883 | MQTT Broker 地址 |
| `MQTT_USERNAME` / `MQTT_PASSWORD` | public / public | Broker 凭证 |
| `MQTT_TOPIC` | device/fall/# | 订阅主题（# 为多层通配符） |

> 小程序端后端地址在 `elderly-care-mp/App.vue` 的 `globalData.baseUrl` 配置；微信开发者工具中需在详情里勾选“不校验合法域名”，真机预览改为电脑局域网 IP。

## 项目文档

- [01-需求分析与业务拆解](docs/01-需求分析与业务拆解.md)
- [02-数据库设计说明](docs/02-数据库设计说明.md)
- [03-系统架构说明](docs/03-系统架构说明.md)
- [04-接口文档](docs/04-接口文档.md)
- [05-部署与验收说明](docs/05-部署与验收说明.md)
