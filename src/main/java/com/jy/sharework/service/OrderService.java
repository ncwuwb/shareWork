package com.jy.sharework.service;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jy.sharework.common.BusinessException;
import com.jy.sharework.entity.BizOrder;
import com.jy.sharework.entity.BizSpace;
import com.jy.sharework.entity.BizWorkstation;
import com.jy.sharework.entity.SysUser;
import com.jy.sharework.mapper.BizOrderMapper;
import com.jy.sharework.mapper.BizSpaceMapper;
import com.jy.sharework.mapper.BizWorkstationMapper;
import com.jy.sharework.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final BizOrderMapper bizOrderMapper;
    private final BizWorkstationMapper bizWorkstationMapper;
    private final BizSpaceMapper bizSpaceMapper;
    private final SysUserMapper sysUserMapper;
    private final RedisLockService redisLockService;
    private final SysConfigService sysConfigService;
    private final CreditService creditService;
    private final NotifyService notifyService;
    private final GeoCheckService geoCheckService;
    private final StringRedisTemplate stringRedisTemplate;

    @Transactional(rollbackFor = Exception.class)
    public BizOrder createOrder(Long userId, Long workstationId, LocalDateTime start, LocalDateTime end) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null || user.getStatus() != 1) {
            throw new BusinessException("账号不可用");
        }
        if (creditService.isBookingForbidden(user)) {
            throw new BusinessException("当前信用状态限制预约");
        }
        int minCredit = sysConfigService.getInt("credit_ban_threshold", 60);
        if (user.getCreditScore() != null && user.getCreditScore() < minCredit) {
            throw new BusinessException("信用分低于预约门槛");
        }

        int maxDays = sysConfigService.getInt("max_advance_days", 14);
        if (start.toLocalDate().isAfter(LocalDateTime.now().toLocalDate().plusDays(maxDays))) {
            throw new BusinessException("超出可提前预约天数");
        }
        if (!start.isBefore(end)) {
            throw new BusinessException("结束时间必须晚于开始时间");
        }
        long totalMinutes = Duration.between(start, end).toMinutes();
        if (totalMinutes < 60) {
            throw new BusinessException("单次预约至少 1 小时");
        }
        if (totalMinutes > 24 * 60) {
            throw new BusinessException("单次预约最多 24 小时");
        }

        rateLimit(userId);

        BizWorkstation workstation = bizWorkstationMapper.selectById(workstationId);
        if (workstation == null) {
            throw new BusinessException("工位不存在");
        }
        if (workstation.getBaseStatus() != null && workstation.getBaseStatus() == 1) {
            throw new BusinessException("工位维护中，暂不可预约");
        }
        if (workstation.getBaseStatus() != null && workstation.getBaseStatus() == 2) {
            if (workstation.getReservedUserId() == null || !workstation.getReservedUserId().equals(userId)) {
                throw new BusinessException("该工位为专属保留工位");
            }
        }

        Map<String, String> locks = acquireReservationLocks(userId, workstationId);
        if (locks == null) {
            throw new BusinessException(409, "当前预约请求过于集中，请刷新后重试");
        }
        try {
            if (bizOrderMapper.countUserOverlap(userId, start, end) > 0) {
                throw new BusinessException("该时间段内您已有其他预约");
            }
            if (bizOrderMapper.countWorkstationOverlap(workstationId, start, end) > 0) {
                throw new BusinessException("该时间段内工位已被占用");
            }
            BizOrder order = new BizOrder();
            order.setOrderNo(IdUtil.fastSimpleUUID());
            order.setUserId(userId);
            order.setWorkstationId(workstationId);
            order.setStartTime(start);
            order.setEndTime(end);
            order.setStatus(0);
            bizOrderMapper.insert(order);
            notifyService.send(userId, "预约成功", "您已成功预约工位，请按时签到。", "ORDER_CREATED");
            return order;
        } finally {
            releaseReservationLocks(locks);
        }
    }

    private Map<String, String> acquireReservationLocks(Long userId, Long workstationId) {
        List<String> keys = new ArrayList<>();
        keys.add("lock:reserve:user:" + userId);
        keys.add("lock:reserve:workstation:" + workstationId);
        Map<String, String> locks = new LinkedHashMap<>();
        for (String key : keys) {
            String token = redisLockService.tryLock(key, 30);
            if (token == null) {
                releaseReservationLocks(locks);
                return null;
            }
            locks.put(key, token);
        }
        return locks;
    }

    private void releaseReservationLocks(Map<String, String> locks) {
        if (locks == null || locks.isEmpty()) {
            return;
        }
        List<Map.Entry<String, String>> entries = new ArrayList<>(locks.entrySet());
        for (int i = entries.size() - 1; i >= 0; i--) {
            Map.Entry<String, String> entry = entries.get(i);
            redisLockService.unlock(entry.getKey(), entry.getValue());
        }
    }

    private void rateLimit(Long userId) {
        int limit = sysConfigService.getInt("reserve_rate_limit", 20);
        String key = "rate:reserve:" + userId;
        Long count = stringRedisTemplate.opsForValue().increment(key);
        if (count != null && count == 1L) {
            stringRedisTemplate.expire(key, 1, TimeUnit.MINUTES);
        }
        if (count != null && count > limit) {
            throw new BusinessException("操作过于频繁，请稍后再试");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long userId, Long orderId, boolean confirmLateCancel) {
        BizOrder order = bizOrderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException("订单不存在");
        }
        if (order.getStatus() != 0) {
            throw new BusinessException("当前订单不可取消");
        }
        LocalDateTime now = LocalDateTime.now();
        long minutesToStart = java.time.temporal.ChronoUnit.MINUTES.between(now, order.getStartTime());
        if (minutesToStart >= 60) {
            order.setStatus(3);
            bizOrderMapper.updateById(order);
            notifyService.send(userId, "预约已取消", "您已取消本次预约。", "ORDER_CANCELLED");
            return;
        }
        if (!confirmLateCancel) {
            throw new BusinessException(1001, "距离开始不足 1 小时，取消将扣分，是否继续？");
        }
        int deduct = sysConfigService.getInt("credit_cancel_late_deduct", 5);
        order.setStatus(3);
        bizOrderMapper.updateById(order);
        creditService.changeScore(userId, orderId, 2, deduct, "开始前 1 小时内取消预约");
        notifyService.send(userId, "预约已取消", "已取消预约并扣除信用分 " + deduct + "。", "ORDER_CANCELLED");
    }

    @Transactional(rollbackFor = Exception.class)
    public void sign(Long userId, Long orderId, double longitude, double latitude) {
        BizOrder order = bizOrderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException("订单不存在");
        }
        if (order.getStatus() != 0) {
            throw new BusinessException("当前状态不可签到");
        }
        int before = sysConfigService.getInt("sign_before_minutes", 15);
        int after = sysConfigService.getInt("sign_after_minutes", 30);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime winStart = order.getStartTime().minusMinutes(before);
        LocalDateTime winEnd = order.getStartTime().plusMinutes(after);
        if (now.isBefore(winStart)) {
            throw new BusinessException("签到时间未到");
        }
        if (now.isAfter(winEnd)) {
            throw new BusinessException("已错过签到时间");
        }

        BizWorkstation workstation = bizWorkstationMapper.selectById(order.getWorkstationId());
        BizSpace area = bizSpaceMapper.selectById(workstation.getSpaceId());
        BigDecimal centerLongitude = area.getCenterLongitude();
        BigDecimal centerLatitude = area.getCenterLatitude();
        if (centerLongitude == null || centerLatitude == null) {
            throw new BusinessException("当前区域未配置签到中心点");
        }
        double distance = geoCheckService.distanceMeters(
                longitude, latitude, centerLongitude.doubleValue(), centerLatitude.doubleValue());
        int radius = sysConfigService.getInt("geo_radius_meters", 500);
        if (distance > radius) {
            throw new BusinessException("当前位置不在允许签到范围内");
        }

        order.setStatus(1);
        order.setSignTime(now);
        bizOrderMapper.updateById(order);
        int reward = sysConfigService.getInt("credit_sign_reward", 1);
        creditService.changeScore(userId, orderId, 1, reward, "按时签到奖励");
        notifyService.send(userId, "签到成功", "已开始使用工位。", "ORDER_SIGNED");
    }

    @Transactional(rollbackFor = Exception.class)
    public void finishEarly(Long userId, Long orderId) {
        BizOrder order = bizOrderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException("订单不存在");
        }
        if (order.getStatus() != 1) {
            throw new BusinessException("当前订单不可结束");
        }
        LocalDateTime now = LocalDateTime.now();
        order.setStatus(2);
        order.setActualEndTime(now);
        bizOrderMapper.updateById(order);
        int reward = sysConfigService.getInt("credit_release_reward", 2);
        creditService.changeScore(userId, orderId, 1, reward, "主动提前结束奖励");
        notifyService.send(userId, "使用已结束", "工位已释放，感谢规范使用。", "ORDER_FINISHED");
    }

    public BizOrder getUserOrder(Long userId, Long orderId) {
        BizOrder order = bizOrderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException("订单不存在");
        }
        return order;
    }

    public com.jy.sharework.dto.AppOrderView getUserOrderView(Long userId, Long orderId) {
        com.jy.sharework.dto.AppOrderView view = bizOrderMapper.selectViewById(userId, orderId);
        if (view == null) {
            throw new BusinessException("订单不存在");
        }
        return view;
    }

    public Page<com.jy.sharework.dto.AppOrderView> pageUserOrderViews(Long userId, Integer status, int page, int size) {
        Page<com.jy.sharework.dto.AppOrderView> pageObj = new Page<>(page, size);
        Page<com.jy.sharework.dto.AppOrderView> result = bizOrderMapper.selectViewPage(pageObj, userId);
        if (status != null) {
            List<com.jy.sharework.dto.AppOrderView> filtered = result.getRecords().stream()
                    .filter(o -> status.equals(o.getStatus()))
                    .collect(java.util.stream.Collectors.toList());
            result.setRecords(filtered);
        }
        return result;
    }

    public Page<BizOrder> pageUserOrders(Long userId, Integer status, int page, int size) {
        LambdaQueryWrapper<BizOrder> query = new LambdaQueryWrapper<BizOrder>()
                .eq(BizOrder::getUserId, userId)
                .orderByDesc(BizOrder::getCreateTime);
        if (status != null) {
            query.eq(BizOrder::getStatus, status);
        }
        return bizOrderMapper.selectPage(new Page<>(page, size), query);
    }

    @Transactional(rollbackFor = Exception.class)
    public void markBreach(BizOrder order) {
        if (order.getStatus() != 0) {
            return;
        }
        order.setStatus(4);
        bizOrderMapper.updateById(order);
        int deduct = sysConfigService.getInt("credit_breach_deduct", 10);
        creditService.changeScore(order.getUserId(), order.getId(), 2, deduct, "预约未签到违约");
        notifyService.send(order.getUserId(), "预约违约", "您未在规定时间内签到，已扣分并释放工位。", "ORDER_BREACH");
    }

    @Transactional(rollbackFor = Exception.class)
    public void adminForceCancel(Long orderId) {
        BizOrder order = bizOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (order.getStatus() == 3 || order.getStatus() == 2) {
            return;
        }
        order.setStatus(3);
        bizOrderMapper.updateById(order);
        notifyService.send(order.getUserId(), "预约已取消", "管理员已强制取消您的预约。", "ORDER_ADMIN_CANCEL");
    }

    @Transactional(rollbackFor = Exception.class)
    public void adminRevokeBreach(Long orderId) {
        BizOrder order = bizOrderMapper.selectById(orderId);
        if (order == null || order.getStatus() != 4) {
            throw new BusinessException("仅违约订单支持撤销");
        }
        int deduct = sysConfigService.getInt("credit_breach_deduct", 10);
        order.setStatus(3);
        bizOrderMapper.updateById(order);
        creditService.changeScore(order.getUserId(), orderId, 1, deduct, "管理员撤销违约，恢复积分");
        creditService.clearRestrictIfRecovered(order.getUserId());
        notifyService.send(order.getUserId(), "违约处理已撤销", "管理员已撤销违约记录并恢复积分。", "ORDER_APPEAL_OK");
    }
}
