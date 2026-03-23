package com.jy.sharework.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jy.sharework.common.BusinessException;
import com.jy.sharework.common.R;
import com.jy.sharework.entity.SysAdminSpace;
import com.jy.sharework.entity.SysUser;
import com.jy.sharework.mapper.SysAdminSpaceMapper;
import com.jy.sharework.mapper.SysUserMapper;
import com.jy.sharework.security.LoginUser;
import com.jy.sharework.service.AdminScopeService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 超级管理员：创建区域管理员并分配管辖空间
 */
@RestController
@RequestMapping("/api/admin/staff")
@RequiredArgsConstructor
public class AdminStaffScopeController {
    private final SysUserMapper sysUserMapper;
    private final SysAdminSpaceMapper sysAdminSpaceMapper;
    private final AdminScopeService adminScopeService;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/area-admin")
    public R<SysUser> createAreaAdmin(@AuthenticationPrincipal LoginUser admin, @RequestBody AreaAdminBody body) {
        if (!adminScopeService.isSuper(admin)) {
            throw new BusinessException("仅超级管理员可操作");
        }
        SysUser u = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getPhone, body.getPhone()));
        if (u == null) {
            u = new SysUser();
            u.setPhone(body.getPhone());
            u.setRealName(body.getRealName());
            u.setPassword(passwordEncoder.encode(body.getPassword()));
            u.setRoleType(2);
            u.setCreditScore(100);
            u.setStatus(1);
            sysUserMapper.insert(u);
        } else {
            u.setRoleType(2);
            u.setRealName(body.getRealName());
            if (body.getPassword() != null && !body.getPassword().isEmpty()) {
                u.setPassword(passwordEncoder.encode(body.getPassword()));
            }
            sysUserMapper.updateById(u);
        }
        sysAdminSpaceMapper.delete(new LambdaQueryWrapper<SysAdminSpace>().eq(SysAdminSpace::getUserId, u.getId()));
        for (Long sid : body.getSpaceIds()) {
            SysAdminSpace sas = new SysAdminSpace();
            sas.setUserId(u.getId());
            sas.setSpaceId(sid);
            sysAdminSpaceMapper.insert(sas);
        }
        u.setPassword(null);
        return R.ok(u);
    }

    @GetMapping("/area-admin/{userId}/spaces")
    public R<List<Long>> listSpaces(@AuthenticationPrincipal LoginUser admin, @PathVariable Long userId) {
        if (!adminScopeService.isSuper(admin)) {
            throw new BusinessException("仅超级管理员可查看");
        }
        List<SysAdminSpace> list = sysAdminSpaceMapper.selectList(
                new LambdaQueryWrapper<SysAdminSpace>().eq(SysAdminSpace::getUserId, userId));
        return R.ok(list.stream().map(SysAdminSpace::getSpaceId).collect(Collectors.toList()));
    }

    @Data
    public static class AreaAdminBody {
        private String phone;
        private String realName;
        private String password;
        private List<Long> spaceIds;
    }
}
