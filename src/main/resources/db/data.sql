USE shike_ordering;

INSERT INTO shop (
  id, name, logo_url, phone, address, notice, business_hours, business_status,
  dine_in_enabled, delivery_enabled, delivery_fee, min_delivery_amount, package_fee,
  delivery_range, estimated_delivery_minutes, version, create_time, update_time
) VALUES (
  1, '食刻小馆', NULL, '13800000000', '请在部署前配置真实店铺地址',
  '欢迎光临食刻小馆', '09:00-22:00', 1, 1, 1, 3.00, 20.00, 0.00,
  '商家配送，具体范围请电话咨询', 30, 0, NOW(3), NOW(3)
) ON DUPLICATE KEY UPDATE id = VALUES(id);

INSERT INTO category (id, shop_id, name, status, sort, deleted, create_time, update_time) VALUES
  (1, 1, '热门推荐', 1, 10, 0, NOW(3), NOW(3)),
  (2, 1, '主食', 1, 20, 0, NOW(3), NOW(3)),
  (3, 1, '小吃', 1, 30, 0, NOW(3), NOW(3)),
  (4, 1, '饮品', 1, 40, 0, NOW(3), NOW(3)),
  (5, 1, '套餐', 1, 50, 0, NOW(3), NOW(3))
ON DUPLICATE KEY UPDATE id = VALUES(id);
