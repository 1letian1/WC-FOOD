package com.shike.ordering.client.wechat;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class MockWechatClientTest {
    @Test
    void exchangeCode_shouldReturnStableNonRawOpenid() {
        MockWechatClient client = new MockWechatClient();
        String openid = client.exchangeCode("dev-code").openid();

        assertThat(openid).startsWith("mock_").doesNotContain("dev-code");
        assertThat(client.exchangeCode("dev-code").openid()).isEqualTo(openid);
    }
}
