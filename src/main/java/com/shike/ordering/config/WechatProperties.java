package com.shike.ordering.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("shike.wechat")
public record WechatProperties(String appId, String appSecret, boolean mockEnabled) { }
