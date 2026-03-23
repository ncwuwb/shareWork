package com.jy.sharework.controller.app;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jy.sharework.common.R;
import com.jy.sharework.dto.AppOrderView;
import com.jy.sharework.dto.ReserveRequest;
import com.jy.sharework.dto.SignRequest;
import com.jy.sharework.entity.BizOrder;
import com.jy.sharework.security.LoginUser;
import com.jy.sharework.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/app/orders")
@RequiredArgsConstructor
public class AppOrderController {
    private final OrderService orderService;

    @PostMapping
    public R<BizOrder> create(@AuthenticationPrincipal LoginUser lu, @Valid @RequestBody ReserveRequest req) {
        return R.ok(orderService.createOrder(lu.getUserId(), req.getWorkstationId(), req.getStartTime(), req.getEndTime()));
    }

    @GetMapping
    public R<Page<AppOrderView>> page(@AuthenticationPrincipal LoginUser lu,
                                      @RequestParam(required = false) Integer status,
                                      @RequestParam(defaultValue = "1") int page,
                                      @RequestParam(defaultValue = "10") int size) {
        return R.ok(orderService.pageUserOrderViews(lu.getUserId(), status, page, size));
    }

    @PostMapping("/{id}/cancel")
    public R<Void> cancel(@AuthenticationPrincipal LoginUser lu,
                          @PathVariable Long id,
                          @RequestParam(defaultValue = "false") boolean confirmLateCancel) {
        orderService.cancelOrder(lu.getUserId(), id, confirmLateCancel);
        return R.ok();
    }

    @PostMapping("/{id}/sign")
    public R<Void> sign(@AuthenticationPrincipal LoginUser lu, @PathVariable Long id, @Valid @RequestBody SignRequest req) {
        orderService.sign(lu.getUserId(), id, req.getLongitude(), req.getLatitude());
        return R.ok();
    }

    @PostMapping("/{id}/finish")
    public R<Void> finish(@AuthenticationPrincipal LoginUser lu, @PathVariable Long id) {
        orderService.finishEarly(lu.getUserId(), id);
        return R.ok();
    }

    @GetMapping("/{id}")
    public R<AppOrderView> detail(@AuthenticationPrincipal LoginUser lu, @PathVariable Long id) {
        return R.ok(orderService.getUserOrderView(lu.getUserId(), id));
    }
}
