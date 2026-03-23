package com.jy.sharework.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jy.sharework.common.BusinessException;
import com.jy.sharework.common.R;
import com.jy.sharework.entity.SysConfig;
import com.jy.sharework.mapper.SysConfigMapper;
import com.jy.sharework.security.LoginUser;
import com.jy.sharework.service.AdminScopeService;
import com.jy.sharework.service.SysConfigService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/config")
@RequiredArgsConstructor
public class AdminConfigController {
    private final SysConfigMapper sysConfigMapper;
    private final SysConfigService sysConfigService;
    private final AdminScopeService adminScopeService;

    @GetMapping("/list")
    public R<List<SysConfig>> list(@AuthenticationPrincipal LoginUser admin) {
        if (!adminScopeService.isSuper(admin)) {
            throw new BusinessException("仅超级管理员可查看全局参数");
        }
        return R.ok(sysConfigMapper.selectList(new LambdaQueryWrapper<>()));
    }

    @PostMapping
    public R<Void> save(@AuthenticationPrincipal LoginUser admin, @RequestBody ConfigItem body) {
        if (!adminScopeService.isSuper(admin)) {
            throw new BusinessException("仅超级管理员可修改");
        }
        sysConfigService.upsert(body.getConfigKey(), body.getConfigValue(), body.getDescription());
        return R.ok();
    }

    @Data
    public static class ConfigItem {
        private String configKey;
        private String configValue;
        private String description;
    }
}
