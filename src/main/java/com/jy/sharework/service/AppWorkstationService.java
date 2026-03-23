package com.jy.sharework.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jy.sharework.entity.BizFacility;
import com.jy.sharework.entity.BizOrder;
import com.jy.sharework.entity.BizWorkstation;
import com.jy.sharework.entity.BizWorkstationFacility;
import com.jy.sharework.mapper.BizFacilityMapper;
import com.jy.sharework.mapper.BizOrderMapper;
import com.jy.sharework.mapper.BizWorkstationFacilityMapper;
import com.jy.sharework.mapper.BizWorkstationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppWorkstationService {
    private final BizWorkstationMapper bizWorkstationMapper;
    private final BizOrderMapper bizOrderMapper;
    private final BizWorkstationFacilityMapper bizWorkstationFacilityMapper;
    private final BizFacilityMapper bizFacilityMapper;

    public List<BizFacility> listFacilities() {
        return bizFacilityMapper.selectList(new LambdaQueryWrapper<BizFacility>().orderByAsc(BizFacility::getId));
    }

    /**
     * 工位在查询时段的展示状态：0空闲 1已预约 2使用中 3不可用
     */
    public int runtimeStatus(BizWorkstation ws, LocalDateTime start, LocalDateTime end, Long currentUserId) {
        if (ws.getBaseStatus() != null && ws.getBaseStatus() == 1) {
            return 3;
        }
        if (ws.getBaseStatus() != null && ws.getBaseStatus() == 2) {
            if (currentUserId == null || ws.getReservedUserId() == null || !ws.getReservedUserId().equals(currentUserId)) {
                return 3;
            }
        }
        List<BizOrder> overlaps = bizOrderMapper.selectList(new LambdaQueryWrapper<BizOrder>()
                .eq(BizOrder::getWorkstationId, ws.getId())
                .in(BizOrder::getStatus, 0, 1)
                .lt(BizOrder::getStartTime, end)
                .gt(BizOrder::getEndTime, start));
        boolean inUse = overlaps.stream().anyMatch(o -> o.getStatus() == 1);
        if (inUse) {
            return 2;
        }
        boolean reserved = overlaps.stream().anyMatch(o -> o.getStatus() == 0);
        if (reserved) {
            return 1;
        }
        return 0;
    }

    public BizWorkstation getById(Long id) {
        return bizWorkstationMapper.selectById(id);
    }

    public List<BizWorkstation> listForArea(Long spaceId, LocalDateTime start, LocalDateTime end,
                                            List<Long> facilityIds, Long currentUserId) {
        List<BizWorkstation> list = bizWorkstationMapper.selectList(
                new LambdaQueryWrapper<BizWorkstation>().eq(BizWorkstation::getSpaceId, spaceId));
        if (facilityIds != null && !facilityIds.isEmpty()) {
            Set<Long> allowed = new HashSet<>(list.stream().map(BizWorkstation::getId).collect(Collectors.toList()));
            for (Long fid : facilityIds) {
                List<BizWorkstationFacility> rows = bizWorkstationFacilityMapper.selectList(
                        new LambdaQueryWrapper<BizWorkstationFacility>()
                                .eq(BizWorkstationFacility::getFacilityId, fid));
                Set<Long> has = rows.stream().map(BizWorkstationFacility::getWorkstationId).collect(Collectors.toSet());
                allowed.retainAll(has);
            }
            final Set<Long> f = allowed;
            list = list.stream().filter(w -> f.contains(w.getId())).collect(Collectors.toList());
        }
        return list;
    }

    public Map<Long, List<Long>> loadFacilityMap(Collection<Long> workstationIds) {
        if (workstationIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<BizWorkstationFacility> rows = bizWorkstationFacilityMapper.selectList(
                new LambdaQueryWrapper<BizWorkstationFacility>().in(BizWorkstationFacility::getWorkstationId, workstationIds));
        Map<Long, List<Long>> m = new HashMap<>();
        for (BizWorkstationFacility r : rows) {
            m.computeIfAbsent(r.getWorkstationId(), k -> new ArrayList<>()).add(r.getFacilityId());
        }
        return m;
    }
}
