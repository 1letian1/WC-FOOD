package com.shike.ordering.client.wechat;

public interface WechatClient {
    Identity exchangeCode(String code);

    record Identity(String openid) { }
}
