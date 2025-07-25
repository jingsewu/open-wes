-- MySQL dump 10.13  Distrib 8.0.18, for Linux (x86_64)
--
-- Host: localhost    Database: nacos_config
-- ------------------------------------------------------
-- Server version	8.0.18

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `config_info`
--
use nacos_config;

DROP TABLE IF EXISTS `config_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `config_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `data_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT 'data_id',
  `group_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `content` longtext CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT 'content',
  `md5` varchar(32) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL COMMENT 'md5',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  `src_user` text CHARACTER SET utf8 COLLATE utf8_bin COMMENT 'source user',
  `src_ip` varchar(50) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL COMMENT 'source ip',
  `app_name` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `tenant_id` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT '' COMMENT '租户字段',
  `c_desc` varchar(256) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `c_use` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `effect` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `type` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `c_schema` text CHARACTER SET utf8 COLLATE utf8_bin,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_configinfo_datagrouptenant` (`data_id`,`group_id`,`tenant_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=5770 DEFAULT CHARSET=utf8 COLLATE=utf8_bin ROW_FORMAT=DYNAMIC COMMENT='config_info';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `config_info`
--

LOCK TABLES `config_info` WRITE;
/*!40000 ALTER TABLE `config_info` DISABLE KEYS */;
INSERT INTO `config_info` VALUES (2923,'station','openwes','spring:  \n  cloud:\n    nacos:\n      discovery:\n        server-addr: nacos.openwes.com:8848\n        namespace: test\n        username: nacos\n        password: nacos\n        group: openwes\n        \n  liquibase:\n    change-log: classpath*:liquibase.xml\n    database-change-log-table: lqb_change_log\n    database-change-log-lock-table: lqb_change_log_lock\n\n  jackson:\n    date-format: \'yyyy-MM-dd HH:mm:ss\'\n    time-zone: \'Asia/Shanghai\'\n    # JsonInclude.Include\n    default-property-inclusion: NON_NULL\n\n  servlet:\n    multipart:\n      max-file-size: 20MB\n      max-request-size: 20MB\n\n  redis:\n    redisson:\n      config: |\n        singleServerConfig:\n          idleConnectionTimeout: 100000\n          connectTimeout: 10000\n          timeout: 3000\n          retryAttempts: 3\n          retryInterval: 1500\n          password: null\n          subscriptionsPerConnection: 5\n          clientName: null\n          address: redis://redis.openwes.com:6379\n          subscriptionConnectionMinimumIdleSize: 1\n          subscriptionConnectionPoolSize: 50\n          connectionMinimumIdleSize: 32\n          connectionPoolSize: 64\n          database: 3\n          dnsMonitoringInterval: 5000\n        threads: 0\n        nettyThreads: 0\n        codec:\n          class: \"org.redisson.codec.JsonJacksonCodec\"\n        transportMode: \"NIO\"\n        lockWatchdogTimeout: 10000\n\n\nserver:\n  max-http-header-size: 1024000\n\nlogging:\n    config: classpath:logback-spring.xml\n    max-file-size: 250MB\n    max-history: 168 \n    total-size-cap: 50GB\n\ndubbo:\n  application:\n    id: station\n    name: station\n    serialize-check-status: WARN    \n  consumer:\n    check: false\n  scan:\n    base-packages: org.openwes\n  protocol:\n    name: dubbo\n    port: -1\n  registry:\n    address: nacos://nacos.openwes.com:8848\n    timeout: 10000\n','b9d93e34448b255fc5429368c1440b2e','2024-11-29 13:52:03','2024-12-08 02:59:41',NULL,'223.74.150.111','','test','','','','yaml',''),(2924,'gateway','openwes','spring:\n  cloud:\n    nacos:\n      discovery:\n        server-addr: nacos.openwes.com:8848\n        namespace: test\n        username: nacos\n        password: nacos\n        group: openwes\n    gateway:\n      httpclient:\n        websocket:\n          max-frame-payload-length: 10240000\n      routes:\n        - id: user\n          uri: lb://wes\n          predicates:\n            - Path=/user/**\n          filters:\n            - RewritePath=/user/(?<segment>.*) , /$\\{segment}\n        - id: ai\n          uri: lb://wes\n          predicates:\n            - Path=/ai/**\n          filters:\n            - RewritePath=/ai/(?<segment>.*) , /$\\{segment}    \n        - id: mdm\n          uri: lb://wes\n          predicates:\n            - Path=/mdm/**\n          filters:\n            - RewritePath=/mdm/(?<segment>.*) , /$\\{segment}\n        - id: wms\n          uri: lb://wes\n          predicates:\n            - Path=/wms/**\n          filters:\n            - RewritePath=/wms/(?<segment>.*) , /$\\{segment}\n        - id: search\n          uri: lb://wes\n          predicates:\n            - Path=/search/**\n          filters:\n            - RewritePath=/search/(?<segment>.*) , /$\\{segment}\n        - id: plugin\n          uri: lb://plugin\n          predicates:\n            - Path=/plugin/**\n          filters:\n            - RewritePath=/plugin/(?<segment>.*) , /$\\{segment}\n        - id: api-platform\n          uri: lb://wes\n          predicates:\n            - Path=/api-platform/**\n          filters:\n            - RewritePath=/api-platform/(?<segment>.*) , /$\\{segment}\n        - id: station\n          uri: lb://station\n          predicates:\n            - Path=/station/**\n          filters:\n            - RewritePath=/station/(?<segment>.*) , /$\\{segment}  \n  jackson:\n    date-format: yyyy-MM-dd HH:mm:ss\n    time-zone: Asia/Shanghai\n    default-property-inclusion: NON_NULL\n  servlet:\n    multipart:\n      max-file-size: 20MB\n      max-request-size: 20MB\n  redis:\n    redisson:\n      config: |\n        singleServerConfig:\n          idleConnectionTimeout: 100000\n          connectTimeout: 10000\n          timeout: 3000\n          retryAttempts: 3\n          retryInterval: 1500\n          password: null\n          subscriptionsPerConnection: 5\n          clientName: null\n          address: redis://redis.openwes.com:6379\n          subscriptionConnectionMinimumIdleSize: 1\n          subscriptionConnectionPoolSize: 50\n          connectionMinimumIdleSize: 32\n          connectionPoolSize: 64\n          database: 3\n          dnsMonitoringInterval: 5000\n        threads: 0\n        nettyThreads: 0\n        codec:\n          class: \"org.redisson.codec.JsonJacksonCodec\"\n        transportMode: \"NIO\"\n        lockWatchdogTimeout: 10000\nserver:\n  max-http-header-size: 1024000\nlogging:\n  config: classpath:logback-spring.xml\n  max-file-size: 250MB\n  max-history: 168\n  total-size-cap: 50GB\n\ndubbo:\n  application:\n    id: gateway\n    name: gateway\n  consumer:\n    check: false\n  scan:\n    base-packages: com.openwes\n  protocol:\n    name: dubbo\n    port: -1\n  registry:\n    address: nacos://nacos.openwes.com:8848\n    timeout: 10000\n  cloud:\n    subscribed-services: mdm\n','c6813634518b693ca0e1d5e8cc2ce841','2024-11-29 13:53:00','2025-06-02 09:01:24',NULL,'192.168.247.1','','test','','','','yaml',''),(2925,'wes','openwes','spring:  \n  cloud:\n    nacos:\n      discovery:\n        server-addr: nacos.openwes.com:8848\n        namespace: test\n        username: nacos\n        password: nacos\n        group: openwes\n  security:\n    oauth2:\n      client:\n        registration:\n          azure:\n            client-id: 05c43694-8873-4c8f-80c3-bb4c49779e9c\n            client-secret: 0AS8Q~Yh82lKBs0WgNZTvgzz~Jl0C_Voz2bAHcXn\n            scope: openid, email, profile\n            authorization-grant-type: authorization_code\n            redirect-uri: \"{baseUrl}/login/oauth2/code/{registrationId}\"\n            client-name: Azure\n        provider:\n          azure:\n            authorization-uri: https://login.microsoftonline.com/27a5f62e-7c13-44ba-b556-fad637f7e476/oauth2/v2.0/authorize\n            token-uri: https://login.microsoftonline.com/27a5f62e-7c13-44ba-b556-fad637f7e476/oauth2/v2.0/token\n            user-info-uri: https://graph.microsoft.com/oidc/userinfo\n            user-name-attribute: name\n            jwk-set-uri: https://login.microsoftonline.com/common/discovery/keys\n  ai:\n    zhipuai:\n      api-key: a8abc75289aa75f62753d9b798cdc50d.86MFpDnMx71Zcm8F\n      chat.options.model: GLM-4-air\n      chat.options.temperature: 0\n\n  jackson:\n    date-format: \'yyyy-MM-dd HH:mm:ss\'\n    time-zone: \'Asia/Shanghai\'\n    # JsonInclude.Include\n    default-property-inclusion: NON_NULL\n\n  servlet:\n    multipart:\n      max-file-size: 20MB\n      max-request-size: 20MB\n\n  redis:\n    redisson:\n      config: |\n        singleServerConfig:\n          idleConnectionTimeout: 100000\n          connectTimeout: 10000\n          timeout: 3000\n          retryAttempts: 3\n          retryInterval: 1500\n          password: null\n          subscriptionsPerConnection: 5\n          clientName: null\n          address: redis://redis.openwes.com:6379\n          subscriptionConnectionMinimumIdleSize: 1\n          subscriptionConnectionPoolSize: 50\n          connectionMinimumIdleSize: 32\n          connectionPoolSize: 64\n          database: 3\n          dnsMonitoringInterval: 5000\n        threads: 0\n        nettyThreads: 0\n        codec:\n          class: \"org.redisson.codec.JsonJacksonCodec\"\n        transportMode: \"NIO\"\n        lockWatchdogTimeout: 10000\n\n  datasource:\n    driverClassName: com.mysql.cj.jdbc.Driver\n    type: com.zaxxer.hikari.HikariDataSource\n    url: jdbc:mysql://mysql.openwes.com:3306/openwes?useSSL=false&serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=utf8&allowPublicKeyRetrieval=true\n    username: root\n    password: root\n    hikari:\n      minimum-idle: 5\n      idle-timeout: 180000\n      maximum-pool-size: 100\n      auto-commit: true\n      pool-name: ${spring.application.name}_datasource_pool\n      max-lifetime: 1800000\n      connection-timeout: 30000\n      connection-test-query: SELECT 1\n\n  jpa:\n    database: mysql\n    show-sql: false\n    generate-ddl: true\n    hibernate:\n      ddl-auto: none\n    properties:\n      hibernate:\n        format_sql: true\n        use_sql_comments: true\n\nserver:\n  max-http-header-size: 1024000\n\nlogging:\n    config: classpath:logback-spring.xml\n    max-file-size: 250MB\n    max-history: 168 \n    total-size-cap: 50GB\n\ndubbo:\n  application:\n    id: wms\n    name: wms\n    serialize-check-status: WARN    \n    qos-enable: false     \n  consumer:\n    check: false\n    filter: -exception\n    timeout: 3000\n  provider:\n    filter: -exception  \n    timeout: 5000\n  scan:\n    base-packages: org.openwes\n  protocol:\n    name: dubbo\n    port: -1\n    threadpool: fixed \n    threads: 500      \n  registry:\n    address: nacos://nacos.openwes.com:8848\n    timeout: 10000','5f84aff80972fffe2c7dfed97e995640','2024-11-29 13:53:56','2025-02-24 03:09:21',NULL,'59.39.221.30','','test','','','','yaml',''),(4245,'ai','openwes','spring:  \n  cloud:\n    nacos:\n      discovery:\n        server-addr: nacos.openwes.com:8848\n        namespace: test\n        username: nacos\n        password: nacos\n        group: openwes\n  ai:\n    zhipuai:\n      api-key: a8abc75289aa75f62753d9b798cdc50d.86MFpDnMx71Zcm8F\n      chat.options.model: GLM-4-air\n      chat.options.temperature: 0\n                  \n  jackson:\n    date-format: \'yyyy-MM-dd HH:mm:ss\'\n    time-zone: \'Asia/Shanghai\'\n    # JsonInclude.Include\n    default-property-inclusion: NON_NULL\n\n  servlet:\n    multipart:\n      max-file-size: 20MB\n      max-request-size: 20MB\n\n  redis:\n    redisson:\n      config: |\n        singleServerConfig:\n          idleConnectionTimeout: 100000\n          connectTimeout: 10000\n          timeout: 3000\n          retryAttempts: 3\n          retryInterval: 1500\n          password: null\n          subscriptionsPerConnection: 5\n          clientName: null\n          address: redis://redis.openwes.com:6379\n          subscriptionConnectionMinimumIdleSize: 1\n          subscriptionConnectionPoolSize: 50\n          connectionMinimumIdleSize: 32\n          connectionPoolSize: 64\n          database: 3\n          dnsMonitoringInterval: 5000\n        threads: 0\n        nettyThreads: 0\n        codec:\n          class: \"org.redisson.codec.JsonJacksonCodec\"\n        transportMode: \"NIO\"\n        lockWatchdogTimeout: 10000\n\n  datasource:\n    driverClassName: com.mysql.cj.jdbc.Driver\n    type: com.zaxxer.hikari.HikariDataSource\n    url: jdbc:mysql://mysql.openwes.com:3306/openwes?useSSL=false&serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=utf8&allowPublicKeyRetrieval=true\n    username: root\n    password: root\n    hikari:\n      minimum-idle: 5\n      idle-timeout: 180000\n      maximum-pool-size: 100\n      auto-commit: true\n      pool-name: ${spring.application.name}_datasource_pool\n      max-lifetime: 1800000\n      connection-timeout: 30000\n      connection-test-query: SELECT 1\n\n  jpa:\n    database: mysql\n    show-sql: false\n    generate-ddl: true\n    hibernate:\n      ddl-auto: none\n    properties:\n      hibernate:\n        format_sql: true\n        use_sql_comments: true\n\nserver:\n  max-http-header-size: 1024000\n\nlogging:\n    config: classpath:logback-spring.xml\n    max-file-size: 250MB\n    max-history: 168 \n    total-size-cap: 50GB\n\ndubbo:\n  application:\n    id: ai\n    name: ai\n    serialize-check-status: WARN        \n    qos-enable: false    \n  consumer:\n    check: false\n    filter: -exception\n    timeout: 3000\n  provider:\n    filter: -exception\n    timeout: 5000          \n  scan:\n    base-packages: org.openwes\n  protocol:\n    name: dubbo\n    port: -1\n  registry:\n    address: nacos://nacos.openwes.com:8848\n    timeout: 10000\n','2315483c66b3933055a0fccd89f5c5a9','2025-01-08 03:58:32','2025-02-24 03:09:07',NULL,'59.39.221.30','','test','','','','yaml','');
/*!40000 ALTER TABLE `config_info` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `config_info_aggr`
--

DROP TABLE IF EXISTS `config_info_aggr`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `config_info_aggr` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `data_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT 'data_id',
  `group_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT 'group_id',
  `datum_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT 'datum_id',
  `content` longtext CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT '内容',
  `gmt_modified` datetime NOT NULL COMMENT '修改时间',
  `app_name` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `tenant_id` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT '' COMMENT '租户字段',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_configinfoaggr_datagrouptenantdatum` (`data_id`,`group_id`,`tenant_id`,`datum_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin ROW_FORMAT=DYNAMIC COMMENT='增加租户字段';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `config_info_aggr`
--

LOCK TABLES `config_info_aggr` WRITE;
/*!40000 ALTER TABLE `config_info_aggr` DISABLE KEYS */;
/*!40000 ALTER TABLE `config_info_aggr` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `config_info_beta`
--

DROP TABLE IF EXISTS `config_info_beta`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `config_info_beta` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `data_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT 'data_id',
  `group_id` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT 'group_id',
  `app_name` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL COMMENT 'app_name',
  `content` longtext CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT 'content',
  `beta_ips` varchar(1024) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL COMMENT 'betaIps',
  `md5` varchar(32) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL COMMENT 'md5',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  `src_user` text CHARACTER SET utf8 COLLATE utf8_bin COMMENT 'source user',
  `src_ip` varchar(50) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL COMMENT 'source ip',
  `tenant_id` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT '' COMMENT '租户字段',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_configinfobeta_datagrouptenant` (`data_id`,`group_id`,`tenant_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin ROW_FORMAT=DYNAMIC COMMENT='config_info_beta';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `config_info_beta`
--

LOCK TABLES `config_info_beta` WRITE;
/*!40000 ALTER TABLE `config_info_beta` DISABLE KEYS */;
/*!40000 ALTER TABLE `config_info_beta` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `config_info_tag`
--

DROP TABLE IF EXISTS `config_info_tag`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `config_info_tag` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `data_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT 'data_id',
  `group_id` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT 'group_id',
  `tenant_id` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT '' COMMENT 'tenant_id',
  `tag_id` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT 'tag_id',
  `app_name` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL COMMENT 'app_name',
  `content` longtext CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT 'content',
  `md5` varchar(32) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL COMMENT 'md5',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  `src_user` text CHARACTER SET utf8 COLLATE utf8_bin COMMENT 'source user',
  `src_ip` varchar(50) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL COMMENT 'source ip',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_configinfotag_datagrouptenanttag` (`data_id`,`group_id`,`tenant_id`,`tag_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin ROW_FORMAT=DYNAMIC COMMENT='config_info_tag';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `config_info_tag`
--

LOCK TABLES `config_info_tag` WRITE;
/*!40000 ALTER TABLE `config_info_tag` DISABLE KEYS */;
/*!40000 ALTER TABLE `config_info_tag` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `config_tags_relation`
--

DROP TABLE IF EXISTS `config_tags_relation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `config_tags_relation` (
  `id` bigint(20) NOT NULL COMMENT 'id',
  `tag_name` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT 'tag_name',
  `tag_type` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL COMMENT 'tag_type',
  `data_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT 'data_id',
  `group_id` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT 'group_id',
  `tenant_id` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT '' COMMENT 'tenant_id',
  `nid` bigint(20) NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`nid`) USING BTREE,
  UNIQUE KEY `uk_configtagrelation_configidtag` (`id`,`tag_name`,`tag_type`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin ROW_FORMAT=DYNAMIC COMMENT='config_tag_relation';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `config_tags_relation`
--

LOCK TABLES `config_tags_relation` WRITE;
/*!40000 ALTER TABLE `config_tags_relation` DISABLE KEYS */;
/*!40000 ALTER TABLE `config_tags_relation` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `group_capacity`
--

DROP TABLE IF EXISTS `group_capacity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `group_capacity` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `group_id` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT 'Group ID，空字符表示整个集群',
  `quota` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '配额，0表示使用默认值',
  `usage` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '使用量',
  `max_size` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '单个配置大小上限，单位为字节，0表示使用默认值',
  `max_aggr_count` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '聚合子配置最大个数，，0表示使用默认值',
  `max_aggr_size` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '单个聚合数据的子配置大小上限，单位为字节，0表示使用默认值',
  `max_history_count` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '最大变更历史数量',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_group_id` (`group_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin ROW_FORMAT=DYNAMIC COMMENT='集群、各Group容量信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `group_capacity`
--

LOCK TABLES `group_capacity` WRITE;
/*!40000 ALTER TABLE `group_capacity` DISABLE KEYS */;
/*!40000 ALTER TABLE `group_capacity` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `his_config_info`
--

DROP TABLE IF EXISTS `his_config_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `his_config_info` (
  `id` bigint(20) unsigned NOT NULL,
  `nid` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `data_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `group_id` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `app_name` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL COMMENT 'app_name',
  `content` longtext CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `md5` varchar(32) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `src_user` text CHARACTER SET utf8 COLLATE utf8_bin,
  `src_ip` varchar(50) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `op_type` char(10) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `tenant_id` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT '' COMMENT '租户字段',
  PRIMARY KEY (`nid`) USING BTREE,
  KEY `idx_gmt_create` (`gmt_create`) USING BTREE,
  KEY `idx_gmt_modified` (`gmt_modified`) USING BTREE,
  KEY `idx_did` (`data_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=10698 DEFAULT CHARSET=utf8 COLLATE=utf8_bin ROW_FORMAT=DYNAMIC COMMENT='多租户改造';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `permissions`
--

DROP TABLE IF EXISTS `permissions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `permissions` (
  `role` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `resource` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `action` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  UNIQUE KEY `uk_role_permission` (`role`,`resource`,`action`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `permissions`
--

LOCK TABLES `permissions` WRITE;
/*!40000 ALTER TABLE `permissions` DISABLE KEYS */;
/*!40000 ALTER TABLE `permissions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `roles`
--

DROP TABLE IF EXISTS `roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `roles` (
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `role` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  UNIQUE KEY `idx_user_role` (`username`,`role`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `roles`
--

LOCK TABLES `roles` WRITE;
/*!40000 ALTER TABLE `roles` DISABLE KEYS */;
INSERT INTO `roles` VALUES ('nacos','ROLE_ADMIN');
/*!40000 ALTER TABLE `roles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tenant_capacity`
--

DROP TABLE IF EXISTS `tenant_capacity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tenant_capacity` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT 'Tenant ID',
  `quota` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '配额，0表示使用默认值',
  `usage` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '使用量',
  `max_size` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '单个配置大小上限，单位为字节，0表示使用默认值',
  `max_aggr_count` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '聚合子配置最大个数',
  `max_aggr_size` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '单个聚合数据的子配置大小上限，单位为字节，0表示使用默认值',
  `max_history_count` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '最大变更历史数量',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_tenant_id` (`tenant_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin ROW_FORMAT=DYNAMIC COMMENT='租户容量信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tenant_capacity`
--

LOCK TABLES `tenant_capacity` WRITE;
/*!40000 ALTER TABLE `tenant_capacity` DISABLE KEYS */;
/*!40000 ALTER TABLE `tenant_capacity` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tenant_info`
--

DROP TABLE IF EXISTS `tenant_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tenant_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `kp` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT 'kp',
  `tenant_id` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT '' COMMENT 'tenant_id',
  `tenant_name` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT '' COMMENT 'tenant_name',
  `tenant_desc` varchar(256) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL COMMENT 'tenant_desc',
  `create_source` varchar(32) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL COMMENT 'create_source',
  `gmt_create` bigint(20) NOT NULL COMMENT '创建时间',
  `gmt_modified` bigint(20) NOT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_tenant_info_kptenantid` (`kp`,`tenant_id`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8 COLLATE=utf8_bin ROW_FORMAT=DYNAMIC COMMENT='tenant_info';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tenant_info`
--

LOCK TABLES `tenant_info` WRITE;
/*!40000 ALTER TABLE `tenant_info` DISABLE KEYS */;
INSERT INTO `tenant_info` VALUES (6,'1','test','test','test','nacos',1678756828142,1678756828142);
/*!40000 ALTER TABLE `tenant_info` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `password` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `enabled` tinyint(1) NOT NULL,
  PRIMARY KEY (`username`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES ('2tWP61hsKAZmHTmCHilN80VNpsC','$2a$10$/7InAt7G2DF73CqR9KhNK./BcQbds5GZaXkXUJNZWqcCLiIe6jIBC',1),('FS2C3G','$2a$10$bL3bYucZz8Rqmm/k.5ww2Ov17xMrDCnIYn3j8T3vsBzP28.R6nOkS',1),('nacos','$2a$10$EuWPZHzz32dJN7jexM34MOeYirDdFAZm2kuWj7VEOJhhZkDrxfvUu',1),('nacos_new_user','$2a$10$3zo/j1XTQOuLnvz8qly0feCVpCFnO4NlBbd00LGVQGOxp0Pkm4QrG',1),('test123','$2a$10$m73jdXTD9lPNgRM67SlDoedSs73go2al6JWyCkv1IC881ViKfUcP6',1);
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-03-05 16:32:22
