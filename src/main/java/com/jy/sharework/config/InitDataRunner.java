package com.jy.sharework.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jy.sharework.entity.SysUser;
import com.jy.sharework.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class InitDataRunner implements ApplicationRunner {
    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;
    private final ShareProperties shareProperties;

    @Override
    public void run(ApplicationArguments args) {
        ShareProperties.Admin admin = shareProperties.getBootstrap().getAdmin();
        if (!admin.isEnabled()) {
            return;
        }
        if (!StringUtils.hasText(admin.getPhone()) || !StringUtils.hasText(admin.getPassword())) {
            log.warn("Skip bootstrap admin initialization because phone or password is blank.");
            return;
        }
        long count = sysUserMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getPhone, admin.getPhone()));
        if (count > 0) {
            return;
        }
        SysUser user = new SysUser();
        user.setPhone(admin.getPhone());
        user.setPassword(passwordEncoder.encode(admin.getPassword()));
        user.setRealName(admin.getRealName());
        user.setRoleType(1);
        user.setCreditScore(100);
        user.setStatus(1);
        sysUserMapper.insert(user);
        log.info("Bootstrap super admin initialized for phone {}.", admin.getPhone());
    }
}
