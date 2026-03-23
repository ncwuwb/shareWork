package com.jy.sharework.service;

import com.jy.sharework.common.BusinessException;
import com.jy.sharework.config.ShareProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class CaptchaService {
    private static final String CODE_PREFIX = "captcha:code:";
    private static final String COOLDOWN_PREFIX = "captcha:cooldown:";
    private static final String COUNT_PREFIX = "captcha:count:";

    private final StringRedisTemplate redisTemplate;
    private final SysConfigService sysConfigService;
    private final ShareProperties shareProperties;
    private final SecureRandom random = new SecureRandom();

    public void send(String phone, String clientIp) {
        applyCooldown(phone);
        incrementWithLimit(
                COUNT_PREFIX + "phone:" + phone,
                sysConfigService.getInt("captcha_phone_hour_limit", 10),
                "该手机号发送验证码过于频繁，请稍后再试"
        );
        if (StringUtils.hasText(clientIp)) {
            incrementWithLimit(
                    COUNT_PREFIX + "ip:" + clientIp,
                    sysConfigService.getInt("captcha_ip_hour_limit", 30),
                    "当前网络请求过于频繁，请稍后再试"
            );
        }

        String code = String.format("%06d", random.nextInt(1_000_000));
        redisTemplate.opsForValue().set(CODE_PREFIX + phone, code, 5, TimeUnit.MINUTES);
        log.info("captcha generated for phone {}", maskPhone(phone));
        if (shareProperties.getCaptcha().isDebugLogCode()) {
            log.info("captcha debug code for {} is {}", maskPhone(phone), code);
        }
    }

    public boolean verify(String phone, String code) {
        if (phone == null || code == null) {
            return false;
        }
        String cached = redisTemplate.opsForValue().get(CODE_PREFIX + phone);
        return cached != null && cached.equals(code);
    }

    public void consume(String phone) {
        redisTemplate.delete(CODE_PREFIX + phone);
    }

    private void applyCooldown(String phone) {
        int cooldownSeconds = sysConfigService.getInt("captcha_send_cooldown_seconds", 60);
        Boolean ok = redisTemplate.opsForValue().setIfAbsent(
                COOLDOWN_PREFIX + phone, "1", cooldownSeconds, TimeUnit.SECONDS);
        if (!Boolean.TRUE.equals(ok)) {
            throw new BusinessException("验证码发送间隔过短，请稍后再试");
        }
    }

    private void incrementWithLimit(String key, int limit, String message) {
        Long current = redisTemplate.opsForValue().increment(key);
        if (current != null && current == 1L) {
            redisTemplate.expire(key, 1, TimeUnit.HOURS);
        }
        if (current != null && current > limit) {
            throw new BusinessException(message);
        }
    }

    private String maskPhone(String phone) {
        if (!StringUtils.hasText(phone) || phone.length() < 7) {
            return "unknown";
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }
}
