package com.shike.ordering.client.wechat;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.shike.ordering.common.exception.BusinessException;
import com.shike.ordering.common.exception.ErrorCode;
import com.shike.ordering.config.WechatProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "shike.wechat", name = "mock-enabled", havingValue = "false", matchIfMissing = true)
public class WechatApiClient implements WechatClient {
    private final RestClient.Builder restClientBuilder;
    private final WechatProperties properties;

    @Override
    public Identity exchangeCode(String code) {
        if (!StringUtils.hasText(properties.appId()) || !StringUtils.hasText(properties.appSecret())) {
            throw new BusinessException(ErrorCode.WECHAT_SERVICE_ERROR);
        }
        try {
            WechatResponse response = restClientBuilder.build().get().uri(uriBuilder -> uriBuilder
                    .scheme("https").host("api.weixin.qq.com").path("/sns/jscode2session")
                    .queryParam("appid", properties.appId()).queryParam("secret", properties.appSecret())
                    .queryParam("js_code", code).queryParam("grant_type", "authorization_code").build())
                    .retrieve().body(WechatResponse.class);
            if (response == null || !StringUtils.hasText(response.openid()) || response.errorCode() != null) {
                throw new BusinessException(ErrorCode.WECHAT_SERVICE_ERROR);
            }
            return new Identity(response.openid());
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BusinessException(ErrorCode.WECHAT_SERVICE_ERROR);
        }
    }

    private record WechatResponse(String openid, @JsonProperty("errcode") Integer errorCode) { }
}
