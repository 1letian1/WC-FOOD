package com.shike.ordering.client.wechat;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Component
@ConditionalOnProperty(prefix = "shike.wechat", name = "mock-enabled", havingValue = "true")
public class MockWechatClient implements WechatClient {
    @Override
    public Identity exchangeCode(String code) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(code.getBytes(StandardCharsets.UTF_8));
            return new Identity("mock_" + HexFormat.of().formatHex(digest).substring(0, 48));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }
}
