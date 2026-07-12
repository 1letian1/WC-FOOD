CREATE DATABASE IF NOT EXISTS shike_ordering CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE shike_ordering;
-- 基础里程碑示范表，业务表按 docs/DATABASE_DESIGN.md 后续分阶段加入。
CREATE TABLE IF NOT EXISTS system_config (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  config_key VARCHAR(100) NOT NULL COMMENT '配置键',
  config_value VARCHAR(500) NULL COMMENT '配置值',
  description VARCHAR(255) NULL COMMENT '说明',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  version INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  create_time DATETIME(3) NOT NULL COMMENT '创建时间',
  update_time DATETIME(3) NOT NULL COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_system_config_key_deleted (config_key, deleted),
  KEY idx_system_config_update_time (update_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统配置示范表';
