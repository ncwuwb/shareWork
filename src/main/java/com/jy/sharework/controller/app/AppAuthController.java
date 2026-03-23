package com.jy.sharework.controller.app;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jy.sharework.common.R;
import com.jy.sharework.dto.AppLoginRequest;
import com.jy.sharework.entity.SysUser;
import com.jy.sharework.mapper.SysUserMapper;
import com.jy.sharework.security.JwtUtils;
import com.jy.sharework.service.CaptchaService;
import com.jy.sharework.service.SysConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/app")
@RequiredArgsConstructor
public class AppAuthController {
    private final CaptchaService captchaService;
    private final SysUserMapper sysUserMapper;
    private final JwtUtils jwtUtils;
    private final SysConfigService sysConfigService;

    @PostMapping("/send-captcha")
    public R<Void> sendCaptcha(@RequestParam String phone, HttpServletRequest request) {
        if (phone == null || !phone.matches("^1\\d{10}$")) {
            return R.fail("请输入正确的手机号");
        }
        captchaService.send(phone, resolveClientIp(request));
        return R.ok();
    }

    @PostMapping("/login")
    public R<Map<String, Object>> login(@Valid @RequestBody AppLoginRequest req) {
        if (!captchaService.verify(req.getPhone(), req.getCode())) {
            return R.fail("验证码错误或已过期");
        }
        captchaService.consume(req.getPhone());
        SysUser user = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getPhone, req.getPhone()));
        if (user == null) {
            user = new SysUser();
            user.setPhone(req.getPhone());
            user.setRoleType(3);
            user.setCreditScore(sysConfigService.getInt("initial_credit_score", 100));
            user.setStatus(1);
            sysUserMapper.insert(user);
        } else {
            if (user.getRoleType() != 3) {
                return R.fail("请使用管理端账号登录后台");
            }
            if (user.getStatus() != 1) {
                return R.fail("账号已被禁用");
            }
        }
        String token = jwtUtils.createAppToken(user.getId(), user.getPhone(), user.getRoleType());
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", user);
        return R.ok(result);
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.trim().isEmpty()) {
            return forwardedFor.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.trim().isEmpty()) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }
}
