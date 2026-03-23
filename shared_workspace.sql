/*
 Navicat Premium Dump SQL

 Source Server         : wwb
 Source Server Type    : MySQL
 Source Server Version : 80026 (8.0.26)
 Source Host           : localhost:3306
 Source Schema         : shared_workspace

 Target Server Type    : MySQL
 Target Server Version : 80026 (8.0.26)
 File Encoding         : 65001

 Date: 23/03/2026 17:59:25
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for biz_credit_log
-- ----------------------------
DROP TABLE IF EXISTS `biz_credit_log`;
CREATE TABLE `biz_credit_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `order_id` bigint NULL DEFAULT NULL COMMENT '关联的订单ID（如果有）',
  `change_type` tinyint NOT NULL COMMENT '变更类型：1-增加，2-扣除',
  `score` int NOT NULL COMMENT '变更分值（绝对值）',
  `reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '变更原因（如：预约违约未签到扣分）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 500 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '信用积分变更明细表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of biz_credit_log
-- ----------------------------
INSERT INTO `biz_credit_log` VALUES (1, 3, NULL, 1, 1, '按时签到奖励', '2026-03-03 15:47:36');
INSERT INTO `biz_credit_log` VALUES (2, 3, NULL, 1, 2, '主动提前结束使用奖励', '2026-03-03 15:47:36');
INSERT INTO `biz_credit_log` VALUES (3, 4, NULL, 2, 10, '预约时段内未签到，判定违约', '2026-02-28 15:47:36');
INSERT INTO `biz_credit_log` VALUES (4, 5, NULL, 1, 1, '按时签到奖励', '2026-03-01 15:47:36');
INSERT INTO `biz_credit_log` VALUES (5, 7, NULL, 1, 1, '按时签到奖励', '2026-03-03 15:47:36');
INSERT INTO `biz_credit_log` VALUES (6, 11, NULL, 2, 10, '预约时段内未签到，判定违约', '2026-03-07 15:47:36');
INSERT INTO `biz_credit_log` VALUES (7, 11, NULL, 2, 5, '距开始不足1小时取消预约', '2026-03-19 15:47:36');
INSERT INTO `biz_credit_log` VALUES (8, 7, NULL, 2, 10, '预约时段内未签到，判定违约', '2026-03-15 15:47:36');
INSERT INTO `biz_credit_log` VALUES (9, 10, NULL, 2, 10, '预约时段内未签到，判定违约', '2026-03-22 15:47:36');
INSERT INTO `biz_credit_log` VALUES (10, 3, NULL, 1, 1, '按时签到奖励', '2026-03-11 15:47:36');
INSERT INTO `biz_credit_log` VALUES (11, 8, NULL, 1, 2, '主动提前结束使用奖励', '2026-03-16 15:47:36');
INSERT INTO `biz_credit_log` VALUES (12, 12, NULL, 1, 1, '按时签到奖励', '2026-03-20 15:47:36');
INSERT INTO `biz_credit_log` VALUES (13, 6, NULL, 2, 10, '预约时段内未签到，判定违约', '2026-02-22 15:47:36');

-- ----------------------------
-- Table structure for biz_facility
-- ----------------------------
DROP TABLE IF EXISTS `biz_facility`;
CREATE TABLE `biz_facility`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '设施名称（如：双屏、升降桌）',
  `icon` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '设施图标（前端Icon名或URL）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 100 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '工位配套设施字典表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of biz_facility
-- ----------------------------
INSERT INTO `biz_facility` VALUES (1, '双屏显示器', 'monitor', '2025-05-08 09:00:00', '2025-05-08 09:00:00', 0);
INSERT INTO `biz_facility` VALUES (2, '电动升降桌', 'desk', '2025-05-08 09:00:00', '2025-05-08 09:00:00', 0);
INSERT INTO `biz_facility` VALUES (3, '人体工学椅', 'chair', '2025-05-08 09:00:00', '2025-05-08 09:00:00', 0);
INSERT INTO `biz_facility` VALUES (4, 'Type-C 扩展坞', 'hub', '2025-05-08 09:00:00', '2025-05-08 09:00:00', 0);
INSERT INTO `biz_facility` VALUES (5, '千兆有线网口', 'ethernet', '2025-05-08 09:00:00', '2025-05-08 09:00:00', 0);
INSERT INTO `biz_facility` VALUES (6, '降噪耳机借用', 'headset', '2025-06-01 10:00:00', '2025-06-01 10:00:00', 0);

-- ----------------------------
-- Table structure for biz_feedback
-- ----------------------------
DROP TABLE IF EXISTS `biz_feedback`;
CREATE TABLE `biz_feedback`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '反馈内容',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '0待处理 1已处理',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 100 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户意见反馈' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of biz_feedback
-- ----------------------------
INSERT INTO `biz_feedback` VALUES (1, 5, '希望大厅地图支持手势旋转，有时横屏看图更方便。', 0, '2026-03-05 15:47:36', '2026-03-05 15:47:36', 0);
INSERT INTO `biz_feedback` VALUES (2, 9, '建议增加「常用工位」收藏，减少每次筛选时间。', 1, '2026-03-13 15:47:36', '2026-03-14 15:47:36', 0);
INSERT INTO `biz_feedback` VALUES (3, 10, '验证码偶尔延迟，能否支持语音验证码？', 0, '2026-03-01 15:47:36', '2026-03-01 15:47:36', 0);
INSERT INTO `biz_feedback` VALUES (4, 3, '静音区空调略冷，能否分区控温。', 0, '2026-03-11 15:47:36', '2026-03-11 15:47:36', 0);
INSERT INTO `biz_feedback` VALUES (5, 14, '订单列表里想直接看到楼层信息，现在很清晰了，点赞。', 1, '2026-03-21 15:47:36', '2026-03-21 15:47:36', 0);

-- ----------------------------
-- Table structure for biz_message
-- ----------------------------
DROP TABLE IF EXISTS `biz_message`;
CREATE TABLE `biz_message`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '接收用户ID',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '消息标题',
  `content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '消息内容',
  `is_read` tinyint(1) NOT NULL DEFAULT 0 COMMENT '阅读状态：0-未读，1-已读',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建/发送时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id_read`(`user_id` ASC, `is_read` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 501 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '站内消息通知表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of biz_message
-- ----------------------------
INSERT INTO `biz_message` VALUES (1, 3, '预约成功', '您已成功预约工位，请按时签到。', 1, '2026-02-26 15:47:36', 0);
INSERT INTO `biz_message` VALUES (2, 4, '签到提醒', '您预约的工位即将开始，请提前前往签到。', 1, '2026-02-28 15:47:36', 0);
INSERT INTO `biz_message` VALUES (3, 4, '违约通知', '订单因未签到已标记为违约，信用分已按规定扣减。', 1, '2026-02-28 15:47:36', 0);
INSERT INTO `biz_message` VALUES (4, 11, '信用预警', '您的信用分已低于常见阈值，请规范使用预约功能。', 1, '2026-03-05 15:47:36', 0);
INSERT INTO `biz_message` VALUES (5, 12, '预约成功', '您已成功预约 A3静-05，时段请见订单详情。', 1, '2026-03-08 15:47:36', 0);
INSERT INTO `biz_message` VALUES (6, 13, '系统维护通知', '本周日凌晨 2:00-4:00 将进行网络维护，期间可能影响签到。', 0, '2026-03-18 15:47:36', 0);
INSERT INTO `biz_message` VALUES (7, 10, '新功能上线', '消息中心支持关键词筛选，欢迎体验。', 0, '2026-03-20 15:47:36', 0);
INSERT INTO `biz_message` VALUES (8, 5, '预约取消确认', '您的预约已取消，工位已释放。', 1, '2026-03-02 15:47:36', 0);
INSERT INTO `biz_message` VALUES (9, 7, '违约通知', '订单因未签到已标记为违约。', 1, '2026-02-22 15:47:36', 0);
INSERT INTO `biz_message` VALUES (10, 6, '预约成功', '您已成功预约 B2岛-03。', 1, '2026-03-14 15:47:36', 0);
INSERT INTO `biz_message` VALUES (11, 14, '预约即将开始', '您有一条明日预约，请留意签到时间窗。', 0, '2026-03-22 15:47:36', 0);
INSERT INTO `biz_message` VALUES (12, 8, '信用奖励', '因主动提前结束使用，信用分已奖励 +2。', 1, '2026-03-16 15:47:36', 0);
INSERT INTO `biz_message` VALUES (500, 10, '预约已取消', '管理员已强制取消您的预约。', 0, '2026-03-23 17:27:00', 0);

-- ----------------------------
-- Table structure for biz_order
-- ----------------------------
DROP TABLE IF EXISTS `biz_order`;
CREATE TABLE `biz_order`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '订单流水号',
  `user_id` bigint NOT NULL COMMENT '预约用户ID',
  `workstation_id` bigint NOT NULL COMMENT '预约工位ID',
  `start_time` datetime NOT NULL COMMENT '预约开始时间',
  `end_time` datetime NOT NULL COMMENT '预约结束时间',
  `sign_time` datetime NULL DEFAULT NULL COMMENT '实际签到时间',
  `actual_end_time` datetime NULL DEFAULT NULL COMMENT '实际结束/提前退座时间',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0-待签到，1-进行中，2-已完成，3-已取消，4-已违约',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '订单创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_order_no`(`order_no` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_workstation_time`(`workstation_id` ASC, `start_time` ASC, `end_time` ASC) USING BTREE COMMENT '用于快速查询某工位特定时间段是否被占用'
) ENGINE = InnoDB AUTO_INCREMENT = 200 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '工位预约订单表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of biz_order
-- ----------------------------
INSERT INTO `biz_order` VALUES (1, 'b32d7d5c1c6520e8b80af1ee455440c9', 3, 1, '2026-02-26 09:00:00', '2026-02-26 18:00:00', '2026-02-26 08:52:00', '2026-02-26 17:40:00', 2, '2026-02-25 15:47:36', '2026-02-26 15:47:36', 0);
INSERT INTO `biz_order` VALUES (2, 'd61f0a3f3a77dc985c7ff636fa7c539b', 3, 2, '2026-02-27 10:00:00', '2026-02-27 16:00:00', '2026-02-27 09:58:00', '2026-02-27 15:55:00', 2, '2026-02-26 15:47:36', '2026-02-27 15:47:36', 0);
INSERT INTO `biz_order` VALUES (3, 'd048e905da692ebeb811307a643395bf', 4, 3, '2026-02-28 09:00:00', '2026-02-28 12:00:00', NULL, NULL, 4, '2026-02-27 15:47:36', '2026-02-28 15:47:36', 0);
INSERT INTO `biz_order` VALUES (4, 'd6fffa443c2cf186ce219e27e610726c', 5, 4, '2026-03-01 13:00:00', '2026-03-01 18:00:00', '2026-03-01 12:55:00', '2026-03-01 18:00:00', 2, '2026-02-28 15:47:36', '2026-03-01 15:47:36', 0);
INSERT INTO `biz_order` VALUES (5, 'b1825f972ac1a08b824440b14509b1d3', 6, 5, '2026-03-02 09:00:00', '2026-03-02 18:00:00', NULL, NULL, 3, '2026-03-01 15:47:36', '2026-03-02 15:47:36', 0);
INSERT INTO `biz_order` VALUES (6, 'f0a9dc6baad74f09749ace285ac26dcb', 7, 6, '2026-03-03 10:00:00', '2026-03-03 17:00:00', '2026-03-03 09:48:00', '2026-03-03 16:20:00', 2, '2026-03-02 15:47:36', '2026-03-03 15:47:36', 0);
INSERT INTO `biz_order` VALUES (7, 'f732a2fb28ade3cc06fbc3da92d98644', 8, 8, '2026-03-04 09:00:00', '2026-03-04 12:00:00', '2026-03-04 08:50:00', '2026-03-04 11:58:00', 2, '2026-03-03 15:47:36', '2026-03-04 15:47:36', 0);
INSERT INTO `biz_order` VALUES (8, 'f79a7caa23d54303ce9c2f386cb90a98', 9, 9, '2026-03-05 14:00:00', '2026-03-05 19:00:00', '2026-03-05 13:55:00', '2026-03-05 18:50:00', 2, '2026-03-04 15:47:36', '2026-03-05 15:47:36', 0);
INSERT INTO `biz_order` VALUES (9, '022b578247e3833968e875b63f631273', 10, 10, '2026-03-06 09:00:00', '2026-03-06 18:00:00', '2026-03-06 08:45:00', '2026-03-06 17:10:00', 2, '2026-03-05 15:47:36', '2026-03-06 15:47:36', 0);
INSERT INTO `biz_order` VALUES (10, '1ac68058174ea9f24da6cc810d6d680b', 11, 11, '2026-03-07 10:00:00', '2026-03-07 15:00:00', NULL, NULL, 4, '2026-03-06 15:47:36', '2026-03-07 15:47:36', 0);
INSERT INTO `biz_order` VALUES (11, 'bb2185d9856fdd756cd747958117f6fa', 12, 13, '2026-03-08 09:00:00', '2026-03-08 18:00:00', '2026-03-08 08:58:00', '2026-03-08 17:05:00', 2, '2026-03-07 15:47:36', '2026-03-08 15:47:36', 0);
INSERT INTO `biz_order` VALUES (12, '421543df0618cb81326182f25d168ee0', 13, 14, '2026-03-09 13:00:00', '2026-03-09 17:00:00', '2026-03-09 12:50:00', '2026-03-09 16:58:00', 2, '2026-03-08 15:47:36', '2026-03-09 15:47:36', 0);
INSERT INTO `biz_order` VALUES (13, '5f4f21bfc2c7f472213e38018ab16aad', 14, 15, '2026-03-10 09:00:00', '2026-03-10 12:00:00', NULL, NULL, 3, '2026-03-09 15:47:36', '2026-03-10 15:47:36', 0);
INSERT INTO `biz_order` VALUES (14, 'ca81d245e566251cabc504bfa1cfda13', 3, 16, '2026-03-11 09:00:00', '2026-03-11 18:00:00', '2026-03-11 08:55:00', '2026-03-11 17:30:00', 2, '2026-03-10 15:47:36', '2026-03-11 15:47:36', 0);
INSERT INTO `biz_order` VALUES (15, '4308933c9dcb11763ef9e1b464f08e95', 4, 17, '2026-03-12 10:00:00', '2026-03-12 16:00:00', '2026-03-12 09:52:00', '2026-03-12 15:45:00', 2, '2026-03-11 15:47:36', '2026-03-12 15:47:36', 0);
INSERT INTO `biz_order` VALUES (16, 'f6af9163e3be8a71a181981465ed9302', 5, 18, '2026-03-13 09:00:00', '2026-03-13 13:00:00', '2026-03-13 08:48:00', '2026-03-13 12:55:00', 2, '2026-03-12 15:47:36', '2026-03-13 15:47:36', 0);
INSERT INTO `biz_order` VALUES (17, 'a3f0530c3d373a39717096e882b56a72', 6, 19, '2026-03-14 14:00:00', '2026-03-14 19:00:00', '2026-03-14 13:50:00', '2026-03-14 18:40:00', 2, '2026-03-13 15:47:36', '2026-03-14 15:47:36', 0);
INSERT INTO `biz_order` VALUES (18, 'eca8ac3f6da65cb44350ccd350dfc063', 7, 21, '2026-03-15 09:00:00', '2026-03-15 18:00:00', NULL, NULL, 4, '2026-03-14 15:47:36', '2026-03-15 15:47:36', 0);
INSERT INTO `biz_order` VALUES (19, '1a631ed55e4163d31af64bc3d46adf39', 8, 22, '2026-03-16 09:00:00', '2026-03-16 12:00:00', '2026-03-16 08:55:00', '2026-03-16 11:50:00', 2, '2026-03-15 15:47:36', '2026-03-16 15:47:36', 0);
INSERT INTO `biz_order` VALUES (20, '55978e3e79d0f02aa100deb93cd1f05b', 9, 23, '2026-03-17 10:00:00', '2026-03-17 17:00:00', '2026-03-17 09:58:00', '2026-03-17 16:30:00', 2, '2026-03-16 15:47:36', '2026-03-17 15:47:36', 0);
INSERT INTO `biz_order` VALUES (21, 'afa0ce8509687f2cac4ba39595fda548', 10, 24, '2026-03-18 13:00:00', '2026-03-18 18:00:00', '2026-03-18 12:52:00', '2026-03-18 17:45:00', 2, '2026-03-17 15:47:36', '2026-03-18 15:47:36', 0);
INSERT INTO `biz_order` VALUES (22, 'f89e31795fb8751ef075a378db6b521a', 11, 1, '2026-03-19 09:00:00', '2026-03-19 18:00:00', NULL, NULL, 3, '2026-03-18 15:47:36', '2026-03-19 15:47:36', 0);
INSERT INTO `biz_order` VALUES (23, '262cdf8c63b1709e566b592e882dc65f', 12, 2, '2026-03-20 09:00:00', '2026-03-20 12:00:00', '2026-03-20 08:50:00', '2026-03-20 11:48:00', 2, '2026-03-19 15:47:36', '2026-03-20 15:47:36', 0);
INSERT INTO `biz_order` VALUES (24, '11629741b17160c5b0433fc09d95414a', 13, 3, '2026-03-21 10:00:00', '2026-03-21 16:00:00', '2026-03-21 09:55:00', '2026-03-21 15:50:00', 2, '2026-03-20 15:47:36', '2026-03-21 15:47:36', 0);
INSERT INTO `biz_order` VALUES (25, '7cdcfab1f839e21926c526cbc914faee', 14, 4, '2026-03-22 09:00:00', '2026-03-22 18:00:00', '2026-03-22 08:52:00', '2026-03-22 17:20:00', 2, '2026-03-21 15:47:36', '2026-03-22 15:47:36', 0);
INSERT INTO `biz_order` VALUES (26, '8a3071bfcda8186a5967d52df0c523c6', 3, 5, '2026-03-23 14:00:00', '2026-03-23 19:00:00', '2026-03-23 13:48:00', '2026-03-23 18:35:00', 2, '2026-03-22 15:47:36', '2026-03-23 15:47:36', 0);
INSERT INTO `biz_order` VALUES (27, 'c02c86bfd21770d1349c668484267300', 4, 6, '2026-03-22 09:00:00', '2026-03-22 11:00:00', NULL, NULL, 3, '2026-03-21 15:47:36', '2026-03-22 15:47:36', 0);
INSERT INTO `biz_order` VALUES (28, '4cedc3199caeb7a443e2f77fe4140293', 5, 8, '2026-03-23 13:00:00', '2026-03-23 18:00:00', '2026-03-23 12:58:00', '2026-03-23 17:55:00', 2, '2026-03-22 15:47:36', '2026-03-23 15:47:36', 0);
INSERT INTO `biz_order` VALUES (29, 'b061bece5ab42e06d86bdd245d61af48', 6, 9, '2026-03-23 09:00:00', '2026-03-23 18:00:00', '2026-03-23 08:45:00', '2026-03-23 17:10:00', 2, '2026-03-22 15:47:36', '2026-03-23 15:47:36', 0);
INSERT INTO `biz_order` VALUES (30, '1d75285258fc04368a749c20c7a39039', 7, 10, '2026-03-22 10:00:00', '2026-03-22 15:00:00', NULL, NULL, 4, '2026-03-21 15:47:36', '2026-03-22 15:47:36', 0);
INSERT INTO `biz_order` VALUES (31, 'ebefbaebbf860417968037f34a1e3e24', 8, 12, '2026-03-23 09:00:00', '2026-03-23 18:00:00', '2026-03-23 08:58:00', '2026-03-23 17:25:00', 2, '2026-03-22 15:47:36', '2026-03-23 15:47:36', 0);
INSERT INTO `biz_order` VALUES (32, '81c5836dbe8809032b15867fd7290d26', 9, 14, '2026-03-23 11:00:00', '2026-03-23 17:00:00', '2026-03-23 10:55:00', '2026-03-23 16:50:00', 2, '2026-03-22 15:47:36', '2026-03-23 15:47:36', 0);
INSERT INTO `biz_order` VALUES (33, 'c21c2c7d4e539d2696750cd330bf0698', 10, 15, '2026-03-23 09:00:00', '2026-03-23 12:00:00', '2026-03-23 08:50:00', NULL, 3, '2026-03-22 15:47:36', '2026-03-23 15:47:36', 0);
INSERT INTO `biz_order` VALUES (34, '5e77f5f005a730915fa289ca4b44bb45', 12, 16, '2026-03-24 14:00:00', '2026-03-24 18:00:00', NULL, NULL, 0, '2026-03-23 15:47:36', '2026-03-23 15:47:36', 0);
INSERT INTO `biz_order` VALUES (35, 'dd4130c58ce8766d12393c7cab98cdb4', 13, 17, '2026-03-25 09:00:00', '2026-03-25 18:00:00', NULL, NULL, 0, '2026-03-22 15:47:36', '2026-03-23 15:47:36', 0);
INSERT INTO `biz_order` VALUES (36, 'c60c2631ef3b3542f7ab26ab115b94cd', 5, 11, '2026-02-24 09:00:00', '2026-02-24 18:00:00', '2026-02-24 08:55:00', '2026-02-24 17:50:00', 2, '2026-02-23 15:47:36', '2026-02-24 15:47:36', 0);
INSERT INTO `biz_order` VALUES (37, 'a8dcb829b9fd09628b729c77eb38d169', 6, 18, '2026-02-23 10:00:00', '2026-02-23 16:00:00', '2026-02-23 09:50:00', '2026-02-23 15:55:00', 2, '2026-02-22 15:47:36', '2026-02-23 15:47:36', 0);
INSERT INTO `biz_order` VALUES (38, '3e90d3f4d641a7f16570e0a358f11b9a', 7, 19, '2026-02-22 13:00:00', '2026-02-22 18:00:00', NULL, NULL, 4, '2026-02-22 15:47:36', '2026-02-22 15:47:36', 0);
INSERT INTO `biz_order` VALUES (39, 'c2f76021d565bcb2c510d12db8aef354', 8, 21, '2026-02-25 09:00:00', '2026-02-25 12:00:00', '2026-02-25 08:48:00', '2026-02-25 11:58:00', 2, '2026-02-24 15:47:36', '2026-02-25 15:47:36', 0);
INSERT INTO `biz_order` VALUES (40, '642ec66486246ae337257f389f48072d', 4, 22, '2026-03-12 09:00:00', '2026-03-12 18:00:00', '2026-03-12 08:52:00', '2026-03-12 17:40:00', 2, '2026-03-11 15:47:36', '2026-03-12 15:47:36', 0);

-- ----------------------------
-- Table structure for biz_space
-- ----------------------------
DROP TABLE IF EXISTS `biz_space`;
CREATE TABLE `biz_space`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `parent_id` bigint NOT NULL DEFAULT 0 COMMENT '父级ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '空间名称（如：A栋、2楼、研发区）',
  `type` tinyint NOT NULL COMMENT '层级类型：1-园区/楼宇，2-楼层，3-办公区域',
  `center_longitude` decimal(10, 6) NULL DEFAULT NULL COMMENT '中心点经度（区域层级使用，用于GEO签到）',
  `center_latitude` decimal(10, 6) NULL DEFAULT NULL COMMENT '中心点纬度（区域层级使用，用于GEO签到）',
  `bg_image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '平面背景底图URL（楼层/区域层级使用）',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_parent_id`(`parent_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 100 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '空间层级字典表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of biz_space
-- ----------------------------
INSERT INTO `biz_space` VALUES (1, 0, '江南创新园', 1, NULL, NULL, NULL, 0, '2025-05-08 09:00:00', '2025-05-08 09:00:00', 0);
INSERT INTO `biz_space` VALUES (2, 1, 'A座研发楼', 2, NULL, NULL, NULL, 0, '2025-05-08 09:05:00', '2025-05-08 09:05:00', 0);
INSERT INTO `biz_space` VALUES (3, 1, 'B座协作中心', 2, NULL, NULL, NULL, 1, '2025-05-10 10:00:00', '2025-05-10 10:00:00', 0);
INSERT INTO `biz_space` VALUES (4, 2, '3层', 3, NULL, NULL, '/uploads/map-a3.png', 0, '2025-05-12 11:00:00', '2026-01-10 12:00:00', 0);
INSERT INTO `biz_space` VALUES (5, 2, '5层', 3, NULL, NULL, '/uploads/map-a5.png', 1, '2025-05-12 11:05:00', '2026-01-10 12:00:00', 0);
INSERT INTO `biz_space` VALUES (6, 3, '2层', 3, NULL, NULL, '/uploads/map-b2.png', 0, '2025-05-15 14:00:00', '2026-02-01 09:00:00', 0);
INSERT INTO `biz_space` VALUES (7, 4, '开放办公东翼', 4, 120.153856, 30.287459, NULL, 0, '2025-05-20 09:00:00', '2025-11-01 10:00:00', 0);
INSERT INTO `biz_space` VALUES (8, 4, '静音专注区', 4, 120.153920, 30.287510, NULL, 1, '2025-05-20 09:10:00', '2025-11-01 10:00:00', 0);
INSERT INTO `biz_space` VALUES (9, 5, '临展机动区', 4, 120.153800, 30.287400, NULL, 0, '2025-05-20 09:20:00', '2025-11-01 10:00:00', 0);
INSERT INTO `biz_space` VALUES (10, 6, '大平层协作岛', 4, 120.154100, 30.287600, NULL, 0, '2025-05-22 10:00:00', '2026-01-15 11:00:00', 0);

-- ----------------------------
-- Table structure for biz_workstation
-- ----------------------------
DROP TABLE IF EXISTS `biz_workstation`;
CREATE TABLE `biz_workstation`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `space_id` bigint NOT NULL COMMENT '所属办公区域ID（关联biz_space）',
  `code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '工位编号（如：A-001）',
  `base_status` tinyint NOT NULL DEFAULT 0 COMMENT '基础状态：0-正常开放，1-维修锁定，2-专属保留',
  `reserved_user_id` bigint NULL DEFAULT NULL COMMENT '专属保留时绑定的用户ID',
  `coord_x` int NULL DEFAULT NULL COMMENT '在底图上的X坐标（用于前端绝对定位）',
  `coord_y` int NULL DEFAULT NULL COMMENT '在底图上的Y坐标（用于前端绝对定位）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_space_id`(`space_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 100 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '工位台账表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of biz_workstation
-- ----------------------------
INSERT INTO `biz_workstation` VALUES (1, 7, 'A3东-01', 0, NULL, 120, 180, '2025-05-21 10:00:00', '2025-05-21 10:00:00', 0);
INSERT INTO `biz_workstation` VALUES (2, 7, 'A3东-02', 0, NULL, 300, 190, '2025-05-21 10:00:00', '2025-05-21 10:00:00', 0);
INSERT INTO `biz_workstation` VALUES (3, 7, 'A3东-03', 0, NULL, 480, 200, '2025-05-21 10:00:00', '2025-05-21 10:00:00', 0);
INSERT INTO `biz_workstation` VALUES (4, 7, 'A3东-04', 0, NULL, 650, 185, '2025-05-21 10:00:00', '2025-05-21 10:00:00', 0);
INSERT INTO `biz_workstation` VALUES (5, 7, 'A3东-05', 0, NULL, 180, 340, '2025-05-21 10:00:00', '2025-05-21 10:00:00', 0);
INSERT INTO `biz_workstation` VALUES (6, 7, 'A3东-06', 0, NULL, 360, 350, '2025-05-21 10:00:00', '2025-05-21 10:00:00', 0);
INSERT INTO `biz_workstation` VALUES (7, 7, 'A3东-07', 1, NULL, 520, 360, '2025-05-21 10:00:00', '2026-02-28 16:00:00', 0);
INSERT INTO `biz_workstation` VALUES (8, 7, 'A3东-08', 0, NULL, 700, 340, '2025-05-21 10:00:00', '2025-05-21 10:00:00', 0);
INSERT INTO `biz_workstation` VALUES (9, 8, 'A3静-01', 0, NULL, 140, 160, '2025-05-21 11:00:00', '2025-05-21 11:00:00', 0);
INSERT INTO `biz_workstation` VALUES (10, 8, 'A3静-02', 0, NULL, 320, 170, '2025-05-21 11:00:00', '2025-05-21 11:00:00', 0);
INSERT INTO `biz_workstation` VALUES (11, 8, 'A3静-03', 0, NULL, 500, 165, '2025-05-21 11:00:00', '2025-05-21 11:00:00', 0);
INSERT INTO `biz_workstation` VALUES (12, 8, 'A3静-04', 0, NULL, 220, 310, '2025-05-21 11:00:00', '2025-05-21 11:00:00', 0);
INSERT INTO `biz_workstation` VALUES (13, 8, 'A3静-05', 0, NULL, 420, 300, '2025-05-21 11:00:00', '2025-05-21 11:00:00', 0);
INSERT INTO `biz_workstation` VALUES (14, 9, 'A5临-01', 0, NULL, 200, 200, '2025-05-25 09:00:00', '2025-05-25 09:00:00', 0);
INSERT INTO `biz_workstation` VALUES (15, 9, 'A5临-02', 0, NULL, 400, 210, '2025-05-25 09:00:00', '2025-05-25 09:00:00', 0);
INSERT INTO `biz_workstation` VALUES (16, 9, 'A5临-03', 0, NULL, 580, 205, '2025-05-25 09:00:00', '2025-05-25 09:00:00', 0);
INSERT INTO `biz_workstation` VALUES (17, 10, 'B2岛-01', 0, NULL, 150, 220, '2025-05-26 10:00:00', '2025-05-26 10:00:00', 0);
INSERT INTO `biz_workstation` VALUES (18, 10, 'B2岛-02', 0, NULL, 340, 230, '2025-05-26 10:00:00', '2025-05-26 10:00:00', 0);
INSERT INTO `biz_workstation` VALUES (19, 10, 'B2岛-03', 0, NULL, 520, 225, '2025-05-26 10:00:00', '2025-05-26 10:00:00', 0);
INSERT INTO `biz_workstation` VALUES (20, 10, 'B2岛-04', 2, 12, 680, 240, '2025-05-26 10:00:00', '2026-01-05 14:00:00', 0);
INSERT INTO `biz_workstation` VALUES (21, 10, 'B2岛-05', 0, NULL, 240, 400, '2025-05-26 10:00:00', '2025-05-26 10:00:00', 0);
INSERT INTO `biz_workstation` VALUES (22, 10, 'B2岛-06', 0, NULL, 460, 410, '2025-05-26 10:00:00', '2025-05-26 10:00:00', 0);
INSERT INTO `biz_workstation` VALUES (23, 7, 'A3东-09', 0, NULL, 820, 320, '2025-08-01 09:00:00', '2025-08-01 09:00:00', 0);
INSERT INTO `biz_workstation` VALUES (24, 8, 'A3静-06', 0, NULL, 600, 295, '2025-08-01 09:00:00', '2025-08-01 09:00:00', 0);

-- ----------------------------
-- Table structure for biz_workstation_facility
-- ----------------------------
DROP TABLE IF EXISTS `biz_workstation_facility`;
CREATE TABLE `biz_workstation_facility`  (
  `workstation_id` bigint NOT NULL COMMENT '工位ID',
  `facility_id` bigint NOT NULL COMMENT '设施ID',
  PRIMARY KEY (`workstation_id`, `facility_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '工位配套设施关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of biz_workstation_facility
-- ----------------------------
INSERT INTO `biz_workstation_facility` VALUES (1, 1);
INSERT INTO `biz_workstation_facility` VALUES (1, 2);
INSERT INTO `biz_workstation_facility` VALUES (1, 3);
INSERT INTO `biz_workstation_facility` VALUES (2, 1);
INSERT INTO `biz_workstation_facility` VALUES (2, 3);
INSERT INTO `biz_workstation_facility` VALUES (2, 4);
INSERT INTO `biz_workstation_facility` VALUES (3, 1);
INSERT INTO `biz_workstation_facility` VALUES (3, 2);
INSERT INTO `biz_workstation_facility` VALUES (3, 5);
INSERT INTO `biz_workstation_facility` VALUES (4, 1);
INSERT INTO `biz_workstation_facility` VALUES (4, 4);
INSERT INTO `biz_workstation_facility` VALUES (5, 2);
INSERT INTO `biz_workstation_facility` VALUES (5, 3);
INSERT INTO `biz_workstation_facility` VALUES (6, 1);
INSERT INTO `biz_workstation_facility` VALUES (6, 2);
INSERT INTO `biz_workstation_facility` VALUES (6, 6);
INSERT INTO `biz_workstation_facility` VALUES (8, 1);
INSERT INTO `biz_workstation_facility` VALUES (8, 3);
INSERT INTO `biz_workstation_facility` VALUES (9, 2);
INSERT INTO `biz_workstation_facility` VALUES (9, 6);
INSERT INTO `biz_workstation_facility` VALUES (10, 1);
INSERT INTO `biz_workstation_facility` VALUES (10, 2);
INSERT INTO `biz_workstation_facility` VALUES (11, 3);
INSERT INTO `biz_workstation_facility` VALUES (12, 4);
INSERT INTO `biz_workstation_facility` VALUES (13, 1);
INSERT INTO `biz_workstation_facility` VALUES (14, 1);
INSERT INTO `biz_workstation_facility` VALUES (14, 2);
INSERT INTO `biz_workstation_facility` VALUES (15, 3);
INSERT INTO `biz_workstation_facility` VALUES (16, 1);
INSERT INTO `biz_workstation_facility` VALUES (17, 2);
INSERT INTO `biz_workstation_facility` VALUES (18, 1);
INSERT INTO `biz_workstation_facility` VALUES (18, 5);
INSERT INTO `biz_workstation_facility` VALUES (19, 3);
INSERT INTO `biz_workstation_facility` VALUES (20, 1);
INSERT INTO `biz_workstation_facility` VALUES (20, 2);
INSERT INTO `biz_workstation_facility` VALUES (21, 4);
INSERT INTO `biz_workstation_facility` VALUES (22, 1);
INSERT INTO `biz_workstation_facility` VALUES (23, 2);
INSERT INTO `biz_workstation_facility` VALUES (24, 6);

-- ----------------------------
-- Table structure for sys_admin_space
-- ----------------------------
DROP TABLE IF EXISTS `sys_admin_space`;
CREATE TABLE `sys_admin_space`  (
  `user_id` bigint NOT NULL COMMENT 'sys_user.id，role_type=2',
  `space_id` bigint NOT NULL COMMENT 'biz_space.id',
  PRIMARY KEY (`user_id`, `space_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '区域管理员管辖空间' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_admin_space
-- ----------------------------
INSERT INTO `sys_admin_space` VALUES (2, 4);
INSERT INTO `sys_admin_space` VALUES (2, 5);
INSERT INTO `sys_admin_space` VALUES (2, 7);
INSERT INTO `sys_admin_space` VALUES (2, 8);

-- ----------------------------
-- Table structure for sys_config
-- ----------------------------
DROP TABLE IF EXISTS `sys_config`;
CREATE TABLE `sys_config`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `config_key` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '配置键（如：max_advance_days）',
  `config_value` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '配置值',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '参数说明',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_config_key`(`config_key` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 37 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '全局业务参数配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_config
-- ----------------------------
INSERT INTO `sys_config` VALUES (25, 'max_advance_days', '14', '可提前预约的最长天数', '2026-03-23 15:47:36', '2026-03-23 15:47:36');
INSERT INTO `sys_config` VALUES (26, 'sign_before_minutes', '15', '预约开始前允许签到的分钟数', '2026-03-23 15:47:36', '2026-03-23 15:47:36');
INSERT INTO `sys_config` VALUES (27, 'sign_after_minutes', '30', '预约开始后允许签到的宽限分钟数', '2026-03-23 15:47:36', '2026-03-23 15:47:36');
INSERT INTO `sys_config` VALUES (28, 'geo_radius_meters', '500', 'GEO 签到有效半径（米）', '2026-03-23 15:47:36', '2026-03-23 15:47:36');
INSERT INTO `sys_config` VALUES (29, 'initial_credit_score', '100', '新用户初始信用分', '2026-03-23 15:47:36', '2026-03-23 15:47:36');
INSERT INTO `sys_config` VALUES (30, 'credit_breach_deduct', '10', '未签到违约扣分', '2026-03-23 15:47:36', '2026-03-23 15:47:36');
INSERT INTO `sys_config` VALUES (31, 'credit_cancel_late_deduct', '5', '距开始不足1小时取消扣分', '2026-03-23 15:47:36', '2026-03-23 15:47:36');
INSERT INTO `sys_config` VALUES (32, 'credit_sign_reward', '1', '按时签到奖励分', '2026-03-23 15:47:36', '2026-03-23 15:47:36');
INSERT INTO `sys_config` VALUES (33, 'credit_release_reward', '2', '主动提前结束奖励分', '2026-03-23 15:47:36', '2026-03-23 15:47:36');
INSERT INTO `sys_config` VALUES (34, 'credit_ban_threshold', '60', '低于该分数禁止预约', '2026-03-23 15:47:36', '2026-03-23 15:47:36');
INSERT INTO `sys_config` VALUES (35, 'credit_ban_days', '7', '禁止预约持续天数', '2026-03-23 15:47:36', '2026-03-23 15:47:36');
INSERT INTO `sys_config` VALUES (36, 'reserve_rate_limit', '20', '每用户每分钟最多预约请求次数', '2026-03-23 15:47:36', '2026-03-23 15:47:36');

-- ----------------------------
-- Table structure for sys_dept
-- ----------------------------
DROP TABLE IF EXISTS `sys_dept`;
CREATE TABLE `sys_dept`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `parent_id` bigint NOT NULL DEFAULT 0 COMMENT '父级ID（0代表顶级部门）',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '部门名称',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除（0正常，1删除）',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 100 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '部门/组织架构表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_dept
-- ----------------------------
INSERT INTO `sys_dept` VALUES (1, 0, '研发中心', 0, '2025-05-08 10:00:00', '2025-05-08 10:00:00', 0);
INSERT INTO `sys_dept` VALUES (2, 0, '产品与设计中心', 1, '2025-05-08 10:00:00', '2025-05-08 10:00:00', 0);
INSERT INTO `sys_dept` VALUES (3, 0, '职能平台', 2, '2025-05-08 10:00:00', '2025-05-08 10:00:00', 0);
INSERT INTO `sys_dept` VALUES (4, 1, '基础架构组', 0, '2025-06-01 09:30:00', '2025-06-01 09:30:00', 0);

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '手机号（登录账号）',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '密码（管理员可能需要，普通用户验证码登录可为空）',
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '真实姓名',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像URL',
  `dept_id` bigint NULL DEFAULT NULL COMMENT '所属部门ID',
  `role_type` tinyint NOT NULL DEFAULT 3 COMMENT '角色：1-超级管理员，2-区域管理员，3-普通用户',
  `credit_score` int NOT NULL DEFAULT 100 COMMENT '信用积分（默认100）',
  `restrict_booking_until` datetime NULL DEFAULT NULL COMMENT '此前不可提交新预约',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '账号状态：1-正常，0-封禁',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册/创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_phone`(`phone` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 102 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, '13800001001', '$2a$10$0wzmy1A9nC/b7VzjIPzaJ.I7hUjfVXxEcEuEfuPsB6pAS8gvjQ/gi', '系统管理员', NULL, NULL, 1, 100, NULL, 1, '2025-05-08 09:00:00', '2026-03-01 12:00:00', 0);
INSERT INTO `sys_user` VALUES (2, '13800001002', '$2a$10$0wzmy1A9nC/b7VzjIPzaJ.I7hUjfVXxEcEuEfuPsB6pAS8gvjQ/gi', '林区域', NULL, NULL, 2, 100, NULL, 1, '2025-05-10 11:00:00', '2026-02-20 15:00:00', 0);
INSERT INTO `sys_user` VALUES (3, '13900138001', NULL, '张三', NULL, 1, 3, 96, NULL, 1, '2025-07-12 14:22:00', '2026-03-20 09:10:00', 0);
INSERT INTO `sys_user` VALUES (4, '13900138002', NULL, '李四', NULL, 1, 3, 100, NULL, 1, '2025-07-15 16:08:00', '2026-03-21 11:00:00', 0);
INSERT INTO `sys_user` VALUES (5, '13900138003', NULL, '王芳', NULL, 2, 3, 88, NULL, 1, '2025-08-03 10:15:00', '2026-03-18 18:00:00', 0);
INSERT INTO `sys_user` VALUES (6, '13900138004', NULL, '赵晨', NULL, 2, 3, 100, NULL, 1, '2025-08-20 09:40:00', '2026-03-22 08:30:00', 0);
INSERT INTO `sys_user` VALUES (7, '13900138005', NULL, '刘洋', NULL, 4, 3, 73, NULL, 0, '2025-09-01 13:00:00', '2026-03-19 17:20:00', 0);
INSERT INTO `sys_user` VALUES (8, '13900138006', NULL, '陈默', NULL, 1, 3, 100, NULL, 1, '2025-09-10 08:50:00', '2026-03-22 10:00:00', 0);
INSERT INTO `sys_user` VALUES (9, '13900138007', NULL, '周琪', NULL, 3, 3, 92, NULL, 1, '2025-10-05 15:30:00', '2026-03-15 12:00:00', 0);
INSERT INTO `sys_user` VALUES (10, '13900138008', NULL, '吴桐', NULL, 1, 3, 85, NULL, 1, '2025-10-18 11:20:00', '2026-03-10 14:40:00', 0);
INSERT INTO `sys_user` VALUES (11, '13900138009', NULL, '郑凯', NULL, 1, 3, 52, '2026-04-15 10:00:00', 1, '2025-11-02 09:00:00', '2026-03-12 16:00:00', 0);
INSERT INTO `sys_user` VALUES (12, '13900138010', NULL, '孙悦', NULL, 2, 3, 100, NULL, 1, '2025-11-20 10:10:00', '2026-03-22 09:00:00', 0);
INSERT INTO `sys_user` VALUES (13, '13900138011', NULL, '钱进', NULL, 4, 3, 98, NULL, 1, '2026-01-08 14:00:00', '2026-03-21 19:00:00', 0);
INSERT INTO `sys_user` VALUES (14, '13900138012', NULL, '何静', NULL, 3, 3, 79, NULL, 1, '2026-02-14 11:30:00', '2026-03-20 20:00:00', 0);
INSERT INTO `sys_user` VALUES (101, '13938549908', NULL, '王文博', NULL, 3, 3, 100, NULL, 1, '2026-03-23 16:40:17', '2026-03-23 16:40:24', 0);

SET FOREIGN_KEY_CHECKS = 1;
