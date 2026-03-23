package com.jy.sharework.controller.app;

import com.jy.sharework.common.R;
import com.jy.sharework.entity.BizFacility;
import com.jy.sharework.entity.BizWorkstation;
import com.jy.sharework.security.LoginUser;
import com.jy.sharework.service.AppWorkstationService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/app/workstations")
@RequiredArgsConstructor
public class AppWorkstationController {
    private final AppWorkstationService appWorkstationService;

    @GetMapping("/facilities")
    public R<List<BizFacility>> facilities() {
        return R.ok(appWorkstationService.listFacilities());
    }

    @GetMapping
    public R<List<Map<String, Object>>> list(@RequestParam Long spaceId,
                                             @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
                                             @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
                                             @RequestParam(required = false) List<Long> facilityIds,
                                             @AuthenticationPrincipal LoginUser lu) {
        List<BizWorkstation> list = appWorkstationService.listForArea(spaceId, startTime, endTime, facilityIds, lu.getUserId());
        List<Long> ids = list.stream().map(BizWorkstation::getId).collect(Collectors.toList());
        Map<Long, List<Long>> facMap = appWorkstationService.loadFacilityMap(ids);
        List<Map<String, Object>> out = new ArrayList<>();
        for (BizWorkstation w : list) {
            int st = appWorkstationService.runtimeStatus(w, startTime, endTime, lu.getUserId());
            Map<String, Object> row = new HashMap<>();
            row.put("id", w.getId());
            row.put("code", w.getCode());
            row.put("spaceId", w.getSpaceId());
            row.put("coordX", w.getCoordX());
            row.put("coordY", w.getCoordY());
            row.put("baseStatus", w.getBaseStatus());
            row.put("runtimeStatus", st);
            row.put("facilityIds", facMap.getOrDefault(w.getId(), Collections.<Long>emptyList()));
            out.add(row);
        }
        return R.ok(out);
    }

    @GetMapping("/{id}")
    public R<Map<String, Object>> detail(@PathVariable Long id,
                                         @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
                                         @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
                                         @AuthenticationPrincipal LoginUser lu) {
        BizWorkstation w = appWorkstationService.getById(id);
        if (w == null) {
            return R.fail("工位不存在");
        }
        int st = appWorkstationService.runtimeStatus(w, startTime, endTime, lu.getUserId());
        Map<Long, List<Long>> facMap = appWorkstationService.loadFacilityMap(Collections.singletonList(id));
        Map<String, Object> row = new HashMap<>();
        row.put("id", w.getId());
        row.put("code", w.getCode());
        row.put("spaceId", w.getSpaceId());
        row.put("coordX", w.getCoordX());
        row.put("coordY", w.getCoordY());
        row.put("baseStatus", w.getBaseStatus());
        row.put("runtimeStatus", st);
        row.put("facilityIds", facMap.getOrDefault(id, Collections.<Long>emptyList()));
        return R.ok(row);
    }
}
