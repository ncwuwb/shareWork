package com.jy.sharework.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jy.sharework.common.BusinessException;
import com.jy.sharework.common.R;
import com.jy.sharework.entity.SysDept;
import com.jy.sharework.mapper.SysDeptMapper;
import com.jy.sharework.security.LoginUser;
import com.jy.sharework.service.AdminScopeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/depts")
@RequiredArgsConstructor
public class AdminDeptController {
    private final SysDeptMapper sysDeptMapper;
    private final AdminScopeService adminScopeService;

    @GetMapping("/tree")
    public R<List<SysDept>> tree(@AuthenticationPrincipal LoginUser admin, @RequestParam(defaultValue = "0") long parentId) {
        if (!adminScopeService.isSuper(admin) && !admin.isAreaAdmin()) {
            throw new BusinessException("无权限");
        }
        return R.ok(sysDeptMapper.selectList(
                new LambdaQueryWrapper<SysDept>().eq(SysDept::getParentId, parentId).orderByAsc(SysDept::getSort)));
    }

    @PostMapping
    public R<SysDept> save(@AuthenticationPrincipal LoginUser admin, @RequestBody SysDept d) {
        if (!adminScopeService.isSuper(admin)) {
            throw new BusinessException("仅超级管理员可维护部门");
        }
        if (d.getId() == null) {
            sysDeptMapper.insert(d);
        } else {
            sysDeptMapper.updateById(d);
        }
        return R.ok(d);
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@AuthenticationPrincipal LoginUser admin, @PathVariable Long id) {
        if (!adminScopeService.isSuper(admin)) {
            throw new BusinessException("仅超级管理员可删除");
        }
        sysDeptMapper.deleteById(id);
        return R.ok();
    }
}
