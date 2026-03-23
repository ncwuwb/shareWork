package com.jy.sharework.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jy.sharework.common.BusinessException;
import com.jy.sharework.common.R;
import com.jy.sharework.entity.BizOrder;
import com.jy.sharework.entity.BizWorkstation;
import com.jy.sharework.entity.SysUser;
import com.jy.sharework.mapper.BizOrderMapper;
import com.jy.sharework.mapper.BizWorkstationMapper;
import com.jy.sharework.mapper.SysUserMapper;
import com.jy.sharework.security.LoginUser;
import com.jy.sharework.service.AdminScopeService;
import com.jy.sharework.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {
    private final BizOrderMapper bizOrderMapper;
    private final BizWorkstationMapper bizWorkstationMapper;
    private final SysUserMapper sysUserMapper;
    private final AdminScopeService adminScopeService;
    private final OrderService orderService;

    /** 预约看板：某日某区域下所有工位占用条 */
    @GetMapping("/board")
    public R<List<Map<String, Object>>> board(@AuthenticationPrincipal LoginUser admin,
                                              @RequestParam Long areaSpaceId,
                                              @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate day) {
        if (!adminScopeService.canAccessSpace(admin, areaSpaceId)) {
            throw new BusinessException("无权限");
        }
        LocalDateTime start = LocalDateTime.of(day, LocalTime.MIN);
        LocalDateTime end = start.plusDays(1);
        List<BizWorkstation> wsList = bizWorkstationMapper.selectList(
                new LambdaQueryWrapper<BizWorkstation>().eq(BizWorkstation::getSpaceId, areaSpaceId));
        List<Map<String, Object>> out = new ArrayList<>();
        for (BizWorkstation w : wsList) {
            List<BizOrder> orders = bizOrderMapper.selectList(new LambdaQueryWrapper<BizOrder>()
                    .eq(BizOrder::getWorkstationId, w.getId())
                    .in(BizOrder::getStatus, 0, 1, 2, 4)
                    .lt(BizOrder::getStartTime, end)
                    .gt(BizOrder::getEndTime, start));
            List<Map<String, Object>> segments = new ArrayList<>();
            for (BizOrder o : orders) {
                SysUser u = sysUserMapper.selectById(o.getUserId());
                Map<String, Object> seg = new HashMap<>();
                seg.put("orderId", o.getId());
                seg.put("startTime", o.getStartTime());
                seg.put("endTime", o.getEndTime());
                seg.put("status", o.getStatus());
                seg.put("userName", u != null ? u.getRealName() : null);
                seg.put("phone", u != null ? u.getPhone() : null);
                segments.add(seg);
            }
            Map<String, Object> row = new HashMap<>();
            row.put("workstationId", w.getId());
            row.put("code", w.getCode());
            row.put("segments", segments);
            out.add(row);
        }
        return R.ok(out);
    }

    @GetMapping("/breaches")
    public R<List<BizOrder>> breaches(@AuthenticationPrincipal LoginUser admin) {
        if (!adminScopeService.isSuper(admin) && !admin.isAreaAdmin()) {
            throw new BusinessException("无权限");
        }
        List<BizOrder> all = bizOrderMapper.selectList(new LambdaQueryWrapper<BizOrder>().eq(BizOrder::getStatus, 4)
                .orderByDesc(BizOrder::getUpdateTime));
        List<BizOrder> filtered = new ArrayList<>();
        for (BizOrder o : all) {
            BizWorkstation w = bizWorkstationMapper.selectById(o.getWorkstationId());
            if (w != null && adminScopeService.canAccessWorkstation(admin, w.getId())) {
                filtered.add(o);
            }
        }
        return R.ok(filtered);
    }

    @PostMapping("/{id}/revoke-breach")
    public R<Void> revokeBreach(@AuthenticationPrincipal LoginUser admin, @PathVariable Long id) {
        BizOrder o = bizOrderMapper.selectById(id);
        if (o == null) {
            return R.fail("订单不存在");
        }
        BizWorkstation w = bizWorkstationMapper.selectById(o.getWorkstationId());
        if (w == null || !adminScopeService.canAccessWorkstation(admin, w.getId())) {
            throw new BusinessException("无权限");
        }
        orderService.adminRevokeBreach(id);
        return R.ok();
    }

    @PostMapping("/{id}/force-cancel")
    public R<Void> forceCancel(@AuthenticationPrincipal LoginUser admin, @PathVariable Long id) {
        BizOrder o = bizOrderMapper.selectById(id);
        if (o == null) {
            return R.fail("订单不存在");
        }
        BizWorkstation w = bizWorkstationMapper.selectById(o.getWorkstationId());
        if (w == null || !adminScopeService.canAccessWorkstation(admin, w.getId())) {
            throw new BusinessException("无权限");
        }
        orderService.adminForceCancel(id);
        return R.ok();
    }
}
