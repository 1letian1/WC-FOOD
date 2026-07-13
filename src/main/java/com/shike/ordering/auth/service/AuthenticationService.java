package com.shike.ordering.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.shike.ordering.auth.model.AuthSession;
import com.shike.ordering.auth.model.CurrentPrincipal;
import com.shike.ordering.auth.model.PrincipalContext;
import com.shike.ordering.auth.model.PrincipalType;
import com.shike.ordering.client.wechat.WechatClient;
import com.shike.ordering.common.exception.BusinessException;
import com.shike.ordering.common.exception.ErrorCode;
import com.shike.ordering.common.security.PasswordService;
import com.shike.ordering.entity.MerchantAccount;
import com.shike.ordering.entity.User;
import com.shike.ordering.mapper.MerchantAccountMapper;
import com.shike.ordering.mapper.UserMapper;
import com.shike.ordering.vo.merchant.MerchantLoginVO;
import com.shike.ordering.vo.merchant.MerchantProfileVO;
import com.shike.ordering.vo.user.UserLoginVO;
import com.shike.ordering.vo.user.UserProfileVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final WechatClient wechatClient;
    private final UserMapper userMapper;
    private final MerchantAccountMapper merchantMapper;
    private final PasswordService passwordService;
    private final TokenGenerator tokenGenerator;
    private final RedisSessionService sessionService;
    private final StringRedisTemplate redisTemplate;
    private final AuthKeyFactory keyFactory;
    private final AuthProperties properties;

    @Transactional(rollbackFor = Exception.class)
    public UserLoginVO loginUser(String code) {
        String openid = wechatClient.exchangeCode(code).openid();
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getOpenid, openid));
        if (user == null) {
            user = new User();
            user.setOpenid(openid);
            user.setStatus(1);
            user.setLastLoginTime(LocalDateTime.now());
            userMapper.insert(user);
        } else {
            if (!Objects.equals(user.getStatus(), 1)) throw new BusinessException(ErrorCode.FORBIDDEN);
            user.setLastLoginTime(LocalDateTime.now());
            userMapper.updateById(user);
        }
        String token = tokenGenerator.generate();
        sessionService.save(token, new AuthSession(user.getId(), PrincipalType.USER, null, LocalDateTime.now(), null));
        log.info("user login succeeded, userId={}", user.getId());
        return new UserLoginVO(token, properties.userSessionTtl().toSeconds(), toUserProfile(user));
    }

    public MerchantLoginVO loginMerchant(String rawUsername, String password) {
        String username = rawUsername.trim();
        if (Boolean.TRUE.equals(redisTemplate.hasKey(keyFactory.merchantLockKey(username)))) {
            throw new BusinessException(ErrorCode.MERCHANT_LOGIN_LOCKED);
        }
        MerchantAccount merchant = merchantMapper.selectOne(
                new LambdaQueryWrapper<MerchantAccount>().eq(MerchantAccount::getUsername, username));
        if (merchant == null || !password.equals(password.trim())
                || !passwordService.matches(password, merchant.getPasswordHash())) {
            recordFailure(username);
            log.warn("merchant login failed, username={}", username);
            throw new BusinessException(ErrorCode.MERCHANT_BAD_CREDENTIALS);
        }
        if (!Objects.equals(merchant.getStatus(), 1)) throw new BusinessException(ErrorCode.MERCHANT_DISABLED);
        redisTemplate.delete(keyFactory.merchantFailureKey(username));
        redisTemplate.delete(keyFactory.merchantLockKey(username));
        merchant.setLastLoginTime(LocalDateTime.now());
        merchantMapper.updateById(merchant);
        String token = tokenGenerator.generate();
        sessionService.save(token, new AuthSession(merchant.getId(), PrincipalType.MERCHANT,
                merchant.getShopId(), LocalDateTime.now(), merchant.getSessionVersion()));
        log.info("merchant login succeeded, merchantId={}, shopId={}", merchant.getId(), merchant.getShopId());
        return new MerchantLoginVO(token, properties.merchantSessionTtl().toSeconds(), toMerchantProfile(merchant));
    }

    public void logout() {
        CurrentPrincipal principal = PrincipalContext.require();
        sessionService.delete(principal.principalType(), principal.principalId(), principal.token());
    }

    public UserProfileVO currentUser() {
        CurrentPrincipal principal = requireType(PrincipalType.USER);
        User user = userMapper.selectById(principal.principalId());
        if (user == null || !Objects.equals(user.getStatus(), 1)) throw new BusinessException(ErrorCode.UNAUTHORIZED);
        return toUserProfile(user);
    }

    public MerchantProfileVO currentMerchant() {
        CurrentPrincipal principal = requireType(PrincipalType.MERCHANT);
        MerchantAccount merchant = merchantMapper.selectById(principal.principalId());
        if (merchant == null || !Objects.equals(merchant.getStatus(), 1)
                || !Objects.equals(merchant.getShopId(), principal.shopId())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return toMerchantProfile(merchant);
    }

    @Transactional(rollbackFor = Exception.class)
    public void changeMerchantPassword(String currentPassword, String newPassword) {
        CurrentPrincipal principal = requireType(PrincipalType.MERCHANT);
        MerchantAccount merchant = merchantMapper.selectById(principal.principalId());
        if (merchant == null || !passwordService.matches(currentPassword, merchant.getPasswordHash())) {
            throw new BusinessException(ErrorCode.MERCHANT_BAD_CREDENTIALS);
        }
        if (!newPassword.equals(newPassword.trim()) || newPassword.equals(merchant.getUsername())
                || passwordService.matches(newPassword, merchant.getPasswordHash())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        MerchantAccount update = new MerchantAccount();
        update.setPasswordHash(passwordService.encode(newPassword));
        update.setSessionVersion(merchant.getSessionVersion() + 1);
        int affected = merchantMapper.update(update, new LambdaUpdateWrapper<MerchantAccount>()
                .eq(MerchantAccount::getId, merchant.getId())
                .eq(MerchantAccount::getSessionVersion, merchant.getSessionVersion()));
        if (affected != 1) throw new BusinessException(ErrorCode.DATA_CONFLICT);
        sessionService.deleteAllMerchantSessions(merchant.getId());
        log.info("merchant password changed, merchantId={}", merchant.getId());
    }

    private void recordFailure(String username) {
        String failureKey = keyFactory.merchantFailureKey(username);
        Long failures = redisTemplate.opsForValue().increment(failureKey);
        if (failures != null && failures == 1) redisTemplate.expire(failureKey, properties.merchantFailureWindow());
        if (failures != null && failures >= properties.merchantMaxFailures()) {
            redisTemplate.opsForValue().set(keyFactory.merchantLockKey(username), "1", properties.merchantLockTtl());
            redisTemplate.delete(failureKey);
        }
    }

    private CurrentPrincipal requireType(PrincipalType type) {
        CurrentPrincipal principal = PrincipalContext.require();
        if (principal.principalType() != type) throw new BusinessException(ErrorCode.FORBIDDEN);
        return principal;
    }

    private UserProfileVO toUserProfile(User user) {
        return new UserProfileVO(user.getId(), user.getNickname(), user.getAvatarUrl(), user.getPhone());
    }

    private MerchantProfileVO toMerchantProfile(MerchantAccount merchant) {
        return new MerchantProfileVO(merchant.getId(), merchant.getShopId(), merchant.getUsername(),
                merchant.getMerchantName(), merchant.getAvatarUrl(), merchant.getRole());
    }
}
