/*
 Navicat Premium Dump SQL

 Source Server         : 192.168.247.128
 Source Server Type    : MySQL
 Source Server Version : 80040 (8.0.40-0ubuntu0.22.04.1)
 Source Host           : 192.168.247.128:3306
 Source Schema         : openwes

 Target Server Type    : MySQL
 Target Server Version : 80040 (8.0.40-0ubuntu0.22.04.1)
 File Encoding         : 65001

 Date: 08/12/2024 17:04:45
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for a_api
-- ----------------------------
CREATE TABLE `a_api`  (
                          `id` bigint NOT NULL,
                          `create_time` bigint NOT NULL DEFAULT 0 COMMENT 'Creation time',
                          `create_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Create user',
                          `update_time` bigint NOT NULL DEFAULT 0 COMMENT 'Update time',
                          `update_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Update user',
                          `api_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'api 类型',
                          `auth` bit(1) NOT NULL,
                          `auth_url` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'auth url',
                          `code` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
                          `description` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
                          `enabled` bit(1) NOT NULL,
                          `encoding` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求编码',
                          `format` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '请求格式',
                          `grant_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'grant type',
                          `headers` json NULL COMMENT '回传请求头',
                          `method` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '请求方式',
                          `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
                          `password` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'auth password',
                          `secret_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'auth secretId',
                          `secret_key` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'auth secretKey',
                          `sync_callback` bit(1) NOT NULL,
                          `token_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'auth tokenName',
                          `url` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求url',
                          `username` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'auth username',
                          PRIMARY KEY (`id`) USING BTREE,
                          UNIQUE INDEX `uk_code`(`code` ASC) USING BTREE,
                          INDEX `idx_api_type`(`api_type` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for a_api_config
-- ----------------------------
CREATE TABLE `a_api_config`  (
                                 `id` bigint NOT NULL,
                                 `create_time` bigint NOT NULL DEFAULT 0 COMMENT 'Creation time',
                                 `create_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Create user',
                                 `update_time` bigint NOT NULL DEFAULT 0 COMMENT 'Update time',
                                 `update_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Update user',
                                 `code` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
                                 `js_param_converter` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '请求参数转换脚本',
                                 `js_response_converter` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '响应参数转换脚本',
                                 `param_converter_type` smallint NULL DEFAULT NULL,
                                 `response_converter_type` smallint NULL DEFAULT NULL,
                                 `template_param_converter` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '请求参数转换模板',
                                 `template_response_converter` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '响应参数转换模板',
                                 PRIMARY KEY (`id`) USING BTREE,
                                 UNIQUE INDEX `uk_code`(`code` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for a_api_log
-- ----------------------------
CREATE TABLE `a_api_log`  (
                              `id` bigint NOT NULL,
                              `create_time` bigint NOT NULL DEFAULT 0 COMMENT 'Creation time',
                              `create_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Create user',
                              `api_code` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
                              `cost_time` int NOT NULL DEFAULT 0,
                              `message_id` bigint NOT NULL DEFAULT 0,
                              `request_data` json NULL COMMENT '请求参数',
                              `response_data` json NULL COMMENT '返回参数',
                              `retry_count` int NOT NULL DEFAULT 0,
                              `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'api log status',
                              PRIMARY KEY (`id`) USING BTREE,
                              UNIQUE INDEX `uk_message_id`(`message_id` ASC) USING BTREE,
                              INDEX `idx_api_code`(`api_code` ASC) USING BTREE,
                              INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for d_domain_event
-- ----------------------------
CREATE TABLE `d_domain_event`  (
                                   `id` bigint NOT NULL,
                                   `create_time` bigint NOT NULL,
                                   `event` varchar(5000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '事件信息',
                                   `event_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '事件类型',
                                   `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '状态',
                                   `update_time` bigint NOT NULL,
                                   `version` bigint NULL DEFAULT NULL,
                                   PRIMARY KEY (`id`) USING BTREE,
                                   INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for e_container_task
-- ----------------------------
CREATE TABLE `e_container_task`  (
                                     `id` bigint NOT NULL,
                                     `create_time` bigint NOT NULL DEFAULT 0 COMMENT 'Creation time',
                                     `create_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Create user',
                                     `update_time` bigint NOT NULL DEFAULT 0 COMMENT 'Update time',
                                     `update_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Update user',
                                     `business_task_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '业务任务 type',
                                     `container_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '容器号',
                                     `container_face` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '容器面',
                                     `container_spec_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '容器规格',
                                     `container_task_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '容器任务 type',
                                     `customer_task_ids` json NULL COMMENT '客户任务ID',
                                     `destinations` json NULL COMMENT '目的地',
                                     `parent_container_task_id` bigint NOT NULL DEFAULT 0 COMMENT '父容器任务ID',
                                     `start_location` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '起点',
                                     `task_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'taskCode',
                                     `task_group_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'taskGroupCode',
                                     `task_group_priority` int NOT NULL DEFAULT 0 COMMENT '组任务优先级',
                                     `task_priority` int NOT NULL DEFAULT 0 COMMENT '任务优先级',
                                     `task_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '任务 status',
                                     PRIMARY KEY (`id`) USING BTREE,
                                     UNIQUE INDEX `uk_task_code`(`task_code` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for e_container_task_and_business_task_relation
-- ----------------------------
CREATE TABLE `e_container_task_and_business_task_relation`  (
                                                                `id` bigint NOT NULL,
                                                                `create_time` bigint NOT NULL DEFAULT 0 COMMENT 'Creation time',
                                                                `create_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Create user',
                                                                `update_time` bigint NOT NULL DEFAULT 0 COMMENT 'Update time',
                                                                `update_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Update user',
                                                                `container_task_and_business_task_relation_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '任务 status',
                                                                `customer_task_id` bigint NULL DEFAULT NULL COMMENT '客户任务ID',
                                                                `task_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '搬箱任务编号',
                                                                `task_id` bigint NULL DEFAULT NULL COMMENT '搬箱任务ID',
                                                                PRIMARY KEY (`id`) USING BTREE,
                                                                INDEX `idx_task_id`(`task_id` ASC) USING BTREE,
                                                                INDEX `idx_task_code`(`task_code` ASC) USING BTREE,
                                                                INDEX `idx_customer_task_id`(`customer_task_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for e_ems_location_config
-- ----------------------------
CREATE TABLE `e_ems_location_config`  (
                                          `id` bigint NOT NULL,
                                          `create_time` bigint NOT NULL DEFAULT 0 COMMENT 'Creation time',
                                          `create_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Create user',
                                          `update_time` bigint NOT NULL DEFAULT 0 COMMENT 'Update time',
                                          `update_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Update user',
                                          `location_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '点位编码',
                                          `location_type` json NOT NULL COMMENT '点位描述',
                                          `warehouse_area_id` bigint NOT NULL COMMENT '库区ID',
                                          `warehouse_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '仓库',
                                          PRIMARY KEY (`id`) USING BTREE,
                                          UNIQUE INDEX `uk_location_code`(`location_code` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for m_address
-- ----------------------------
CREATE TABLE `m_address`  (
                              `id` bigint NOT NULL,
                              `create_time` bigint NOT NULL DEFAULT 0 COMMENT 'Creation time',
                              `create_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Create user',
                              `update_time` bigint NOT NULL DEFAULT 0 COMMENT 'Update time',
                              `update_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Update user',
                              `city` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '市',
                              `country` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '国家',
                              `district` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '区/县',
                              `province` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '省份',
                              PRIMARY KEY (`id`) USING BTREE,
                              UNIQUE INDEX `uk_c_p_c_d`(`country` ASC, `province` ASC, `city` ASC, `district` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for m_barcode_parse_rule
-- ----------------------------
CREATE TABLE `m_barcode_parse_rule`  (
                                         `id` bigint NOT NULL,
                                         `create_time` bigint NOT NULL DEFAULT 0 COMMENT 'Creation time',
                                         `create_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Create user',
                                         `update_time` bigint NOT NULL DEFAULT 0 COMMENT 'Update time',
                                         `update_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Update user',
                                         `brand` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '品牌',
                                         `business_flow` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '业务流程',
                                         `code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '编码',
                                         `enable` bit(1) NOT NULL,
                                         `execute_time` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '执行时间',
                                         `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '名称',
                                         `owner_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '货主编码',
                                         `regular_expression` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '正则表达式',
                                         `result_fields` json NULL COMMENT '解析规则参数',
                                         `union_location` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '拼接位置',
                                         `union_str` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '拼接符',
                                         `version` bigint NULL DEFAULT NULL,
                                         PRIMARY KEY (`id`) USING BTREE,
                                         UNIQUE INDEX `uk_barcode_parse_code`(`code` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for m_batch_attribute_config
-- ----------------------------
CREATE TABLE `m_batch_attribute_config`  (
                                             `id` bigint NOT NULL,
                                             `create_time` bigint NOT NULL DEFAULT 0 COMMENT 'Creation time',
                                             `create_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Create user',
                                             `update_time` bigint NOT NULL DEFAULT 0 COMMENT 'Update time',
                                             `update_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Update user',
                                             `batch_attribute_field_configs` json NULL COMMENT '参数配置',
                                             `code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '编码',
                                             `enable` bit(1) NOT NULL,
                                             `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '名称',
                                             `owner_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '货主编码',
                                             `sku_first_category` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '商品大类',
                                             `version` bigint NULL DEFAULT NULL,
                                             PRIMARY KEY (`id`) USING BTREE,
                                             UNIQUE INDEX `uk_batch_attribute_config_code`(`code` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for m_dictionary
-- ----------------------------
CREATE TABLE `m_dictionary`  (
                                 `id` bigint NOT NULL,
                                 `create_time` bigint NOT NULL DEFAULT 0 COMMENT 'Creation time',
                                 `create_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Create user',
                                 `update_time` bigint NOT NULL DEFAULT 0 COMMENT 'Update time',
                                 `update_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Update user',
                                 `code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '字典编码',
                                 `description` json NULL COMMENT '字典描述',
                                 `editable` bit(1) NOT NULL,
                                 `items` json NOT NULL COMMENT '字典内容',
                                 `name` json NOT NULL COMMENT '字典名称',
                                 `version` bigint NOT NULL,
                                 PRIMARY KEY (`id`) USING BTREE,
                                 UNIQUE INDEX `uk_dictionary_code`(`code` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for m_owner_main_data
-- ----------------------------
CREATE TABLE `m_owner_main_data`  (
                                      `id` bigint NOT NULL,
                                      `create_time` bigint NOT NULL DEFAULT 0 COMMENT 'Creation time',
                                      `create_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Create user',
                                      `update_time` bigint NOT NULL DEFAULT 0 COMMENT 'Update time',
                                      `update_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Update user',
                                      `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '详细地址',
                                      `city` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '市',
                                      `country` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '国家',
                                      `district` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '区',
                                      `fax` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '传真',
                                      `mail` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系邮箱',
                                      `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系人',
                                      `owner_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '货主编码',
                                      `owner_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '货主名称',
                                      `owner_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '货主类型',
                                      `province` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '省',
                                      `tel` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系人电话',
                                      `version` bigint NULL DEFAULT NULL,
                                      `warehouse_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '仓库编码',
                                      PRIMARY KEY (`id`) USING BTREE,
                                      UNIQUE INDEX `uk_owner_warehouse`(`owner_code` ASC, `warehouse_code` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for m_sku_barcode_data
-- ----------------------------
CREATE TABLE `m_sku_barcode_data`  (
                                       `id` bigint NOT NULL,
                                       `create_time` bigint NOT NULL DEFAULT 0 COMMENT 'Creation time',
                                       `create_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Create user',
                                       `update_time` bigint NOT NULL DEFAULT 0 COMMENT 'Update time',
                                       `update_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Update user',
                                       `bar_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商品条码',
                                       `sku_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'sku编码',
                                       `sku_id` bigint NOT NULL COMMENT 'skuID',
                                       PRIMARY KEY (`id`) USING BTREE,
                                       UNIQUE INDEX `uk_sku_id_and_barcode`(`sku_id` ASC, `bar_code` ASC) USING BTREE,
                                       INDEX `idx_barcode`(`bar_code` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for m_sku_main_data
-- ----------------------------
CREATE TABLE `m_sku_main_data`  (
                                    `id` bigint NOT NULL,
                                    `create_time` bigint NOT NULL DEFAULT 0 COMMENT 'Creation time',
                                    `create_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Create user',
                                    `update_time` bigint NOT NULL DEFAULT 0 COMMENT 'Update time',
                                    `update_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Update user',
                                    `barcode_rule_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '条码规则编码',
                                    `brand` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '品牌',
                                    `calculate_heat` bit(1) NOT NULL,
                                    `color` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '颜色',
                                    `effective_days` int NOT NULL COMMENT '效期限制天数',
                                    `enable_effective` bit(1) NOT NULL,
                                    `enable_sn` bit(1) NOT NULL,
                                    `gross_weight` bigint NOT NULL COMMENT '净重',
                                    `heat` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '热度',
                                    `height` bigint NOT NULL COMMENT '高度',
                                    `image_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '图片地址',
                                    `length` bigint NOT NULL COMMENT '长度',
                                    `max_stock` int NULL DEFAULT NULL COMMENT '库存上限',
                                    `min_stock` int NULL DEFAULT NULL COMMENT '库存下限',
                                    `net_weight` bigint NOT NULL COMMENT '毛重',
                                    `no_barcode` bit(1) NOT NULL,
                                    `owner_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '货主编码',
                                    `shelf_life` int NOT NULL COMMENT '保质期(天)',
                                    `size` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '尺码',
                                    `sku_attribute_category` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '一级属性',
                                    `sku_attribute_sub_category` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '二级属性',
                                    `sku_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'sku编码',
                                    `sku_first_category` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '一级分类',
                                    `sku_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'sku名称',
                                    `sku_second_category` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '二级分类',
                                    `sku_third_category` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '三级分类',
                                    `style` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '款式',
                                    `unit` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '单位',
                                    `version` bigint NULL DEFAULT NULL,
                                    `volume` bigint NOT NULL COMMENT '体积',
                                    `warehouse_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '仓库编码',
                                    `width` bigint NOT NULL COMMENT '宽度',
                                    PRIMARY KEY (`id`) USING BTREE,
                                    UNIQUE INDEX `uk_sku_owner_warehouse`(`sku_code` ASC, `owner_code` ASC, `warehouse_code` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for m_system_config
-- ----------------------------
CREATE TABLE `m_system_config`  (
                                    `id` bigint NOT NULL,
                                    `create_time` bigint NOT NULL DEFAULT 0 COMMENT 'Creation time',
                                    `create_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Create user',
                                    `update_time` bigint NOT NULL DEFAULT 0 COMMENT 'Update time',
                                    `update_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Update user',
                                    `basic_config` json NULL,
                                    `ems_config` json NULL,
                                    `inbound_config` json NULL,
                                    `outbound_algo_config` json NULL,
                                    `outbound_config` json NULL,
                                    `singleton_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
                                    `version` bigint NULL DEFAULT NULL,
                                    PRIMARY KEY (`id`) USING BTREE,
                                    UNIQUE INDEX `uk_singleton_key`(`singleton_key` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for m_warehouse_main_data
-- ----------------------------
CREATE TABLE `m_warehouse_main_data`  (
                                          `id` bigint NOT NULL,
                                          `create_time` bigint NOT NULL DEFAULT 0 COMMENT 'Creation time',
                                          `create_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Create user',
                                          `update_time` bigint NOT NULL DEFAULT 0 COMMENT 'Update time',
                                          `update_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Update user',
                                          `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '详细地址',
                                          `area` int NOT NULL DEFAULT 0 COMMENT '仓库面积(m²)',
                                          `business_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '主营业务',
                                          `city` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '市',
                                          `country` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '国家',
                                          `district` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '区',
                                          `fax` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '传真',
                                          `height` int NOT NULL DEFAULT 0 COMMENT '层高(m)',
                                          `mail` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系邮箱',
                                          `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系人',
                                          `province` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '省',
                                          `structure_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '仓库结构',
                                          `tel` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系人电话',
                                          `version` bigint NULL DEFAULT NULL,
                                          `virtual_warehouse` bit(1) NOT NULL,
                                          `warehouse_attr_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '仓库属性',
                                          `warehouse_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '仓库编码',
                                          `warehouse_label` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '仓库标签',
                                          `warehouse_level` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '仓库等级',
                                          `warehouse_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '仓库名称',
                                          `warehouse_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '仓库类型',
                                          PRIMARY KEY (`id`) USING BTREE,
                                          UNIQUE INDEX `uk_warehouse_code`(`warehouse_code` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for p_print_config
-- ----------------------------
CREATE TABLE `p_print_config`  (
                                   `id` bigint NOT NULL,
                                   `create_time` bigint NOT NULL DEFAULT 0 COMMENT 'Creation time',
                                   `create_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Create user',
                                   `update_time` bigint NOT NULL DEFAULT 0 COMMENT 'Update time',
                                   `update_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Update user',
                                   `config_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '配置编码',
                                   `delete_time` bigint NOT NULL DEFAULT 0 COMMENT '删除时间',
                                   `deleted` bit(1) NOT NULL,
                                   `enabled` bit(1) NOT NULL,
                                   `print_config_details` json NULL COMMENT '打印配置详情',
                                   `work_station_id` bigint NOT NULL COMMENT '工作站ID',
                                   PRIMARY KEY (`id`) USING BTREE,
                                   UNIQUE INDEX `idx_print_config_code`(`config_code` ASC, `delete_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for p_print_rule
-- ----------------------------
CREATE TABLE `p_print_rule`  (
                                 `id` bigint NOT NULL,
                                 `create_time` bigint NOT NULL DEFAULT 0 COMMENT 'Creation time',
                                 `create_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Create user',
                                 `update_time` bigint NOT NULL DEFAULT 0 COMMENT 'Update time',
                                 `update_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Update user',
                                 `currier_codes` json NULL COMMENT '承运商编码',
                                 `delete_time` bigint NOT NULL DEFAULT 0 COMMENT '删除时间',
                                 `deleted` bit(1) NOT NULL,
                                 `enabled` bit(1) NOT NULL,
                                 `inbound_order_types` json NULL COMMENT '入库单类型',
                                 `module` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '模块',
                                 `outbound_order_types` json NULL COMMENT '出库单类型',
                                 `owner_codes` json NULL COMMENT '货主编码',
                                 `print_count` int NOT NULL DEFAULT 1 COMMENT '打印份数',
                                 `print_node` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '打印节点',
                                 `rule_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '规则编码',
                                 `rule_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '规则名称',
                                 `sales_platforms` json NULL COMMENT '销售平台',
                                 `sql_script` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT 'sql script that query target args for the template',
                                 `template_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '模板编码',
                                 PRIMARY KEY (`id`) USING BTREE,
                                 UNIQUE INDEX `idx_print_rule_code`(`rule_code` ASC, `delete_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for p_print_template
-- ----------------------------
CREATE TABLE `p_print_template`  (
                                     `id` bigint NOT NULL,
                                     `create_time` bigint NOT NULL DEFAULT 0 COMMENT 'Creation time',
                                     `create_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Create user',
                                     `update_time` bigint NOT NULL DEFAULT 0 COMMENT 'Update time',
                                     `update_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Update user',
                                     `enabled` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '启用状态',
                                     `template_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '模板编码',
                                     `template_file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '模板文件名',
                                     `template_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '模板名称',
                                     PRIMARY KEY (`id`) USING BTREE,
                                     UNIQUE INDEX `idx_template_code`(`template_code` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for u_login_log
-- ----------------------------
CREATE TABLE `u_login_log`  (
                                `id` bigint NOT NULL,
                                `account` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '账号',
                                `gmt_login_time` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '登录时间',
                                `gmt_login_timestamp` bigint NOT NULL COMMENT '登录的时间戳',
                                `login_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '登录地址',
                                `login_failure_msg` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '登录失败原因',
                                `login_ip` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '登录ip',
                                `login_result` int NOT NULL COMMENT '登录结果(1成功, 2失败)',
                                PRIMARY KEY (`id`) USING BTREE,
                                INDEX `idx_user_account`(`account` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for u_menu
-- ----------------------------
CREATE TABLE `u_menu`  (
                           `id` bigint NOT NULL,
                           `create_time` bigint NOT NULL DEFAULT 0 COMMENT 'Creation time',
                           `create_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Create user',
                           `update_time` bigint NOT NULL DEFAULT 0 COMMENT 'Update time',
                           `update_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Update user',
                           `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '描述',
                           `enable` int NOT NULL COMMENT '是否启用(1启用, 0禁用)',
                           `icon` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '图标',
                           `iframe_show` int NULL DEFAULT NULL COMMENT '是否以 iframe 的方式显示(1启用, 0禁用)',
                           `order_num` int NOT NULL COMMENT '排序，数字越小越靠前',
                           `parent_id` bigint NOT NULL COMMENT '父菜单id,如果是顶级菜单, 则为0',
                           `path` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '路径地址',
                           `permissions` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '权限标识, 多个可用逗号分隔',
                           `system_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '所属系统',
                           `title` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '名称',
                           `type` int NOT NULL COMMENT '类型(1: 系统、2: 菜单、3: 权限)',
                           PRIMARY KEY (`id`) USING BTREE,
                           INDEX `idx_system_code`(`system_code` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for u_role
-- ----------------------------
CREATE TABLE `u_role`  (
                           `id` bigint NOT NULL,
                           `create_time` bigint NOT NULL DEFAULT 0 COMMENT 'Creation time',
                           `create_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Create user',
                           `update_time` bigint NOT NULL DEFAULT 0 COMMENT 'Update time',
                           `update_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Update user',
                           `code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色编码',
                           `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '描述',
                           `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色名称',
                           `status` int NOT NULL COMMENT '状态（1启用, 0停用）',
                           `warehouse_codes` json NOT NULL COMMENT '有权限查询的仓库',
                           PRIMARY KEY (`id`) USING BTREE,
                           UNIQUE INDEX `uk_role_code`(`code` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for u_role_menu
-- ----------------------------
CREATE TABLE `u_role_menu`  (
                                `id` bigint NOT NULL,
                                `menu_id` bigint NOT NULL COMMENT '菜单id',
                                `role_id` bigint NOT NULL COMMENT '角色id',
                                PRIMARY KEY (`id`) USING BTREE,
                                INDEX `idx_role_id`(`role_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for u_user
-- ----------------------------
CREATE TABLE `u_user`  (
                           `id` bigint NOT NULL,
                           `create_time` bigint NOT NULL DEFAULT 0 COMMENT 'Creation time',
                           `create_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Create user',
                           `update_time` bigint NOT NULL DEFAULT 0 COMMENT 'Update time',
                           `update_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Update user',
                           `account` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '登录用户名',
                           `avatar` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像地址',
                           `email` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮箱',
                           `last_gmt_login_time` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '上一次登录的时间',
                           `last_login_ip` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '上一次登录的ip地址',
                           `locked` int NOT NULL COMMENT '是否被锁(小于等于5表示未被锁, 大于5表示被锁)',
                           `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名称',
                           `password` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码(加密后)',
                           `phone` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '手机号',
                           `status` int NOT NULL COMMENT '状态（1启用, 0停用）',
                           `tenant_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '租户',
                           `type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '账号标识,默认为 NORMAL:普通账号',
                           PRIMARY KEY (`id`) USING BTREE,
                           UNIQUE INDEX `uk_account`(`account` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for u_user_role
-- ----------------------------
CREATE TABLE `u_user_role`  (
                                `id` bigint NOT NULL,
                                `role_id` bigint NOT NULL COMMENT '角色id',
                                `user_id` bigint NOT NULL COMMENT '用户id',
                                PRIMARY KEY (`id`) USING BTREE,
                                INDEX `idx_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for w_accept_order
-- ----------------------------
CREATE TABLE `w_accept_order`  (
                                   `id` bigint NOT NULL,
                                   `create_time` bigint NOT NULL DEFAULT 0 COMMENT 'Creation time',
                                   `create_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Create user',
                                   `update_time` bigint NOT NULL DEFAULT 0 COMMENT 'Update time',
                                   `update_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Update user',
                                   `audit_time` bigint NOT NULL DEFAULT 0 COMMENT 'audit time',
                                   `audit_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'audit user',
                                   `accept_method` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '收货方式',
                                   `accept_order_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '验收单状态',
                                   `accept_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '收货类型',
                                   `identify_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '验收单创建维度标识',
                                   `order_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '订单编号',
                                   `put_away` bit(1) NOT NULL,
                                   `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '备注',
                                   `total_box` int NOT NULL DEFAULT 0 COMMENT '验收总箱数',
                                   `total_qty` int NOT NULL DEFAULT 0 COMMENT '验收总数量',
                                   `version` bigint NULL DEFAULT NULL,
                                   `warehouse_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '仓库',
                                   PRIMARY KEY (`id`) USING BTREE,
                                   UNIQUE INDEX `uk_order_no`(`order_no` ASC) USING BTREE,
                                   INDEX `idx_identify_no`(`identify_no` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for w_accept_order_detail
-- ----------------------------
CREATE TABLE `w_accept_order_detail`  (
                                          `id` bigint NOT NULL,
                                          `create_time` bigint NOT NULL DEFAULT 0 COMMENT 'Creation time',
                                          `create_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Create user',
                                          `update_time` bigint NOT NULL DEFAULT 0 COMMENT 'Update time',
                                          `update_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Update user',
                                          `accept_order_id` bigint NOT NULL COMMENT '验收单ID',
                                          `batch_attributes` json NULL COMMENT '批次属性',
                                          `box_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '箱号',
                                          `brand` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '品牌',
                                          `color` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '颜色',
                                          `inbound_plan_order_detail_id` bigint NOT NULL COMMENT '入库通知单明细ID',
                                          `inbound_plan_order_id` bigint NOT NULL COMMENT '入库通知单ID',
                                          `owner_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '货主编码',
                                          `pack_box_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '箱号',
                                          `qty_accepted` int NOT NULL COMMENT '验收数量',
                                          `size` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '尺码',
                                          `sku_batch_attribute_id` bigint NOT NULL,
                                          `sku_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'sku编码',
                                          `sku_id` bigint NOT NULL,
                                          `sku_name` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'sku名称',
                                          `style` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '款式',
                                          `target_container_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '目标容器编码',
                                          `target_container_face` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '目标容器面',
                                          `target_container_id` bigint NOT NULL,
                                          `target_container_slot_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '目标容器格口编码',
                                          `target_container_spec_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '目标容器规格编码',
                                          `work_station_id` bigint NOT NULL COMMENT '工作站ID',
                                          PRIMARY KEY (`id`) USING BTREE,
                                          INDEX `idx_inbound_plan_order_detail_id`(`inbound_plan_order_detail_id` ASC) USING BTREE,
                                          INDEX `idx_inbound_plan_order__id`(`inbound_plan_order_id` ASC) USING BTREE,
                                          INDEX `idx_accept_order_id`(`accept_order_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for w_aisle
-- ----------------------------
CREATE TABLE `w_aisle`  (
                            `id` bigint NOT NULL,
                            `create_time` bigint NOT NULL DEFAULT 0 COMMENT 'Creation time',
                            `create_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Create user',
                            `update_time` bigint NOT NULL DEFAULT 0 COMMENT 'Update time',
                            `update_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Update user',
                            `aisle_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '巷道编码',
                            `single_entrance` bit(1) NOT NULL,
                            `warehouse_area_id` bigint NOT NULL DEFAULT 0 COMMENT '库区ID',
                            PRIMARY KEY (`id`) USING BTREE,
                            UNIQUE INDEX `uk_aisle_code_warehouse_area`(`aisle_code` ASC, `warehouse_area_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for w_container
-- ----------------------------
CREATE TABLE `w_container`  (
                                `id` bigint NOT NULL,
                                `create_time` bigint NOT NULL DEFAULT 0 COMMENT 'Creation time',
                                `create_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Create user',
                                `update_time` bigint NOT NULL DEFAULT 0 COMMENT 'Update time',
                                `update_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Update user',
                                `container_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '容器编码',
                                `container_slot_num` int NOT NULL DEFAULT 0 COMMENT '格口数量',
                                `container_slots` json NULL COMMENT '容器格口',
                                `container_spec_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '容器规格编码',
                                `container_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '容器状态',
                                `empty_container` bit(1) NOT NULL,
                                `empty_slot_num` int NOT NULL DEFAULT 0 COMMENT '空格口数量',
                                `location_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '库位编码',
                                `location_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '库位类型',
                                `locked` bit(1) NOT NULL,
                                `occupation_ratio` decimal(18, 6) NOT NULL DEFAULT 0.000000 COMMENT 'SKU体积占用比例',
                                `opened` bit(1) NOT NULL,
                                `version` bigint NULL DEFAULT NULL,
                                `warehouse_area_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '库区编码',
                                `warehouse_area_id` bigint NOT NULL COMMENT '库区ID',
                                `warehouse_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '仓库编码',
                                `warehouse_logic_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '逻辑区编码',
                                `warehouse_logic_id` bigint NOT NULL COMMENT '逻辑区ID',
                                PRIMARY KEY (`id`) USING BTREE,
                                UNIQUE INDEX `uk_container_code_warehouse_code`(`container_code` ASC, `warehouse_code` ASC) USING BTREE,
                                INDEX `idx_container_spec_code`(`container_spec_code` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for w_container_spec
-- ----------------------------
CREATE TABLE `w_container_spec`  (
                                     `id` bigint NOT NULL,
                                     `create_time` bigint NOT NULL DEFAULT 0 COMMENT 'Creation time',
                                     `create_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Create user',
                                     `update_time` bigint NOT NULL DEFAULT 0 COMMENT 'Update time',
                                     `update_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Update user',
                                     `container_slot_num` int NOT NULL DEFAULT 0 COMMENT '格子数量',
                                     `container_slot_specs` json NULL COMMENT '容器格子规格',
                                     `container_spec_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '容器规格编码',
                                     `container_spec_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '容器规格名称',
                                     `container_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '容器类型',
                                     `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '描述',
                                     `height` bigint NOT NULL COMMENT '高度',
                                     `length` bigint NOT NULL COMMENT '长度',
                                     `location` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '播种墙位置：LEFT/MIDDLE/RIGHT',
                                     `version` bigint NULL DEFAULT NULL,
                                     `volume` bigint NOT NULL COMMENT '体积',
                                     `warehouse_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '仓库编码',
                                     `width` bigint NOT NULL COMMENT '宽度',
                                     PRIMARY KEY (`id`) USING BTREE,
                                     UNIQUE INDEX `uk_container_spec_code_warehouse`(`container_spec_code` ASC, `warehouse_code` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for w_container_stock
-- ----------------------------
CREATE TABLE `w_container_stock`  (
                                      `id` bigint NOT NULL,
                                      `create_time` bigint NOT NULL DEFAULT 0 COMMENT 'Creation time',
                                      `create_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Create user',
                                      `update_time` bigint NOT NULL DEFAULT 0 COMMENT 'Update time',
                                      `update_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Update user',
                                      `available_qty` int NOT NULL DEFAULT 0 COMMENT '可用数量',
                                      `box_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '箱号',
                                      `box_stock` bit(1) NOT NULL,
                                      `container_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '容器编码',
                                      `container_face` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '容器面',
                                      `container_id` bigint NULL DEFAULT 0 COMMENT '容器id',
                                      `container_slot_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '容器格口编码',
                                      `frozen_qty` int NOT NULL DEFAULT 0 COMMENT '冻结数量',
                                      `no_outbound_locked_qty` int NOT NULL DEFAULT 0 COMMENT '库内锁定数量',
                                      `outbound_locked_qty` int NOT NULL DEFAULT 0 COMMENT '出库锁定数量',
                                      `sku_batch_attribute_id` bigint NOT NULL DEFAULT 0 COMMENT 'skuBatchAttributeId',
                                      `sku_batch_stock_id` bigint NOT NULL DEFAULT 0 COMMENT '批次库存id',
                                      `sku_id` bigint NOT NULL DEFAULT 0 COMMENT 'skuId',
                                      `total_qty` int NOT NULL DEFAULT 0 COMMENT '总数量',
                                      `version` bigint NULL DEFAULT NULL,
                                      `warehouse_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '仓库编码',
                                      PRIMARY KEY (`id`) USING BTREE,
                                      UNIQUE INDEX `uk_container_slot_sku_batch`(`container_code` ASC, `container_slot_code` ASC, `sku_batch_stock_id` ASC) USING BTREE,
                                      INDEX `idx_sku_batch_stock_id`(`sku_batch_stock_id` ASC) USING BTREE,
                                      INDEX `idx_sku_id`(`sku_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for w_container_stock_transaction
-- ----------------------------
CREATE TABLE `w_container_stock_transaction`  (
                                                  `id` bigint NOT NULL,
                                                  `create_time` bigint NOT NULL DEFAULT 0 COMMENT 'Creation time',
                                                  `create_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Create user',
                                                  `update_time` bigint NOT NULL DEFAULT 0 COMMENT 'Update time',
                                                  `update_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Update user',
                                                  `container_stock_id` bigint NOT NULL DEFAULT 0 COMMENT '明细库存id',
                                                  `operation_task_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
                                                  `order_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '订单号',
                                                  `sku_batch_stock_id` bigint NOT NULL DEFAULT 0 COMMENT '批次库存id',
                                                  `source_container_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '原容器编码',
                                                  `source_container_slot_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '原容器格口编码',
                                                  `target_container_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '目标容器编码',
                                                  `target_container_slot_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '目标容器格口编码',
                                                  `task_id` bigint NOT NULL DEFAULT 0 COMMENT '任务id',
                                                  `transfer_qty` int NOT NULL DEFAULT 0 COMMENT '数量',
                                                  `version` bigint NULL DEFAULT NULL,
                                                  `warehouse_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '仓库编码',
                                                  `container_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '容器编码',
                                                  `container_slot_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '容器格口编码',
                                                  `sku_id` bigint NOT NULL DEFAULT 0 COMMENT 'skuID',
                                                  PRIMARY KEY (`id`) USING BTREE,
                                                  INDEX `idx_container_code`(`source_container_code` ASC) USING BTREE,
                                                  INDEX `idx_order_no`(`order_no` ASC) USING BTREE,
                                                  INDEX `idx_sku_id`(`sku_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for w_empty_container_inbound_order
-- ----------------------------
CREATE TABLE `w_empty_container_inbound_order`  (
                                                    `id` bigint NOT NULL,
                                                    `create_time` bigint NOT NULL DEFAULT 0 COMMENT 'Creation time',
                                                    `create_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Create user',
                                                    `update_time` bigint NOT NULL DEFAULT 0 COMMENT 'Update time',
                                                    `update_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Update user',
                                                    `audit_time` bigint NOT NULL DEFAULT 0 COMMENT 'audit time',
                                                    `audit_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'audit user',
                                                    `inbound_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '状态',
                                                    `inbound_way` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '入库方式',
                                                    `order_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '订单编号',
                                                    `plan_count` int NOT NULL COMMENT '计划数量',
                                                    `warehouse_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '仓库编码',
                                                    PRIMARY KEY (`id`) USING BTREE,
                                                    UNIQUE INDEX `uk_empty_container_inbound_order_order_no`(`order_no` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for w_empty_container_inbound_order_detail
-- ----------------------------
CREATE TABLE `w_empty_container_inbound_order_detail`  (
                                                           `id` bigint NOT NULL,
                                                           `create_time` bigint NOT NULL DEFAULT 0 COMMENT 'Creation time',
                                                           `create_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Create user',
                                                           `update_time` bigint NOT NULL DEFAULT 0 COMMENT 'Update time',
                                                           `update_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Update user',
                                                           `audit_time` bigint NOT NULL DEFAULT 0 COMMENT 'audit time',
                                                           `audit_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'audit user',
                                                           `container_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '容器编号',
                                                           `container_spec_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '容器规格',
                                                           `empty_container_inbound_order_id` bigint NOT NULL COMMENT '空箱入库单ID',
                                                           `inbound_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '状态',
                                                           `location_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '地面码',
                                                           PRIMARY KEY (`id`) USING BTREE,
                                                           INDEX `idx_empty_container_inbound_order_id`(`empty_container_inbound_order_id` ASC) USING BTREE,
                                                           INDEX `idx_container_code`(`container_code` ASC) USING BTREE,
                                                           INDEX `idx_location_code`(`location_code` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for w_inbound_plan_order
-- ----------------------------
CREATE TABLE `w_inbound_plan_order`  (
                                         `id` bigint NOT NULL,
                                         `create_time` bigint NOT NULL DEFAULT 0 COMMENT 'Creation time',
                                         `create_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Create user',
                                         `update_time` bigint NOT NULL DEFAULT 0 COMMENT 'Update time',
                                         `update_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Update user',
                                         `audit_time` bigint NOT NULL DEFAULT 0 COMMENT 'audit time',
                                         `audit_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'audit user',
                                         `abnormal` bit(1) NOT NULL,
                                         `carrier` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '承运商',
                                         `customer_order_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '客户订单编号',
                                         `customer_order_type` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'customer order type',
                                         `estimated_arrival_date` bigint NOT NULL COMMENT '预计到达时间',
                                         `extend_fields` json NULL COMMENT '扩展字段',
                                         `inbound_plan_order_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '状态',
                                         `lpn_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'LPN',
                                         `order_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '订单编号',
                                         `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '备注',
                                         `sender` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '发货方',
                                         `shipping_method` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '承运方式',
                                         `sku_kind_num` int NOT NULL COMMENT 'SKU种类',
                                         `storage_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '存储类型',
                                         `total_box` int NOT NULL DEFAULT 0 COMMENT '总箱数',
                                         `total_qty` int NOT NULL DEFAULT 0 COMMENT '总数量',
                                         `tracking_number` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '承运单号',
                                         `version` bigint NULL DEFAULT NULL,
                                         `warehouse_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '仓库',
                                         PRIMARY KEY (`id`) USING BTREE,
                                         UNIQUE INDEX `uk_order_no`(`order_no` ASC) USING BTREE,
                                         INDEX `idx_customer_order_no`(`customer_order_no` ASC) USING BTREE,
                                         INDEX `idx_lpn`(`lpn_code` ASC) USING BTREE,
                                         INDEX `idx_inbound_plan_order_status`(`inbound_plan_order_status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for w_inbound_plan_order_detail
-- ----------------------------
CREATE TABLE `w_inbound_plan_order_detail`  (
                                                `id` bigint NOT NULL,
                                                `create_time` bigint NOT NULL DEFAULT 0 COMMENT 'Creation time',
                                                `create_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Create user',
                                                `update_time` bigint NOT NULL DEFAULT 0 COMMENT 'Update time',
                                                `update_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Update user',
                                                `abnormal_reason` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '异常原因',
                                                `batch_attributes` json NULL COMMENT '批次属性',
                                                `box_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '箱号',
                                                `brand` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '品牌',
                                                `color` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '颜色',
                                                `extend_fields` json NULL COMMENT '扩展字段',
                                                `inbound_plan_order_id` bigint NOT NULL COMMENT '入库通知单ID',
                                                `owner_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '货主编码',
                                                `qty_abnormal` int NOT NULL COMMENT '异常数量',
                                                `qty_accepted` int NOT NULL COMMENT '验收数量',
                                                `qty_restocked` int NOT NULL COMMENT '计划数量',
                                                `qty_unreceived` int NOT NULL COMMENT '未收货数量',
                                                `responsible_party` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '异常原因责任方',
                                                `size` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '尺码',
                                                `sku_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'sku编码',
                                                `sku_id` bigint NOT NULL COMMENT 'skuID',
                                                `sku_name` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'sku名称',
                                                `style` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '款式',
                                                PRIMARY KEY (`id`) USING BTREE,
                                                INDEX `idx_inbound_plan_order_id`(`inbound_plan_order_id` ASC) USING BTREE,
                                                INDEX `idx_box_no`(`box_no` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for w_location
-- ----------------------------
CREATE TABLE `w_location`  (
                               `id` bigint NOT NULL,
                               `create_time` bigint NOT NULL DEFAULT 0 COMMENT 'Creation time',
                               `create_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Create user',
                               `update_time` bigint NOT NULL DEFAULT 0 COMMENT 'Update time',
                               `update_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Update user',
                               `aisle_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '巷道编码',
                               `heat` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '热度',
                               `location_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '库位编码',
                               `location_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '库位状态',
                               `location_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '库位类型',
                               `occupied` bit(1) NOT NULL,
                               `position` json NOT NULL COMMENT '位置信息',
                               `shelf_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '货架编码',
                               `version` bigint NOT NULL,
                               `warehouse_area_id` bigint NOT NULL DEFAULT 0 COMMENT '库区ID',
                               `warehouse_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '仓库编码',
                               `warehouse_logic_id` bigint NULL DEFAULT 0 COMMENT '逻辑区ID',
                               PRIMARY KEY (`id`) USING BTREE,
                               UNIQUE INDEX `uk_warehouse_area_location`(`location_code` ASC, `warehouse_area_id` ASC) USING BTREE,
                               INDEX `idx_aisle_warehouse_area`(`aisle_code` ASC, `warehouse_area_id` ASC) USING BTREE,
                               INDEX `idx_warehouse_area_id`(`warehouse_area_id` ASC) USING BTREE,
                               INDEX `idx_warehouse_logic_id`(`warehouse_logic_id` ASC) USING BTREE,
                               INDEX `idx_shelf_code`(`shelf_code` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for w_operation_task
-- ----------------------------
CREATE TABLE `w_operation_task`  (
                                     `id` bigint NOT NULL,
                                     `create_time` bigint NOT NULL DEFAULT 0 COMMENT 'Creation time',
                                     `create_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Create user',
                                     `update_time` bigint NOT NULL DEFAULT 0 COMMENT 'Update time',
                                     `update_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Update user',
                                     `abnormal` bit(1) NOT NULL,
                                     `abnormal_qty` int NOT NULL COMMENT '异常数量',
                                     `assigned_station_slot` json NULL COMMENT '分配的工作站格口',
                                     `box_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '箱号',
                                     `container_stock_id` bigint NOT NULL DEFAULT 0 COMMENT '容器库存ID',
                                     `detail_id` bigint NOT NULL COMMENT '单据明细ID',
                                     `operated_qty` int NOT NULL COMMENT '操作数量',
                                     `order_id` bigint NOT NULL COMMENT '单据ID：可能是拣货单/理货单',
                                     `order_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '订单号',
                                     `priority` int NOT NULL DEFAULT 0 COMMENT 'priority',
                                     `required_qty` int NOT NULL COMMENT '需求数量',
                                     `sku_batch_attribute_id` bigint NOT NULL DEFAULT 0 COMMENT '批次ID',
                                     `sku_batch_stock_id` bigint NOT NULL DEFAULT 0 COMMENT '批次库存ID',
                                     `sku_id` bigint NOT NULL DEFAULT 0 COMMENT 'SKU ID',
                                     `source_container_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '原容器编码',
                                     `source_container_face` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '原容器面',
                                     `source_container_slot` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '原容器格口编码',
                                     `target_container_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '目标容器编码',
                                     `target_container_slot_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '目标容器格口编码',
                                     `target_location_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '目标库位',
                                     `task_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '任务状态',
                                     `task_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '任务类型',
                                     `transfer_container_record_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '周转容器记录ID',
                                     `version` bigint NULL DEFAULT NULL,
                                     `warehouse_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '仓库编号',
                                     `work_station_id` bigint NOT NULL COMMENT '工作站ID',
                                     PRIMARY KEY (`id`) USING BTREE,
                                     INDEX `idx_status_source_container_code`(`task_status` ASC, `source_container_code` ASC) USING BTREE,
                                     INDEX `idx_transfer_container_record_id`(`transfer_container_record_id` ASC) USING BTREE,
                                     INDEX `idx_picking_order_id`(`order_id` ASC, `detail_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for w_outbound_plan_order
-- ----------------------------
CREATE TABLE `w_outbound_plan_order`  (
                                          `id` bigint NOT NULL,
                                          `create_time` bigint NOT NULL DEFAULT 0 COMMENT 'Creation time',
                                          `create_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Create user',
                                          `update_time` bigint NOT NULL DEFAULT 0 COMMENT 'Update time',
                                          `update_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Update user',
                                          `audit_time` bigint NOT NULL DEFAULT 0 COMMENT 'audit time',
                                          `audit_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'audit user',
                                          `abnormal` bit(1) NOT NULL,
                                          `abnormal_reason` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '异常原因',
                                          `currier_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '承运商',
                                          `customer_order_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '客户订单编号',
                                          `customer_order_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '订单类型',
                                          `customer_wave_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '外部波次号',
                                          `destinations` json NULL COMMENT '出库封箱目的地',
                                          `expired_time` bigint NOT NULL COMMENT '截单时间',
                                          `order_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '订单编号',
                                          `orig_platform_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '来源平台',
                                          `outbound_plan_order_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '状态',
                                          `priority` bigint NOT NULL COMMENT '优先级',
                                          `reserved_fields` json NULL COMMENT '扩展字段',
                                          `short_outbound` bit(1) NOT NULL,
                                          `short_waiting` bit(1) NOT NULL,
                                          `sku_kind_num` int NOT NULL COMMENT 'SKU种类',
                                          `total_qty` int NOT NULL COMMENT '总数量',
                                          `version` bigint NULL DEFAULT NULL,
                                          `warehouse_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '仓库',
                                          `wave_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '波次号',
                                          `waybill_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '运单号',
                                          PRIMARY KEY (`id`) USING BTREE,
                                          UNIQUE INDEX `uk_order_no`(`order_no` ASC) USING BTREE,
                                          INDEX `idx_customer_order_no`(`customer_order_no` ASC) USING BTREE,
                                          INDEX `idx_customer_wave_no`(`customer_wave_no` ASC) USING BTREE,
                                          INDEX `idx_outbound_plan_order_status`(`outbound_plan_order_status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for w_outbound_plan_order_detail
-- ----------------------------
CREATE TABLE `w_outbound_plan_order_detail`  (
                                                 `id` bigint NOT NULL,
                                                 `create_time` bigint NOT NULL DEFAULT 0 COMMENT 'Creation time',
                                                 `create_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Create user',
                                                 `update_time` bigint NOT NULL DEFAULT 0 COMMENT 'Update time',
                                                 `update_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Update user',
                                                 `batch_attributes` json NULL COMMENT '批次属性',
                                                 `outbound_plan_order_detail_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '状态',
                                                 `outbound_plan_order_id` bigint NOT NULL COMMENT '出库计划单ID',
                                                 `owner_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '货主编码',
                                                 `qty_actual` int NOT NULL COMMENT '实际拣货数量',
                                                 `qty_allocated` int NOT NULL COMMENT '预占的批次库存数量',
                                                 `qty_required` int NOT NULL COMMENT '计划数量',
                                                 `reserved_fields` json NULL COMMENT '扩展字段',
                                                 `sku_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'sku编码',
                                                 `sku_id` bigint NOT NULL COMMENT 'skuID',
                                                 `sku_name` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'sku名称',
                                                 `version` bigint NULL DEFAULT NULL,
                                                 `warehouse_area_ids` json NULL COMMENT '库区ID列表',
                                                 PRIMARY KEY (`id`) USING BTREE,
                                                 INDEX `idx_outbound_plan_order_id`(`outbound_plan_order_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for w_outbound_pre_allocated_record
-- ----------------------------
CREATE TABLE `w_outbound_pre_allocated_record`  (
                                                    `id` bigint NOT NULL,
                                                    `create_time` bigint NOT NULL DEFAULT 0 COMMENT 'Creation time',
                                                    `create_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Create user',
                                                    `batch_attributes` json NULL COMMENT '批次属性',
                                                    `outbound_plan_order_detail_id` bigint NOT NULL COMMENT '出库计划单明细ID',
                                                    `outbound_plan_order_id` bigint NOT NULL COMMENT '出库计划单ID',
                                                    `owner_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '货主',
                                                    `qty_pre_allocated` int NOT NULL COMMENT '预占用数量',
                                                    `sku_batch_stock_id` bigint NOT NULL COMMENT 'sku batch stock ID',
                                                    `sku_id` bigint NOT NULL COMMENT 'skuID',
                                                    `version` bigint NULL DEFAULT NULL,
                                                    `warehouse_area_id` bigint NOT NULL COMMENT 'warehouse area ID',
                                                    `warehouse_area_ids` json NULL COMMENT 'warehouse area IDs',
                                                    PRIMARY KEY (`id`) USING BTREE,
                                                    INDEX `idx_outbound_plan_order_id`(`outbound_plan_order_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for w_outbound_wave
-- ----------------------------
CREATE TABLE `w_outbound_wave`  (
                                    `id` bigint NOT NULL,
                                    `create_time` bigint NOT NULL DEFAULT 0 COMMENT 'Creation time',
                                    `create_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Create user',
                                    `update_time` bigint NOT NULL DEFAULT 0 COMMENT 'Update time',
                                    `update_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Update user',
                                    `outbound_plan_order_ids` json NULL COMMENT '出库计划单ID',
                                    `priority` int NOT NULL,
                                    `short_outbound` bit(1) NOT NULL,
                                    `version` bigint NULL DEFAULT NULL,
                                    `warehouse_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '仓库',
                                    `wave_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '波次号',
                                    `wave_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '状态',
                                    PRIMARY KEY (`id`) USING BTREE,
                                    UNIQUE INDEX `uk_wave_no`(`wave_no` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for w_picking_order
-- ----------------------------
CREATE TABLE `w_picking_order`  (
                                    `id` bigint NOT NULL,
                                    `create_time` bigint NOT NULL DEFAULT 0 COMMENT 'Creation time',
                                    `create_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Create user',
                                    `update_time` bigint NOT NULL DEFAULT 0 COMMENT 'Update time',
                                    `update_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Update user',
                                    `allow_receive` bit(1) NOT NULL,
                                    `assigned_station_slot` json NULL COMMENT '分配的工作站格口',
                                    `is_reallocated_order` bit(1) NOT NULL,
                                    `picking_order_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '拣选单号',
                                    `picking_order_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '状态',
                                    `priority` int NOT NULL,
                                    `received_user_account` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '领取单据的用户账号',
                                    `short_outbound` bit(1) NOT NULL,
                                    `version` bigint NULL DEFAULT NULL,
                                    `warehouse_area_id` bigint NOT NULL COMMENT '库区',
                                    `warehouse_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '仓库',
                                    `wave_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '波次号',
                                    PRIMARY KEY (`id`) USING BTREE,
                                    UNIQUE INDEX `uk_picking_order_no`(`picking_order_no` ASC) USING BTREE,
                                    INDEX `idx_received_user_account`(`received_user_account` ASC) USING BTREE,
                                    INDEX `idx_wave_no`(`wave_no` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for w_picking_order_detail
-- ----------------------------
CREATE TABLE `w_picking_order_detail`  (
                                           `id` bigint NOT NULL,
                                           `create_time` bigint NOT NULL DEFAULT 0 COMMENT 'Creation time',
                                           `create_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Create user',
                                           `update_time` bigint NOT NULL DEFAULT 0 COMMENT 'Update time',
                                           `update_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Update user',
                                           `batch_attributes` json NULL COMMENT '批次属性',
                                           `outbound_order_plan_detail_id` bigint NOT NULL COMMENT '出库计划单明细ID',
                                           `outbound_order_plan_id` bigint NOT NULL COMMENT '出库计划单ID',
                                           `owner_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '货主',
                                           `picking_order_detail_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '状态',
                                           `picking_order_id` bigint NOT NULL COMMENT '拣货单ID',
                                           `qty_abnormal` int NOT NULL COMMENT '异常登记数量',
                                           `qty_actual` int NOT NULL COMMENT '实际拣货数量',
                                           `qty_required` int NOT NULL COMMENT '计划数量',
                                           `qty_short` int NOT NULL COMMENT '缺拣数量',
                                           `retargeting_warehouse_area_ids` json NULL COMMENT '库存重定位库区IDS',
                                           `sku_batch_stock_id` bigint NOT NULL COMMENT 'sku batch stock ID',
                                           `sku_id` bigint NOT NULL COMMENT 'skuID',
                                           `version` bigint NULL DEFAULT NULL,
                                           PRIMARY KEY (`id`) USING BTREE,
                                           INDEX `idx_picking_order_id`(`picking_order_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for w_put_away_task
-- ----------------------------
CREATE TABLE `w_put_away_task`  (
                                    `id` bigint NOT NULL,
                                    `create_time` bigint NOT NULL DEFAULT 0 COMMENT 'Creation time',
                                    `create_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Create user',
                                    `update_time` bigint NOT NULL DEFAULT 0 COMMENT 'Update time',
                                    `update_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Update user',
                                    `audit_time` bigint NOT NULL DEFAULT 0 COMMENT 'audit time',
                                    `audit_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'audit user',
                                    `container_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '容器编码',
                                    `container_spec_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '容器规格',
                                    `extend_fields` json NULL COMMENT '扩展字段',
                                    `location_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '库位编码',
                                    `task_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '任务编号',
                                    `task_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '上架任务状态',
                                    `task_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '上架任务类型',
                                    `version` bigint NULL DEFAULT NULL,
                                    `warehouse_area_id` bigint NOT NULL,
                                    `warehouse_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '仓库编码',
                                    `work_station_id` bigint NOT NULL DEFAULT 0 COMMENT '工作台ID',
                                    PRIMARY KEY (`id`) USING BTREE,
                                    UNIQUE INDEX `uk_order_no`(`task_no` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for w_put_away_task_detail
-- ----------------------------
CREATE TABLE `w_put_away_task_detail`  (
                                           `id` bigint NOT NULL,
                                           `accept_order_detail_id` bigint NOT NULL COMMENT 'accept order detail id',
                                           `accept_order_id` bigint NOT NULL COMMENT 'accept order id',
                                           `batch_attributes` json NULL COMMENT '批次属性',
                                           `container_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '容器编码',
                                           `container_face` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '容器面',
                                           `container_id` bigint NOT NULL,
                                           `container_slot_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '容器格口',
                                           `extend_fields` json NULL COMMENT '扩展字段',
                                           `owner_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '货主编码',
                                           `put_away_task_id` bigint NOT NULL DEFAULT 0 COMMENT '上架任务ID',
                                           `qty_put_away` bigint NOT NULL DEFAULT 0 COMMENT '上架数量',
                                           `sku_batch_attribute_id` bigint NOT NULL DEFAULT 0 COMMENT '批次属性 ID',
                                           `sku_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'SKU 代码',
                                           `sku_id` bigint NOT NULL DEFAULT 0 COMMENT 'SKU id',
                                           `sku_name` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'SKU 名称',
                                           PRIMARY KEY (`id`) USING BTREE,
                                           INDEX `idx_put_away_task_id`(`put_away_task_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for w_put_wall
-- ----------------------------
CREATE TABLE `w_put_wall`  (
                               `id` bigint NOT NULL,
                               `create_time` bigint NOT NULL DEFAULT 0 COMMENT 'Creation time',
                               `create_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Create user',
                               `update_time` bigint NOT NULL DEFAULT 0 COMMENT 'Update time',
                               `update_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Update user',
                               `container_spec_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '容器规格',
                               `delete_time` bigint NOT NULL DEFAULT 0 COMMENT '删除时间',
                               `deleted` bit(1) NOT NULL,
                               `enable` bit(1) NOT NULL,
                               `location` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '播种墙的位置',
                               `put_wall_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '播种墙编码',
                               `put_wall_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '播种墙名称',
                               `put_wall_status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '播种墙状态',
                               `version` bigint NULL DEFAULT NULL,
                               `work_station_id` bigint NOT NULL COMMENT '工作站ID',
                               PRIMARY KEY (`id`) USING BTREE,
                               UNIQUE INDEX `uk_put_wall_work_station`(`put_wall_code` ASC, `work_station_id` ASC, `delete_time` ASC) USING BTREE,
                               INDEX `idx_work_station`(`work_station_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for w_put_wall_slot
-- ----------------------------
CREATE TABLE `w_put_wall_slot`  (
                                    `id` bigint NOT NULL,
                                    `create_time` bigint NOT NULL DEFAULT 0 COMMENT 'Creation time',
                                    `create_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Create user',
                                    `update_time` bigint NOT NULL DEFAULT 0 COMMENT 'Update time',
                                    `update_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Update user',
                                    `bay` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '所在列编码',
                                    `enable` bit(1) NOT NULL,
                                    `face` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '播种墙位置',
                                    `level` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '所在层编码',
                                    `loc_bay` int NULL DEFAULT NULL COMMENT '所在列',
                                    `loc_level` int NULL DEFAULT NULL COMMENT '所在层',
                                    `picking_order_id` bigint NOT NULL COMMENT '已分配拣选单ID',
                                    `ptl_tag` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '电子标签号',
                                    `put_wall_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '播种墙编码',
                                    `put_wall_id` bigint NOT NULL COMMENT '播种墙ID',
                                    `put_wall_slot_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '播种墙槽口编码',
                                    `put_wall_slot_status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '槽口状态',
                                    `transfer_container_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '槽口已绑容器编号',
                                    `transfer_container_record_id` bigint NULL DEFAULT NULL COMMENT '周转容器记录ID',
                                    `version` bigint NULL DEFAULT NULL,
                                    `work_station_id` bigint NOT NULL COMMENT '工作站ID',
                                    PRIMARY KEY (`id`) USING BTREE,
                                    UNIQUE INDEX `uk_work_station_slot_code`(`work_station_id` ASC, `put_wall_slot_code` ASC) USING BTREE,
                                    INDEX `idx_put_wall_id`(`put_wall_id` ASC) USING BTREE,
                                    INDEX `idx_picking_order_id`(`picking_order_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for w_sku_batch_attribute
-- ----------------------------
CREATE TABLE `w_sku_batch_attribute`  (
                                          `id` bigint NOT NULL,
                                          `create_time` bigint NOT NULL DEFAULT 0 COMMENT 'Creation time',
                                          `create_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Create user',
                                          `update_time` bigint NOT NULL DEFAULT 0 COMMENT 'Update time',
                                          `update_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Update user',
                                          `batch_no` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '批次号',
                                          `sku_attribute1` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '批次属性预留',
                                          `sku_attribute10` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '批次属性预留',
                                          `sku_attribute2` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '批次属性预留',
                                          `sku_attribute3` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '批次属性预留',
                                          `sku_attribute4` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '批次属性预留',
                                          `sku_attribute5` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '批次属性预留',
                                          `sku_attribute6` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '批次属性预留',
                                          `sku_attribute7` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '批次属性预留',
                                          `sku_attribute8` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '批次属性预留',
                                          `sku_attribute9` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '批次属性预留',
                                          `sku_attributes` json NULL COMMENT '批次属性',
                                          `sku_id` bigint NOT NULL DEFAULT 0 COMMENT 'skuId',
                                          `version` bigint NULL DEFAULT NULL,
                                          PRIMARY KEY (`id`) USING BTREE,
                                          UNIQUE INDEX `idx_sku_batch_no`(`sku_id` ASC, `batch_no` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for w_sku_batch_stock
-- ----------------------------
CREATE TABLE `w_sku_batch_stock`  (
                                      `id` bigint NOT NULL,
                                      `create_time` bigint NOT NULL DEFAULT 0 COMMENT 'Creation time',
                                      `create_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Create user',
                                      `update_time` bigint NOT NULL DEFAULT 0 COMMENT 'Update time',
                                      `update_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Update user',
                                      `available_qty` int NOT NULL DEFAULT 0 COMMENT '可用数量',
                                      `frozen_qty` int NOT NULL DEFAULT 0 COMMENT '冻结数量',
                                      `no_outbound_locked_qty` int NOT NULL DEFAULT 0 COMMENT '库内锁定数量',
                                      `outbound_locked_qty` int NOT NULL DEFAULT 0 COMMENT '出库锁定数量',
                                      `sku_batch_attribute_id` bigint NOT NULL DEFAULT 0 COMMENT '批次ID',
                                      `sku_id` bigint NOT NULL DEFAULT 0 COMMENT 'skuId',
                                      `total_qty` int NOT NULL DEFAULT 0 COMMENT '总数量',
                                      `version` bigint NULL DEFAULT NULL,
                                      `warehouse_area_id` bigint NOT NULL DEFAULT 0 COMMENT '库区ID',
                                      `warehouse_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '仓库编码',
                                      PRIMARY KEY (`id`) USING BTREE,
                                      UNIQUE INDEX `idx_sku_batch_attribute_warehouse_area`(`sku_batch_attribute_id` ASC, `warehouse_area_id` ASC) USING BTREE,
                                      INDEX `idx_sku_id_warehouse_area`(`sku_id` ASC, `warehouse_area_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for w_stock_abnormal_record
-- ----------------------------
CREATE TABLE `w_stock_abnormal_record`  (
                                            `id` bigint NOT NULL,
                                            `create_time` bigint NOT NULL DEFAULT 0 COMMENT 'Creation time',
                                            `create_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Create user',
                                            `update_time` bigint NOT NULL DEFAULT 0 COMMENT 'Update time',
                                            `update_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Update user',
                                            `abnormal_order_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '原始单号',
                                            `container_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '容器编号',
                                            `container_slot_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '容器格口(子容器编号)',
                                            `container_stock_id` bigint NOT NULL COMMENT '库存id',
                                            `location_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '库位编码',
                                            `order_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '异常单号',
                                            `owner_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '货主编码',
                                            `qty_abnormal` int NOT NULL COMMENT '盈亏',
                                            `reason_desc` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '原因描述',
                                            `replay_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '复盘单号-差异复盘 二盘单号',
                                            `sku_batch_attribute_id` bigint NOT NULL DEFAULT 0 COMMENT '批次id',
                                            `sku_batch_stock_id` bigint NOT NULL DEFAULT 0 COMMENT '库存批次id',
                                            `sku_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '商品编码',
                                            `sku_id` bigint NOT NULL DEFAULT 0 COMMENT '商品id',
                                            `stock_abnormal_reason` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '差异原因',
                                            `stock_abnormal_status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '状态',
                                            `stock_abnormal_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '异常类型',
                                            `version` bigint NULL DEFAULT NULL,
                                            `warehouse_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '仓库',
                                            `abnormal_reason` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '差异原因',
                                            PRIMARY KEY (`id`) USING BTREE,
                                            UNIQUE INDEX `uk_order_no`(`order_no` ASC) USING BTREE,
                                            INDEX `idx_container_code`(`container_code` ASC) USING BTREE,
                                            INDEX `idx_container_stock_id`(`container_stock_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for w_stock_adjustment_detail
-- ----------------------------
CREATE TABLE `w_stock_adjustment_detail`  (
                                              `id` bigint NOT NULL,
                                              `create_time` bigint NOT NULL DEFAULT 0 COMMENT 'Creation time',
                                              `create_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Create user',
                                              `update_time` bigint NOT NULL DEFAULT 0 COMMENT 'Update time',
                                              `update_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Update user',
                                              `abnormal_reason` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '异常原因',
                                              `container_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '容器编号',
                                              `container_slot_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '容器格口(子容器编号)',
                                              `container_stock_id` bigint NOT NULL COMMENT '库存id',
                                              `owner_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '货主',
                                              `qty_adjustment` int NOT NULL COMMENT '调整数量',
                                              `sku_batch_attribute_id` bigint NOT NULL DEFAULT 0 COMMENT '批次id',
                                              `sku_batch_stock_id` bigint NOT NULL DEFAULT 0 COMMENT '库存批次id',
                                              `sku_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '商品编码',
                                              `sku_id` bigint NOT NULL DEFAULT 0 COMMENT '商品id',
                                              `stock_abnormal_record_id` bigint NOT NULL DEFAULT 0 COMMENT '库存异常记录ID',
                                              `stock_adjustment_order_id` bigint NOT NULL DEFAULT 0 COMMENT '调整单ID',
                                              PRIMARY KEY (`id`) USING BTREE,
                                              INDEX `idx_stock_adjustment_order_id`(`stock_adjustment_order_id` ASC) USING BTREE,
                                              INDEX `idx_container_stock_id`(`container_stock_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for w_stock_adjustment_order
-- ----------------------------
CREATE TABLE `w_stock_adjustment_order`  (
                                             `id` bigint NOT NULL,
                                             `create_time` bigint NOT NULL DEFAULT 0 COMMENT 'Creation time',
                                             `create_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Create user',
                                             `update_time` bigint NOT NULL DEFAULT 0 COMMENT 'Update time',
                                             `update_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Update user',
                                             `description` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '描述',
                                             `order_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '调整单号',
                                             `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '状态',
                                             `version` bigint NULL DEFAULT NULL,
                                             `warehouse_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '仓库',
                                             PRIMARY KEY (`id`) USING BTREE,
                                             UNIQUE INDEX `uk_order_no`(`order_no` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for w_stocktake_order
-- ----------------------------
CREATE TABLE `w_stocktake_order`  (
                                      `id` bigint NOT NULL,
                                      `create_time` bigint NOT NULL DEFAULT 0 COMMENT 'Creation time',
                                      `create_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Create user',
                                      `update_time` bigint NOT NULL DEFAULT 0 COMMENT 'Update time',
                                      `update_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Update user',
                                      `abnormal` int NOT NULL COMMENT '异常标识',
                                      `customer_order_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '客户订单编号',
                                      `include_empty_slot` bit(1) NOT NULL COMMENT '是否盘点空格口',
                                      `include_zero_stock` bit(1) NOT NULL COMMENT '是否盘点零库存',
                                      `order_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '订单编号',
                                      `stocktake_create_method` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '创建方式',
                                      `stocktake_method` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '盘点方式',
                                      `stocktake_order_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '状态',
                                      `stocktake_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '盘点单类型',
                                      `stocktake_unit_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '创建类型',
                                      `version` bigint NULL DEFAULT NULL,
                                      `warehouse_area_id` bigint NOT NULL COMMENT '库区ID',
                                      `warehouse_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '仓库编码',
                                      `warehouse_logic_id` bigint NULL DEFAULT NULL COMMENT '逻辑区ID',
                                      PRIMARY KEY (`id`) USING BTREE,
                                      UNIQUE INDEX `idx_order_no`(`order_no` ASC) USING BTREE,
                                      INDEX `idx_customer_order_no`(`customer_order_no` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for w_stocktake_order_detail
-- ----------------------------
CREATE TABLE `w_stocktake_order_detail`  (
                                             `id` bigint NOT NULL,
                                             `create_time` bigint NOT NULL DEFAULT 0 COMMENT 'Creation time',
                                             `create_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Create user',
                                             `update_time` bigint NOT NULL DEFAULT 0 COMMENT 'Update time',
                                             `update_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Update user',
                                             `stocktake_order_id` bigint NOT NULL COMMENT '盘点单ID',
                                             `stocktake_unit_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '盘点基本单位类型',
                                             `unit_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '盘点基本单位编码',
                                             `unit_id` bigint NULL DEFAULT NULL COMMENT '盘点基本单位Id：按商品盘点时为SkuId',
                                             `version` bigint NULL DEFAULT NULL,
                                             PRIMARY KEY (`id`) USING BTREE,
                                             INDEX `idx_stocktake_order_id`(`stocktake_order_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for w_stocktake_record
-- ----------------------------
CREATE TABLE `w_stocktake_record`  (
                                       `id` bigint NOT NULL,
                                       `create_time` bigint NOT NULL DEFAULT 0 COMMENT 'Creation time',
                                       `create_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Create user',
                                       `update_time` bigint NOT NULL DEFAULT 0 COMMENT 'Update time',
                                       `update_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Update user',
                                       `abnormal_reason` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '异常原因',
                                       `container_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '容器编码',
                                       `container_face` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '容器面',
                                       `container_id` bigint NOT NULL COMMENT '容器ID',
                                       `container_slot_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '容器格口号',
                                       `qty_original` int NOT NULL COMMENT '原库存数量',
                                       `qty_stocktake` int NOT NULL COMMENT '盘点库存数量',
                                       `sku_batch_attribute_id` bigint NOT NULL COMMENT '批次属性ID',
                                       `sku_batch_stock_id` bigint NOT NULL COMMENT '批次库存ID',
                                       `sku_id` bigint NOT NULL COMMENT 'SKU ID',
                                       `stock_id` bigint NOT NULL COMMENT '库存ID',
                                       `stocktake_order_id` bigint NOT NULL COMMENT '盘点单ID',
                                       `stocktake_record_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '盘点记录状态',
                                       `stocktake_task_detail_id` bigint NOT NULL COMMENT '盘点任务明细ID',
                                       `stocktake_task_id` bigint NOT NULL COMMENT '盘点任务ID',
                                       `version` bigint NULL DEFAULT NULL,
                                       `warehouse_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '仓库编码',
                                       `work_station_id` bigint NULL DEFAULT NULL COMMENT '盘点工作站ID',
                                       PRIMARY KEY (`id`) USING BTREE,
                                       INDEX `idx_stocktake_order_id`(`stocktake_order_id` ASC) USING BTREE,
                                       INDEX `idx_stocktake_task_id`(`stocktake_task_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for w_stocktake_task
-- ----------------------------
CREATE TABLE `w_stocktake_task`  (
                                     `id` bigint NOT NULL,
                                     `create_time` bigint NOT NULL DEFAULT 0 COMMENT 'Creation time',
                                     `create_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Create user',
                                     `update_time` bigint NOT NULL DEFAULT 0 COMMENT 'Update time',
                                     `update_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Update user',
                                     `received_user_id` bigint NULL DEFAULT NULL COMMENT '领单用户id',
                                     `stocktake_create_method` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '创建方式',
                                     `stocktake_method` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '盘点方式',
                                     `stocktake_order_id` bigint NOT NULL COMMENT '盘点单ID',
                                     `stocktake_task_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '状态',
                                     `stocktake_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '盘点单类型',
                                     `stocktake_unit_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '创建类型',
                                     `task_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '任务编号',
                                     `version` bigint NULL DEFAULT NULL,
                                     `warehouse_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '仓库',
                                     `work_station_id` bigint NULL DEFAULT NULL COMMENT '工作站',
                                     PRIMARY KEY (`id`) USING BTREE,
                                     UNIQUE INDEX `idx_task_no`(`task_no` ASC) USING BTREE,
                                     INDEX `idx_stocktake_order_id`(`stocktake_order_id` ASC) USING BTREE,
                                     INDEX `idx_stocktake_task_status`(`stocktake_task_status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for w_stocktake_task_detail
-- ----------------------------
CREATE TABLE `w_stocktake_task_detail`  (
                                            `id` bigint NOT NULL,
                                            `create_time` bigint NOT NULL DEFAULT 0 COMMENT 'Creation time',
                                            `create_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Create user',
                                            `update_time` bigint NOT NULL DEFAULT 0 COMMENT 'Update time',
                                            `update_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Update user',
                                            `container_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '容器编码',
                                            `container_face` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '容器面',
                                            `stocktake_order_id` bigint NOT NULL COMMENT '盘点单ID',
                                            `stocktake_task_detail_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '盘点任务明细状态',
                                            `stocktake_task_id` bigint NOT NULL COMMENT '盘点任务ID',
                                            `version` bigint NULL DEFAULT NULL,
                                            `warehouse_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '仓库',
                                            PRIMARY KEY (`id`) USING BTREE,
                                            INDEX `idx_stocktake_task_id`(`stocktake_task_id` ASC) USING BTREE,
                                            INDEX `idx_stocktake_order_id`(`stocktake_order_id` ASC) USING BTREE,
                                            INDEX `idx_container_code_face`(`container_code` ASC, `container_face` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for w_transfer_container
-- ----------------------------
CREATE TABLE `w_transfer_container`  (
                                         `id` bigint NOT NULL,
                                         `create_time` bigint NOT NULL DEFAULT 0 COMMENT 'Creation time',
                                         `create_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Create user',
                                         `update_time` bigint NOT NULL DEFAULT 0 COMMENT 'Update time',
                                         `update_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Update user',
                                         `container_spec_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '周转容器规格',
                                         `current_period_relate_record_ids` json NULL COMMENT '表示一个周期内关联的周转容器记录(TransferContainerRecord)',
                                         `location_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '点位编码',
                                         `locked_time` bigint NOT NULL DEFAULT 0 COMMENT '锁定时间',
                                         `transfer_container_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '周转容器编码',
                                         `transfer_container_status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '状态',
                                         `version` bigint NULL DEFAULT NULL,
                                         `virtual_container` bit(1) NOT NULL,
                                         `warehouse_area_id` bigint NOT NULL COMMENT '最后一次工作的库区',
                                         `warehouse_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '仓库编号',
                                         PRIMARY KEY (`id`) USING BTREE,
                                         UNIQUE INDEX `uk_container_code_warehouse`(`transfer_container_code` ASC, `warehouse_code` ASC) USING BTREE,
                                         INDEX `idx_update_time`(`update_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for w_transfer_container_record
-- ----------------------------
CREATE TABLE `w_transfer_container_record`  (
                                                `id` bigint NOT NULL,
                                                `create_time` bigint NOT NULL DEFAULT 0 COMMENT 'Creation time',
                                                `create_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Create user',
                                                `update_time` bigint NOT NULL DEFAULT 0 COMMENT 'Update time',
                                                `update_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Update user',
                                                `container_index` int NOT NULL COMMENT '第几个周转箱',
                                                `destination` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '周转箱目的地',
                                                `picking_order_id` bigint NOT NULL COMMENT '拣选订单ID',
                                                `put_wall_slot_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '播种墙格口',
                                                `seal_time` bigint NOT NULL DEFAULT 0 COMMENT '封箱时间',
                                                `transfer_container_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '周转容器编码',
                                                `transfer_container_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '周转箱记录状态',
                                                `version` bigint NULL DEFAULT NULL,
                                                `warehouse_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '仓库编码',
                                                `work_station_id` bigint NULL DEFAULT NULL COMMENT '工作站ID',
                                                PRIMARY KEY (`id`) USING BTREE,
                                                UNIQUE INDEX `uk_container_order_order_status`(`transfer_container_code` ASC, `picking_order_id` ASC, `seal_time` ASC) USING BTREE,
                                                INDEX `idx_order`(`picking_order_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for w_warehouse_area
-- ----------------------------
CREATE TABLE `w_warehouse_area`  (
                                     `id` bigint NOT NULL,
                                     `create_time` bigint NOT NULL DEFAULT 0 COMMENT 'Creation time',
                                     `create_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Create user',
                                     `update_time` bigint NOT NULL DEFAULT 0 COMMENT 'Update time',
                                     `update_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Update user',
                                     `delete_time` bigint NOT NULL DEFAULT 0 COMMENT '删除时间',
                                     `deleted` bit(1) NOT NULL,
                                     `enable` bit(1) NOT NULL,
                                     `level` int NOT NULL,
                                     `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
                                     `temperature_limit` int NOT NULL,
                                     `version` bigint NOT NULL,
                                     `warehouse_area_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '库区编码',
                                     `warehouse_area_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '库区名称',
                                     `warehouse_area_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '库区类型',
                                     `warehouse_area_use` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '库区用途',
                                     `warehouse_area_work_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '库区工作类型',
                                     `warehouse_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '仓库编码',
                                     `warehouse_group_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '仓区编码',
                                     `wet_limit` int NOT NULL,
                                     PRIMARY KEY (`id`) USING BTREE,
                                     UNIQUE INDEX `uk_warehouse_area_group_code`(`warehouse_area_code` ASC, `warehouse_group_code` ASC, `warehouse_code` ASC, `delete_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for w_warehouse_area_group
-- ----------------------------
CREATE TABLE `w_warehouse_area_group`  (
                                           `id` bigint NOT NULL,
                                           `create_time` bigint NOT NULL DEFAULT 0 COMMENT 'Creation time',
                                           `create_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Create user',
                                           `update_time` bigint NOT NULL DEFAULT 0 COMMENT 'Update time',
                                           `update_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Update user',
                                           `delete_time` bigint NOT NULL DEFAULT 0 COMMENT '删除时间',
                                           `deleted` bit(1) NOT NULL,
                                           `enable` bit(1) NOT NULL,
                                           `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
                                           `version` bigint NOT NULL,
                                           `warehouse_area_group_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '仓区编码',
                                           `warehouse_area_group_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '仓区名称',
                                           `warehouse_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '仓库编码',
                                           PRIMARY KEY (`id`) USING BTREE,
                                           UNIQUE INDEX `uk_warehouse_area_group_code`(`warehouse_area_group_code` ASC, `warehouse_code` ASC, `delete_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for w_warehouse_config
-- ----------------------------
CREATE TABLE `w_warehouse_config`  (
                                       `id` bigint NOT NULL,
                                       `create_time` bigint NOT NULL DEFAULT 0 COMMENT 'Creation time',
                                       `create_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Create user',
                                       `update_time` bigint NOT NULL DEFAULT 0 COMMENT 'Update time',
                                       `update_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Update user',
                                       `version` bigint NOT NULL,
                                       `warehouse_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
                                       `warehouse_main_data_config` json NULL,
                                       PRIMARY KEY (`id`) USING BTREE,
                                       UNIQUE INDEX `uk_warehouse_code`(`warehouse_code` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for w_warehouse_logic
-- ----------------------------
CREATE TABLE `w_warehouse_logic`  (
                                      `id` bigint NOT NULL,
                                      `create_time` bigint NOT NULL DEFAULT 0 COMMENT 'Creation time',
                                      `create_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Create user',
                                      `update_time` bigint NOT NULL DEFAULT 0 COMMENT 'Update time',
                                      `update_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Update user',
                                      `delete_time` bigint NOT NULL DEFAULT 0 COMMENT '备注',
                                      `deleted` bit(1) NOT NULL,
                                      `enable` bit(1) NOT NULL,
                                      `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
                                      `version` bigint NOT NULL,
                                      `warehouse_area_id` bigint NOT NULL DEFAULT 0 COMMENT '库区ID',
                                      `warehouse_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '仓库编码',
                                      `warehouse_logic_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '逻辑区编码',
                                      `warehouse_logic_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '逻辑区名称',
                                      PRIMARY KEY (`id`) USING BTREE,
                                      UNIQUE INDEX `uk_warehouse_logic_area`(`warehouse_area_id` ASC, `warehouse_logic_code` ASC, `delete_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for w_work_station
-- ----------------------------
CREATE TABLE `w_work_station`  (
                                   `id` bigint NOT NULL,
                                   `create_time` bigint NOT NULL DEFAULT 0 COMMENT 'Creation time',
                                   `create_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Create user',
                                   `update_time` bigint NOT NULL DEFAULT 0 COMMENT 'Update time',
                                   `update_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Update user',
                                   `allow_work_station_modes` json NULL COMMENT '工作站允许的操作',
                                   `delete_time` bigint NOT NULL DEFAULT 0 COMMENT '删除时间',
                                   `deleted` bit(1) NOT NULL,
                                   `enable` bit(1) NOT NULL,
                                   `position` json NULL COMMENT '工作站位置',
                                   `station_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '工作站编码',
                                   `station_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '工作站编码',
                                   `version` bigint NULL DEFAULT NULL,
                                   `warehouse_area_id` bigint NOT NULL COMMENT '库区ID',
                                   `warehouse_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '仓库编码',
                                   `work_locations` json NULL COMMENT '工作站工作位',
                                   `work_station_mode` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作类型',
                                   `work_station_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '状态',
                                   PRIMARY KEY (`id`) USING BTREE,
                                   UNIQUE INDEX `uk_station_code_warehouse`(`station_code` ASC, `warehouse_code` ASC, `delete_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for w_work_station_config
-- ----------------------------
CREATE TABLE `w_work_station_config`  (
                                          `id` bigint NOT NULL,
                                          `create_time` bigint NOT NULL DEFAULT 0 COMMENT 'Creation time',
                                          `create_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Create user',
                                          `update_time` bigint NOT NULL DEFAULT 0 COMMENT 'Update time',
                                          `update_user` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Update user',
                                          `inbound_station_config` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '入库工作站配置',
                                          `picking_station_config` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '拣货工作站配置',
                                          `relocation_station_config` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '理库工作站配置',
                                          `stocktake_station_config` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '盘点工作站配置',
                                          `version` bigint NULL DEFAULT NULL,
                                          `work_station_id` bigint NOT NULL COMMENT '工作站ID',
                                          PRIMARY KEY (`id`) USING BTREE,
                                          UNIQUE INDEX `uk_work_station_id`(`work_station_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

SET FOREIGN_KEY_CHECKS = 1;
