package com.jy.sharework.task;

import com.jy.sharework.entity.BizOrder;
import com.jy.sharework.mapper.BizOrderMapper;
import com.jy.sharework.service.OrderService;
import com.jy.sharework.service.SysConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTimeoutTask {
    private final BizOrderMapper bizOrderMapper;
    private final OrderService orderService;
    private final SysConfigService sysConfigService;

    /** 每分钟扫描待签到超时订单 */
    @Scheduled(fixedDelay = 60_000)
    public void breachPendingSign() {
        int after = sysConfigService.getInt("sign_after_minutes", 30);
        LocalDateTime deadline = LocalDateTime.now().minusMinutes(after);
        List<BizOrder> list = bizOrderMapper.listPendingSignBefore(deadline);
        for (BizOrder o : list) {
            try {
                orderService.markBreach(o);
            } catch (Exception e) {
                log.warn("mark breach fail order {}", o.getId(), e);
            }
        }
    }
}
