USE shike_ordering;

INSERT INTO shop (
  id, name, logo_url, phone, address, notice, business_hours, business_status,
  dine_in_enabled, delivery_enabled, delivery_fee, min_delivery_amount, package_fee,
  delivery_range, estimated_delivery_minutes, version, create_time, update_time
) VALUES (
  1, '食刻小馆', NULL, '13800000000', '请在部署前配置真实店铺地址',
  '欢迎光临食刻小馆', '09:00-22:00', 1, 1, 1, 3.00, 20.00, 0.00,
  '商家配送，具体范围请电话咨询', 30, 0, NOW(3), NOW(3)
) ON DUPLICATE KEY UPDATE update_time = NOW(3);

-- 仅供本地开发的商家账号：admin_demo / ShikeTest123，生产环境必须删除或替换。
INSERT INTO merchant_account (
  id, shop_id, username, password_hash, password_algorithm, merchant_name,
  avatar_url, role, status, session_version, last_login_time, create_time, update_time
) VALUES (
  1, 1, 'admin_demo', '$2a$12$ZvetzNu2uLocpXUgL/NmZ.b6T7UUs1L/Z53rUmFrG7.szkEVijcvW',
  'BCRYPT', '测试商家', NULL, 'MERCHANT', 1, 1, NULL, NOW(3), NOW(3)
) ON DUPLICATE KEY UPDATE update_time = NOW(3);

INSERT INTO category (id, shop_id, name, status, sort, deleted, create_time, update_time) VALUES
  (1, 1, '热门推荐', 1, 10, 0, NOW(3), NOW(3)),
  (2, 1, '主食', 1, 20, 0, NOW(3), NOW(3)),
  (3, 1, '小吃', 1, 30, 0, NOW(3), NOW(3)),
  (4, 1, '饮品', 1, 40, 0, NOW(3), NOW(3)),
  (5, 1, '套餐', 1, 50, 0, NOW(3), NOW(3))
ON DUPLICATE KEY UPDATE update_time = NOW(3);

INSERT INTO product (
  id, shop_id, category_id, name, image_url, description, detail, price, original_price,
  stock, status, recommended, deleted, version, create_time, update_time
) VALUES
  (1, 1, 1, '招牌牛肉饭', '/images/demo/beef-rice.jpg', '招牌推荐，开发示例商品', NULL, 28.00, 32.00, 100, 1, 1, 0, 0, NOW(3), NOW(3)),
  (2, 1, 3, '香酥鸡排', '/images/demo/chicken-cutlet.jpg', '香酥小吃，开发示例商品', NULL, 16.00, NULL, 80, 1, 0, 0, 0, NOW(3), NOW(3)),
  (3, 1, 4, '柠檬茶', '/images/demo/lemon-tea.jpg', '清爽饮品，开发示例商品', NULL, 12.00, NULL, 60, 1, 0, 0, 0, NOW(3), NOW(3))
ON DUPLICATE KEY UPDATE update_time = NOW(3);

INSERT INTO product_specification (
  id, product_id, name, price_delta, status, sort, deleted, create_time, update_time
) VALUES
  (1, 1, '标准份', 0.00, 1, 10, 0, NOW(3), NOW(3)),
  (2, 1, '加大份', 5.00, 1, 20, 0, NOW(3), NOW(3)),
  (3, 3, '大杯', 3.00, 1, 10, 0, NOW(3), NOW(3))
ON DUPLICATE KEY UPDATE update_time = NOW(3);

INSERT INTO product_taste (
  id, product_id, name, status, sort, deleted, create_time, update_time
) VALUES
  (1, 1, '正常辣', 1, 10, 0, NOW(3), NOW(3)),
  (2, 1, '少辣', 1, 20, 0, NOW(3), NOW(3)),
  (3, 1, '不辣', 1, 30, 0, NOW(3), NOW(3)),
  (4, 3, '少冰', 1, 10, 0, NOW(3), NOW(3)),
  (5, 3, '去冰', 1, 20, 0, NOW(3), NOW(3))
ON DUPLICATE KEY UPDATE update_time = NOW(3);
