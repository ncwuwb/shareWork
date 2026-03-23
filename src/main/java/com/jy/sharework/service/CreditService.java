package com.jy.sharework.service;

import com.jy.sharework.entity.BizCreditLog;
import com.jy.sharework.entity.SysUser;
import com.jy.sharework.mapper.BizCreditLogMapper;
import com.jy.sharework.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CreditService {
    private final SysUserMapper sysUserMapper;
    private final BizCreditLogMapper bizCreditLogMapper;
    private final SysConfigService sysConfigService;

    /** changeType: 1 增加 2 扣除 */
    @Transactional(rollbackFor = Exception.class)
    public void changeScore(Long userId, Long orderId, int changeType, int score, String reason) {
        SysUser u = sysUserMapper.selectById(userId);
        if (u == null) {
            return;
        }
        int delta = changeType == 1 ? score : -score;
        int newScore = u.getCreditScore() + delta;
        if (newScore < 0) {
            newScore = 0;
        }
        u.setCreditScore(newScore);
        sysUserMapper.updateById(u);

        BizCreditLog log = new BizCreditLog();
        log.setUserId(userId);
        log.setOrderId(orderId);
        log.setChangeType(changeType);
        log.setScore(score);
        log.setReason(reason);
        bizCreditLogMapper.insert(log);

        maybeApplyBookingBan(u.getId(), newScore);
    }

    private void maybeApplyBookingBan(Long userId, int newScore) {
        int threshold = sysConfigService.getInt("credit_ban_threshold", 60);
        int days = sysConfigService.getInt("credit_ban_days", 7);
        if (newScore < threshold) {
            SysUser u = sysUserMapper.selectById(userId);
            if (u == null) {
                return;
            }
            LocalDateTime until = LocalDateTime.now().plusDays(days);
            if (u.getRestrictBookingUntil() == null || until.isAfter(u.getRestrictBookingUntil())) {
                u.setRestrictBookingUntil(until);
                sysUserMapper.updateById(u);
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void clearRestrictIfRecovered(Long userId) {
        SysUser u = sysUserMapper.selectById(userId);
        if (u == null) {
            return;
        }
        int threshold = sysConfigService.getInt("credit_ban_threshold", 60);
        if (u.getCreditScore() >= threshold) {
            u.setRestrictBookingUntil(null);
            sysUserMapper.updateById(u);
        }
    }

    public boolean isBookingForbidden(SysUser u) {
        if (u.getRestrictBookingUntil() == null) {
            return false;
        }
        return LocalDateTime.now().isBefore(u.getRestrictBookingUntil());
    }
}
