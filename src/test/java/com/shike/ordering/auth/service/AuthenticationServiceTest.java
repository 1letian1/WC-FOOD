package com.shike.ordering.auth.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.shike.ordering.auth.model.CurrentPrincipal;
import com.shike.ordering.auth.model.PrincipalContext;
import com.shike.ordering.auth.model.PrincipalType;
import com.shike.ordering.client.wechat.WechatClient;
import com.shike.ordering.common.exception.BusinessException;
import com.shike.ordering.common.security.PasswordService;
import com.shike.ordering.config.RedisProperties;
import com.shike.ordering.entity.MerchantAccount;
import com.shike.ordering.entity.User;
import com.shike.ordering.mapper.MerchantAccountMapper;
import com.shike.ordering.mapper.UserMapper;
import com.shike.ordering.vo.user.UserLoginVO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import java.time.Duration;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class AuthenticationServiceTest {
    private final WechatClient wechatClient = mock(WechatClient.class);
    private final UserMapper userMapper = mock(UserMapper.class);
    private final MerchantAccountMapper merchantMapper = mock(MerchantAccountMapper.class);
    private final PasswordService passwordService = mock(PasswordService.class);
    private final TokenGenerator tokenGenerator = mock(TokenGenerator.class);
    private final RedisSessionService sessionService = mock(RedisSessionService.class);
    private final StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
    private final AuthKeyFactory keyFactory = new AuthKeyFactory(new RedisProperties("shike:"));
    private final AuthProperties properties = new AuthProperties(
            Duration.ofDays(7), Duration.ofHours(12), Duration.ofMinutes(15), 5, Duration.ofMinutes(30));
    private final AuthenticationService service = new AuthenticationService(wechatClient, userMapper, merchantMapper,
            passwordService, tokenGenerator, sessionService, redisTemplate, keyFactory, properties);

    @AfterEach
    void clearContext() {
        PrincipalContext.clear();
    }

    @Test
    void loginUser_shouldTrustOpenidFromWechatClientOnly() {
        when(wechatClient.exchangeCode("wx-code")).thenReturn(new WechatClient.Identity("trusted-openid"));
        when(userMapper.selectOne(any())).thenReturn(null);
        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(11L);
            return 1;
        }).when(userMapper).insert(any(User.class));
        when(tokenGenerator.generate()).thenReturn("user-token");

        UserLoginVO result = service.loginUser("wx-code");

        assertThat(result.token()).isEqualTo("user-token");
        assertThat(result.user().id()).isEqualTo(11L);
        verify(sessionService).save(eq("user-token"), argThat(session ->
                session.principalId().equals(11L) && session.principalType() == PrincipalType.USER));
    }

    @Test
    void loginMerchant_whenFifthAttemptFails_shouldCreateThirtyMinuteLock() {
        @SuppressWarnings("unchecked")
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(redisTemplate.hasKey(anyString())).thenReturn(false);
        when(valueOperations.increment(anyString())).thenReturn(1L, 2L, 3L, 4L, 5L);
        when(merchantMapper.selectOne(any())).thenReturn(null);

        for (int attempt = 0; attempt < 5; attempt++) {
            assertThatThrownBy(() -> service.loginMerchant("merchant", "wrong"))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code").isEqualTo(30001);
        }

        verify(valueOperations).set(keyFactory.merchantLockKey("merchant"), "1", Duration.ofMinutes(30));
        verify(redisTemplate).delete(keyFactory.merchantFailureKey("merchant"));
    }

    @Test
    void changeMerchantPassword_shouldIncrementVersionAndInvalidateAllSessions() {
        PrincipalContext.set(new CurrentPrincipal(3L, PrincipalType.MERCHANT, 1L, "current-token"));
        MerchantAccount merchant = new MerchantAccount();
        merchant.setId(3L);
        merchant.setShopId(1L);
        merchant.setUsername("merchant");
        merchant.setPasswordHash("old-hash");
        merchant.setSessionVersion(2);
        when(merchantMapper.selectById(3L)).thenReturn(merchant);
        when(passwordService.matches("Oldpass1", "old-hash")).thenReturn(true);
        when(passwordService.matches("Newpass2", "old-hash")).thenReturn(false);
        when(passwordService.encode("Newpass2")).thenReturn("new-hash");
        when(merchantMapper.update(any(MerchantAccount.class), any(Wrapper.class))).thenReturn(1);

        service.changeMerchantPassword("Oldpass1", "Newpass2");

        verify(merchantMapper).update(argThat(update ->
                "new-hash".equals(update.getPasswordHash()) && update.getSessionVersion() == 3), any(Wrapper.class));
        verify(sessionService).deleteAllMerchantSessions(3L);
    }
}
