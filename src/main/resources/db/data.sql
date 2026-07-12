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
