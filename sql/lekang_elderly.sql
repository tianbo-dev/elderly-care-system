-- ============================================================
-- 乐康养老系统 - 数据库建表脚本
-- 数据库: MySQL 8.0+
-- 字符集: utf8mb4
-- ============================================================

CREATE DATABASE IF NOT EXISTS `lekang_elderly` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `lekang_elderly`;

-- ============================================================
-- 1. 用户表 user
-- 用途: 存储登录账号（管理员 / 家属 / 老人）
-- 设计原因: 登录注册功能的核心表，不同角色用 role 字段区分
-- ============================================================
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `username`    VARCHAR(50)  NOT NULL                COMMENT '登录账号',
    `password`    VARCHAR(100) NOT NULL                COMMENT '密码(BCrypt加密)',
    `phone`       VARCHAR(20)  DEFAULT NULL            COMMENT '手机号',
    `role`        VARCHAR(20)  NOT NULL DEFAULT 'FAMILY' COMMENT '角色: ADMIN管理员/FAMILY家属/ELDER老人',
    `status`      TINYINT      NOT NULL DEFAULT 1      COMMENT '状态: 1正常 0禁用',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ============================================================
-- 2. 老人入住登记表 resident
-- 用途: 存储老人信息 + 7步入住流程数据
-- 设计原因: 入住登记管理CRUD的核心表，从UI截图中提取了基本信息、家属信息、资料上传等字段
-- ============================================================
DROP TABLE IF EXISTS `resident`;
CREATE TABLE `resident` (
    `id`              BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`         BIGINT        DEFAULT NULL             COMMENT '关联user表ID(老人登录账号)',
    -- 基本信息
    `name`            VARCHAR(50)   NOT NULL                 COMMENT '姓名',
    `gender`          TINYINT       NOT NULL                 COMMENT '性别: 1男 2女',
    `age`             INT           NOT NULL                 COMMENT '年龄',
    `id_card`         VARCHAR(20)   DEFAULT NULL             COMMENT '身份证号',
    `birthday`        DATE          DEFAULT NULL             COMMENT '出生日期',
    -- 住宿信息
    `room_no`         VARCHAR(20)   DEFAULT NULL             COMMENT '房间号',
    `bed_no`          VARCHAR(20)   DEFAULT NULL             COMMENT '床位号',
    `check_in_date`   DATE          DEFAULT NULL             COMMENT '入住日期',
    -- 健康信息
    `health_status`   VARCHAR(200)  DEFAULT NULL             COMMENT '健康状况/慢性病',
    -- 家属信息
    `guardian_name`   VARCHAR(50)   DEFAULT NULL             COMMENT '监护人姓名',
    `guardian_phone`  VARCHAR(20)   DEFAULT NULL             COMMENT '监护人电话',
    `guardian_rel`    VARCHAR(20)   DEFAULT NULL             COMMENT '与老人关系',
    -- 资料上传 (UI截图中的三个上传项)
    `photo_one_inch`  VARCHAR(255)  DEFAULT NULL             COMMENT '一寸照片URL',
    `id_card_front`   VARCHAR(255)  DEFAULT NULL             COMMENT '身份证人像面URL',
    `id_card_back`    VARCHAR(255)  DEFAULT NULL             COMMENT '身份证国徽面URL',
    -- 入住流程状态 (对应UI中的7步)
    `check_status`    TINYINT       NOT NULL DEFAULT 0       COMMENT '入住流程: 0未开始 1申请 2评估 3审核 4配置 5签约 6缴费 7完成',
    `resident_status` TINYINT       NOT NULL DEFAULT 1       COMMENT '老人状态: 1在住 0已退住',
    -- 时间戳
    `create_time`     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_name` (`name`),
    KEY `idx_room_no` (`room_no`),
    KEY `idx_guardian_phone` (`guardian_phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='老人入住登记表';

-- ============================================================
-- 3. 硬件设备表 device
-- 用途: 存储老人佩戴的跌倒监测设备
-- 设计原因: MQTT消息需要知道是哪个设备发出的，关联到具体老人
-- ============================================================
DROP TABLE IF EXISTS `device`;
CREATE TABLE `device` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `device_id`   VARCHAR(50)  NOT NULL                COMMENT '设备唯一编号(上报消息用)',
    `device_name` VARCHAR(100) DEFAULT NULL            COMMENT '设备名称',
    `device_type` VARCHAR(20)  NOT NULL DEFAULT 'FALL' COMMENT '设备类型: FALL跌倒监测',
    `resident_id` BIGINT       DEFAULT NULL            COMMENT '绑定的老人ID',
    `status`      TINYINT      NOT NULL DEFAULT 1      COMMENT '状态: 1在线 0离线',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_device_id` (`device_id`),
    KEY `idx_resident_id` (`resident_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='硬件设备表';

-- ============================================================
-- 4. 告警记录表 alarm
-- 用途: 存储跌倒告警事件
-- 设计原因: MQTT收到消息后写入此表，Web后台展示告警列表并处理
-- ============================================================
DROP TABLE IF EXISTS `alarm`;
CREATE TABLE `alarm` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `device_id`    VARCHAR(50)  NOT NULL                COMMENT '来源设备编号',
    `resident_id`  BIGINT       DEFAULT NULL            COMMENT '关联老人ID',
    `resident_name` VARCHAR(50) DEFAULT NULL            COMMENT '冗余: 老人姓名(方便展示)',
    `alarm_type`   VARCHAR(20)  NOT NULL DEFAULT 'FALL' COMMENT '告警类型: FALL跌倒',
    `alarm_time`   DATETIME     NOT NULL                COMMENT '告警时间',
    `status`       TINYINT      NOT NULL DEFAULT 0      COMMENT '处理状态: 0待处理 1已处理',
    `handle_time`  DATETIME     DEFAULT NULL            COMMENT '处理时间',
    `handle_by`    VARCHAR(50)  DEFAULT NULL            COMMENT '处理人',
    `handle_remark` VARCHAR(500) DEFAULT NULL            COMMENT '处理备注',
    `create_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_resident_id` (`resident_id`),
    KEY `idx_alarm_time` (`alarm_time`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='告警记录表';

-- ============================================================
-- 5. 账单表 bill
-- 用途: 存储老人入住缴费账单
-- 设计原因: UI截图中首期缴费步骤展示了账单信息，需要持久化
-- ============================================================
DROP TABLE IF EXISTS `bill`;
CREATE TABLE `bill` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `bill_no`      VARCHAR(30)  NOT NULL                COMMENT '账单编号',
    `resident_id`  BIGINT       NOT NULL                COMMENT '关联老人ID',
    `amount`       DECIMAL(12,2) NOT NULL               COMMENT '账单金额',
    `bill_period`  VARCHAR(50)  DEFAULT NULL            COMMENT '账单周期(如 2024-10-10 ~ 2024-11-10)',
    `bill_type`    VARCHAR(20)  NOT NULL DEFAULT 'FIRST' COMMENT '账单类型: FIRST首期 MONTHLY月费',
    `pay_status`   TINYINT      NOT NULL DEFAULT 0      COMMENT '支付状态: 0待支付 1已支付',
    `pay_method`   VARCHAR(20)  DEFAULT NULL            COMMENT '支付方式: WECHAT微信 ALIPAY支付宝 OFFLINE线下',
    `pay_time`     DATETIME     DEFAULT NULL            COMMENT '支付时间',
    `create_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_bill_no` (`bill_no`),
    KEY `idx_resident_id` (`resident_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='账单表';

-- ============================================================
-- 初始数据 (密码统一为 123456, BCrypt加密)
-- ============================================================

-- 管理员账号
INSERT INTO `user` (`username`, `password`, `phone`, `role`, `status`) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '13800138000', 'ADMIN', 1);

-- 示例家属账号
INSERT INTO `user` (`username`, `password`, `phone`, `role`, `status`) VALUES
('family01', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '13900139001', 'FAMILY', 1);

-- 示例老人入住记录
INSERT INTO `resident` (`name`, `gender`, `age`, `id_card`, `birthday`, `room_no`, `bed_no`, `check_in_date`, `health_status`, `guardian_name`, `guardian_phone`, `guardian_rel`, `check_status`, `resident_status`) VALUES
('张建国', 1, 78, '110101194601011234', '1946-01-01', 'A栋-301', '1号床', '2024-09-01', '高血压、糖尿病', '张伟', '13900139001', '儿子', 7, 1),
('李秀兰', 2, 82, '110101194205054321', '1942-05-05', 'B栋-205', '2号床', '2024-08-15', '心脏病', '李娜', '13900139002', '女儿', 7, 1),
('王德福', 1, 75, '110101194903035678', '1949-03-03', 'A栋-402', '1号床', '2024-09-10', '腿脚不便', '王芳', '13900139003', '女儿', 4, 1);

-- 示例设备
INSERT INTO `device` (`device_id`, `device_name`, `device_type`, `resident_id`, `status`) VALUES
('FALL-001', '跌倒监测手环001', 'FALL', 1, 1),
('FALL-002', '跌倒监测手环002', 'FALL', 2, 1),
('FALL-003', '跌倒监测手环003', 'FALL', 3, 0);

-- 示例告警记录
INSERT INTO `alarm` (`device_id`, `resident_id`, `resident_name`, `alarm_type`, `alarm_time`, `status`, `handle_time`, `handle_by`, `handle_remark`) VALUES
('FALL-001', 1, '张建国', 'FALL', '2024-09-15 14:30:00', 1, '2024-09-15 14:45:00', 'admin', '老人轻微摔倒，已检查无碍');

-- 示例账单
INSERT INTO `bill` (`bill_no`, `resident_id`, `amount`, `bill_period`, `bill_type`, `pay_status`, `pay_method`, `pay_time`) VALUES
('ZD20241015000001', 1, 50000.00, '2024-10-10 ~ 2024-11-10', 'FIRST', 1, 'WECHAT', '2024-09-05 10:30:00'),
('ZD20241015000002', 2, 50000.00, '2024-09-15 ~ 2024-10-15', 'FIRST', 1, 'OFFLINE', '2024-08-20 15:00:00');
