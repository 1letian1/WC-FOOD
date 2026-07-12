CREATE DATABASE IF NOT EXISTS shike_ordering CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE shike_ordering;

CREATE TABLE IF NOT EXISTS shop (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  name VARCHAR(100) NOT NULL COMMENT '店铺名称',
  logo_url VARCHAR(500) NULL COMMENT '店铺Logo URL',
  phone VARCHAR(20) NOT NULL COMMENT '联系电话',
  address VARCHAR(255) NOT NULL COMMENT '店铺地址',
  notice VARCHAR(500) NULL COMMENT '店铺公告',
  business_hours VARCHAR(255) NOT NULL COMMENT '营业时间展示文本',
  business_status TINYINT NOT NULL DEFAULT 0 COMMENT '营业状态：0休息，1营业',
  dine_in_enabled TINYINT NOT NULL DEFAULT 1 COMMENT '堂食是否开放',
  delivery_enabled TINYINT NOT NULL DEFAULT 1 COMMENT '配送是否开放',
  delivery_fee DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '配送费',
  min_delivery_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '起送金额',
  package_fee DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '包装费',
  delivery_range VARCHAR(500) NULL COMMENT '配送范围文字说明',
  estimated_delivery_minutes INT UNSIGNED NULL COMMENT '预计配送分钟数',
  version INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  create_time DATETIME(3) NOT NULL COMMENT '创建时间',
  update_time DATETIME(3) NOT NULL COMMENT '更新时间',
  PRIMARY KEY (id),
  CONSTRAINT chk_shop_business_status CHECK (business_status IN (0, 1)),
  CONSTRAINT chk_shop_dine_in_enabled CHECK (dine_in_enabled IN (0, 1)),
  CONSTRAINT chk_shop_delivery_enabled CHECK (delivery_enabled IN (0, 1)),
  CONSTRAINT chk_shop_delivery_fee CHECK (delivery_fee >= 0),
  CONSTRAINT chk_shop_min_delivery_amount CHECK (min_delivery_amount >= 0),
  CONSTRAINT chk_shop_package_fee CHECK (package_fee >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='店铺';

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
