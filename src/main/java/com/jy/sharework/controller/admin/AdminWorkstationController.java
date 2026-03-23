package com.jy.sharework.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jy.sharework.common.BusinessException;
import com.jy.sharework.common.R;
import com.jy.sharework.dto.BatchWorkstationRequest;
import com.jy.sharework.entity.BizWorkstation;
import com.jy.sharework.entity.BizWorkstationFacility;
import com.jy.sharework.mapper.BizWorkstationFacilityMapper;
import com.jy.sharework.mapper.BizWorkstationMapper;
import com.jy.sharework.security.LoginUser;
import com.jy.sharework.service.AdminScopeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.IntStream;

@RestController
@RequestMapping("/api/admin/workstations")
@RequiredArgsConstructor
public class AdminWorkstationController {
    private final BizWorkstationMapper bizWorkstationMapper;
    private final BizWorkstationFacilityMapper bizWorkstationFacilityMapper;
    private final AdminScopeService adminScopeService;

    @GetMapping("/page")
    public R<Page<BizWorkstation>> page(@AuthenticationPrincipal LoginUser admin,
                                         @RequestParam Long spaceId,
                                         @RequestParam(defaultValue = "1") int page,
                                         @RequestParam(defaultValue = "20") int size) {
        if (!adminScopeService.canAccessSpace(admin, spaceId)) {
            throw new BusinessException("无权限");
        }
        return R.ok(bizWorkstationMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<BizWorkstation>().eq(BizWorkstation::getSpaceId, spaceId)));
    }

    @PostMapping("/batch")
    public R<Void> batch(@AuthenticationPrincipal LoginUser admin, @Valid @RequestBody BatchWorkstationRequest req) {
        if (!adminScopeService.canAccessSpace(admin, req.getSpaceId())) {
            throw new BusinessException("无权限");
        }
        IntStream.range(0, req.getCount()).forEach(i -> {
            int n = req.getStartNum() + i;
            String code = req.getPrefix() + String.format("%03d", n);
            BizWorkstation w = new BizWorkstation();
            w.setSpaceId(req.getSpaceId());
            w.setCode(code);
            w.setBaseStatus(0);
            bizWorkstationMapper.insert(w);
        });
        return R.ok();
    }

    @PutMapping("/{id}")
    public R<BizWorkstation> update(@AuthenticationPrincipal LoginUser admin, @PathVariable Long id, @RequestBody BizWorkstation body) {
        BizWorkstation w = bizWorkstationMapper.selectById(id);
        if (w == null) {
            return R.fail("不存在");
        }
        if (!adminScopeService.canAccessSpace(admin, w.getSpaceId())) {
            throw new BusinessException("无权限");
        }
        if (body.getCoordX() != null) {
            w.setCoordX(body.getCoordX());
        }
        if (body.getCoordY() != null) {
            w.setCoordY(body.getCoordY());
        }
        if (body.getBaseStatus() != null) {
            w.setBaseStatus(body.getBaseStatus());
        }
        if (body.getReservedUserId() != null) {
            w.setReservedUserId(body.getReservedUserId());
        }
        if (body.getCode() != null) {
            w.setCode(body.getCode());
        }
        bizWorkstationMapper.updateById(w);
        return R.ok(w);
    }

    @PostMapping("/{id}/facilities")
    public R<Void> bindFacilities(@AuthenticationPrincipal LoginUser admin, @PathVariable Long id, @RequestBody List<Long> facilityIds) {
        BizWorkstation w = bizWorkstationMapper.selectById(id);
        if (w == null) {
            return R.fail("不存在");
        }
        if (!adminScopeService.canAccessSpace(admin, w.getSpaceId())) {
            throw new BusinessException("无权限");
        }
        bizWorkstationFacilityMapper.delete(new LambdaQueryWrapper<BizWorkstationFacility>()
                .eq(BizWorkstationFacility::getWorkstationId, id));
        for (Long fid : facilityIds) {
            BizWorkstationFacility bf = new BizWorkstationFacility();
            bf.setWorkstationId(id);
            bf.setFacilityId(fid);
            bizWorkstationFacilityMapper.insert(bf);
        }
        return R.ok();
    }
}
