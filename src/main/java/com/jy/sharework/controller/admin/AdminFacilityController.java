package com.jy.sharework.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jy.sharework.common.BusinessException;
import com.jy.sharework.common.R;
import com.jy.sharework.entity.BizFacility;
import com.jy.sharework.mapper.BizFacilityMapper;
import com.jy.sharework.security.LoginUser;
import com.jy.sharework.service.AdminScopeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/facilities")
@RequiredArgsConstructor
public class AdminFacilityController {
    private final BizFacilityMapper bizFacilityMapper;
    private final AdminScopeService adminScopeService;

    @GetMapping
    public R<List<BizFacility>> all(@AuthenticationPrincipal LoginUser admin) {
        if (!adminScopeService.isSuper(admin) && !admin.isAreaAdmin()) {
            throw new BusinessException("无权限");
        }
        return R.ok(bizFacilityMapper.selectList(new LambdaQueryWrapper<BizFacility>().orderByAsc(BizFacility::getId)));
    }

    @GetMapping("/page")
    public R<Page<BizFacility>> page(@AuthenticationPrincipal LoginUser admin,
                                     @RequestParam(defaultValue = "1") int page,
                                     @RequestParam(defaultValue = "20") int size) {
        if (!adminScopeService.isSuper(admin)) {
            throw new BusinessException("仅超级管理员可维护设施字典");
        }
        return R.ok(bizFacilityMapper.selectPage(new Page<>(page, size), new LambdaQueryWrapper<>()));
    }

    @PostMapping
    public R<BizFacility> save(@AuthenticationPrincipal LoginUser admin, @RequestBody BizFacility f) {
        if (!adminScopeService.isSuper(admin)) {
            throw new BusinessException("仅超级管理员可维护设施字典");
        }
        if (f.getId() == null) {
            bizFacilityMapper.insert(f);
        } else {
            bizFacilityMapper.updateById(f);
        }
        return R.ok(f);
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@AuthenticationPrincipal LoginUser admin, @PathVariable Long id) {
        if (!adminScopeService.isSuper(admin)) {
            throw new BusinessException("仅超级管理员可删除");
        }
        bizFacilityMapper.deleteById(id);
        return R.ok();
    }
}
