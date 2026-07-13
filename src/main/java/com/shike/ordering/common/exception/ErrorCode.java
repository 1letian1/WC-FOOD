package com.shike.ordering.common.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    SUCCESS(0, "success", HttpStatus.OK),
    PARAM_ERROR(10001, "参数错误", HttpStatus.BAD_REQUEST),
    REQUEST_FORMAT_ERROR(10002, "请求格式错误", HttpStatus.BAD_REQUEST),
    METHOD_NOT_SUPPORTED(10003, "请求方法不支持", HttpStatus.METHOD_NOT_ALLOWED),
    API_NOT_FOUND(10004, "接口不存在", HttpStatus.NOT_FOUND),
    MEDIA_TYPE_NOT_SUPPORTED(10005, "Content-Type 不支持", HttpStatus.UNSUPPORTED_MEDIA_TYPE),
    DATA_CONFLICT(10006, "数据已存在或存在关联，无法操作", HttpStatus.CONFLICT),
    UNAUTHORIZED(20001, "未登录或登录已过期", HttpStatus.UNAUTHORIZED),
    FORBIDDEN(20002, "无权限访问", HttpStatus.FORBIDDEN),
    MERCHANT_BAD_CREDENTIALS(30001, "账号或密码错误", HttpStatus.UNAUTHORIZED),
    MERCHANT_DISABLED(30002, "商家账号已禁用", HttpStatus.FORBIDDEN),
    MERCHANT_LOGIN_LOCKED(30003, "登录失败次数过多，请稍后重试", HttpStatus.TOO_MANY_REQUESTS),
    PRODUCT_NOT_FOUND(40001, "商品不存在", HttpStatus.NOT_FOUND),
    PRODUCT_OFF_SALE(40002, "商品已下架", HttpStatus.CONFLICT),
    PRODUCT_SOLD_OUT(40003, "商品已售罄", HttpStatus.CONFLICT),
    PRODUCT_STOCK_INSUFFICIENT(40004, "商品库存不足", HttpStatus.CONFLICT),
    CATEGORY_NOT_FOUND(40005, "分类不存在", HttpStatus.NOT_FOUND),
    PRODUCT_SPECIFICATION_INVALID(40006, "商品规格不可用", HttpStatus.CONFLICT),
    PRODUCT_TASTE_INVALID(40007, "商品口味不可用", HttpStatus.CONFLICT),
    CART_EMPTY(50001, "购物车为空", HttpStatus.CONFLICT),
    ADDRESS_NOT_FOUND(50002, "地址不存在", HttpStatus.NOT_FOUND),
    CART_ITEM_NOT_FOUND(50003, "购物车条目不存在", HttpStatus.NOT_FOUND),
    CART_QUANTITY_EXCEEDED(50004, "购物车商品数量不能超过99", HttpStatus.BAD_REQUEST),
    ORDER_NOT_FOUND(60001, "订单不存在", HttpStatus.NOT_FOUND),
    ORDER_STATE_CONFLICT(60002, "订单状态已变化，请刷新后重试", HttpStatus.CONFLICT),
    DUPLICATE_OPERATION(60004, "请求重复，请勿重复操作", HttpStatus.CONFLICT),
    SHOP_NOT_FOUND(70005, "店铺不存在", HttpStatus.NOT_FOUND),
    FILE_TYPE_UNSUPPORTED(80001, "文件类型不支持", HttpStatus.BAD_REQUEST),
    FILE_TOO_LARGE(80002, "文件大小超出限制", HttpStatus.BAD_REQUEST),
    FILE_STORAGE_ERROR(80003, "文件保存失败", HttpStatus.INTERNAL_SERVER_ERROR),
    WECHAT_SERVICE_ERROR(90001, "微信服务调用失败", HttpStatus.BAD_GATEWAY),
    REDIS_UNAVAILABLE(90002, "服务暂时不可用，请稍后重试", HttpStatus.SERVICE_UNAVAILABLE),
    SYSTEM_ERROR(99999, "系统繁忙，请稍后重试", HttpStatus.INTERNAL_SERVER_ERROR);

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(int code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    public int getCode() { return code; }
    public String getMessage() { return message; }
    public HttpStatus getHttpStatus() { return httpStatus; }
}
