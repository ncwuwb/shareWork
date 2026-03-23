package com.jy.sharework.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jy.sharework.common.R;
import com.jy.sharework.dto.AdminLoginRequest;
import com.jy.sharework.entity.SysUser;
import com.jy.sharework.mapper.SysUserMapper;
import com.jy.sharework.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminAuthController {
    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @PostMapping("/login")
    public R<Map<String, Object>> login(@Valid @RequestBody AdminLoginRequest req) {
        SysUser u = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getPhone, req.getPhone()));
        if (u == null || u.getRoleType() == null || u.getRoleType() == 3) {
            return R.fail("账号或密码错误");
        }
        if (u.getPassword() == null || !passwordEncoder.matches(req.getPassword(), u.getPassword())) {
            return R.fail("账号或密码错误");
        }
        if (u.getStatus() != 1) {
            return R.fail("账号已封禁");
        }
        String token = jwtUtils.createAdminToken(u.getId(), u.getPhone(), u.getRoleType());
        Map<String, Object> m = new HashMap<>();
        m.put("token", token);
        u.setPassword(null);
        m.put("user", u);
        return R.ok(m);
    }
}
