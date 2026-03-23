package com.jy.sharework.controller.admin;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jy.sharework.common.BusinessException;
import com.jy.sharework.common.R;
import com.jy.sharework.entity.BizOrder;
import com.jy.sharework.entity.BizSpace;
import com.jy.sharework.entity.BizWorkstation;
import com.jy.sharework.entity.SysDept;
import com.jy.sharework.entity.SysUser;
import com.jy.sharework.mapper.BizOrderMapper;
import com.jy.sharework.mapper.BizSpaceMapper;
import com.jy.sharework.mapper.BizWorkstationMapper;
import com.jy.sharework.mapper.SysDeptMapper;
import com.jy.sharework.mapper.SysUserMapper;
import com.jy.sharework.security.LoginUser;
import com.jy.sharework.service.AdminScopeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {
    private final BizOrderMapper bizOrderMapper;
    private final BizWorkstationMapper bizWorkstationMapper;
    private final BizSpaceMapper bizSpaceMapper;
    private final SysUserMapper sysUserMapper;
    private final SysDeptMapper sysDeptMapper;
    private final AdminScopeService adminScopeService;

    private static Object mapGet(Map<String, Object> row, String... keys) {
        for (String key : keys) {
            if (row.containsKey(key)) {
                return row.get(key);
            }
            for (Map.Entry<String, Object> entry : row.entrySet()) {
                if (entry.getKey().equalsIgnoreCase(key)) {
                    return entry.getValue();
                }
            }
        }
        return null;
    }

    private Map<String, Object> buildSummary(LoginUser admin) {
        if (!adminScopeService.isSuper(admin) && !admin.isAreaAdmin()) {
            throw new BusinessException("无权限");
        }
        if (adminScopeService.isSuper(admin)) {
            return buildGlobalSummary();
        }
        return buildScopedSummary(admin);
    }

    private Map<String, Object> buildGlobalSummary() {
        int today = bizOrderMapper.countTodayOrders();
        int signed = bizOrderMapper.countTodaySigned();
        double signRate = today == 0 ? 0 : Math.round(signed * 1000.0 / today) / 10.0;
        long totalWorkstations = bizWorkstationMapper.selectCount(null);
        long inUse = bizOrderMapper.selectCount(new LambdaQueryWrapper<BizOrder>().eq(BizOrder::getStatus, 1));
        double vacancy = totalWorkstations == 0 ? 0 : Math.round((1 - inUse * 1.0 / totalWorkstations) * 1000) / 10.0;
        Map<String, Object> result = new HashMap<>();
        result.put("todayOrders", today);
        result.put("todaySignRate", signRate);
        result.put("vacancyRate", vacancy);
        LocalDateTime from = LocalDateTime.now().minusDays(30);
        result.put("hotRegions", enrichRegionNames(bizOrderMapper.hotRegions(from)));
        result.put("hotWorkstations", enrichWsCodes(bizOrderMapper.hotWorkstations(from)));
        result.put("deptBreach", enrichDeptNames(bizOrderMapper.deptBreachStats(from)));
        result.put("breach30d", bizOrderMapper.countBreachSince(from));
        return result;
    }

    private Map<String, Object> buildScopedSummary(LoginUser admin) {
        Set<Long> spaceIds = adminScopeService.accessibleSpaceIds(admin);
        if (spaceIds.isEmpty()) {
            return emptySummary();
        }

        List<BizWorkstation> workstations = bizWorkstationMapper.selectList(
                new LambdaQueryWrapper<BizWorkstation>().in(BizWorkstation::getSpaceId, spaceIds));
        if (workstations.isEmpty()) {
            return emptySummary();
        }

        Map<Long, BizWorkstation> workstationMap = workstations.stream()
                .collect(Collectors.toMap(BizWorkstation::getId, Function.identity()));
        List<Long> workstationIds = new ArrayList<>(workstationMap.keySet());
        Map<String, Object> result = new HashMap<>();

        LocalDate todayDate = LocalDate.now();
        LocalDateTime dayStart = todayDate.atStartOfDay();
        LocalDateTime dayEnd = dayStart.plusDays(1);
        List<BizOrder> todayOrders = bizOrderMapper.selectList(new LambdaQueryWrapper<BizOrder>()
                .in(BizOrder::getWorkstationId, workstationIds)
                .ge(BizOrder::getStartTime, dayStart)
                .lt(BizOrder::getStartTime, dayEnd));

        int today = todayOrders.size();
        long signed = todayOrders.stream()
                .filter(order -> order.getSignTime() != null
                        && order.getStatus() != null
                        && (order.getStatus() == 1 || order.getStatus() == 2))
                .count();
        long inUse = bizOrderMapper.selectCount(new LambdaQueryWrapper<BizOrder>()
                .in(BizOrder::getWorkstationId, workstationIds)
                .eq(BizOrder::getStatus, 1));
        double signRate = today == 0 ? 0 : Math.round(signed * 1000.0 / today) / 10.0;
        double vacancy = workstations.isEmpty() ? 0 : Math.round((1 - inUse * 1.0 / workstations.size()) * 1000) / 10.0;

        LocalDateTime from = LocalDateTime.now().minusDays(30);
        List<BizOrder> recentOrders = bizOrderMapper.selectList(new LambdaQueryWrapper<BizOrder>()
                .in(BizOrder::getWorkstationId, workstationIds)
                .ge(BizOrder::getCreateTime, from));

        result.put("todayOrders", today);
        result.put("todaySignRate", signRate);
        result.put("vacancyRate", vacancy);
        result.put("hotRegions", buildScopedHotRegions(recentOrders, workstationMap));
        result.put("hotWorkstations", buildScopedHotWorkstations(recentOrders, workstationMap));
        result.put("deptBreach", buildScopedDeptBreach(recentOrders));
        result.put("breach30d", recentOrders.stream().filter(order -> order.getStatus() != null && order.getStatus() == 4).count());
        return result;
    }

    @GetMapping("/summary")
    public R<Map<String, Object>> summary(@AuthenticationPrincipal LoginUser admin) {
        return R.ok(buildSummary(admin));
    }

    private List<Map<String, Object>> enrichRegionNames(List<Map<String, Object>> rows) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            Map<String, Object> copy = new HashMap<>(row);
            Object sid = mapGet(row, "spaceId", "SPACE_ID");
            if (sid instanceof Number) {
                BizSpace space = bizSpaceMapper.selectById(((Number) sid).longValue());
                copy.put("spaceName", space != null ? space.getName() : null);
            }
            out.add(copy);
        }
        return out;
    }

    private List<Map<String, Object>> enrichDeptNames(List<Map<String, Object>> rows) {
        if (rows == null || rows.isEmpty()) {
            return rows == null ? Collections.emptyList() : rows;
        }
        List<Map<String, Object>> out = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            Map<String, Object> copy = new HashMap<>(row);
            Object did = mapGet(row, "deptId", "DEPT_ID");
            if (did instanceof Number) {
                SysDept dept = sysDeptMapper.selectById(((Number) did).longValue());
                copy.put("deptName", dept != null ? dept.getName() : null);
            }
            out.add(copy);
        }
        return out;
    }

    private List<Map<String, Object>> enrichWsCodes(List<Map<String, Object>> rows) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            Map<String, Object> copy = new HashMap<>(row);
            Object wid = mapGet(row, "workstationId", "WORKSTATION_ID");
            if (wid instanceof Number) {
                BizWorkstation workstation = bizWorkstationMapper.selectById(((Number) wid).longValue());
                copy.put("code", workstation != null ? workstation.getCode() : null);
            }
            out.add(copy);
        }
        return out;
    }

    private List<Map<String, Object>> buildScopedHotRegions(List<BizOrder> orders, Map<Long, BizWorkstation> workstationMap) {
        if (orders.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, Long> counts = new HashMap<>();
        for (BizOrder order : orders) {
            BizWorkstation workstation = workstationMap.get(order.getWorkstationId());
            if (workstation != null) {
                counts.merge(workstation.getSpaceId(), 1L, Long::sum);
            }
        }
        if (counts.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, String> names = bizSpaceMapper.selectBatchIds(counts.keySet()).stream()
                .collect(Collectors.toMap(BizSpace::getId, BizSpace::getName));
        return counts.entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue(Comparator.reverseOrder()))
                .limit(3)
                .map(entry -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("spaceId", entry.getKey());
                    row.put("cnt", entry.getValue());
                    row.put("spaceName", names.get(entry.getKey()));
                    return row;
                })
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> buildScopedHotWorkstations(List<BizOrder> orders, Map<Long, BizWorkstation> workstationMap) {
        if (orders.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, Long> counts = orders.stream()
                .collect(Collectors.groupingBy(BizOrder::getWorkstationId, Collectors.counting()));
        return counts.entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue(Comparator.reverseOrder()))
                .limit(10)
                .map(entry -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("workstationId", entry.getKey());
                    row.put("cnt", entry.getValue());
                    BizWorkstation workstation = workstationMap.get(entry.getKey());
                    row.put("code", workstation != null ? workstation.getCode() : null);
                    return row;
                })
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> buildScopedDeptBreach(List<BizOrder> orders) {
        if (orders.isEmpty()) {
            return Collections.emptyList();
        }
        Set<Long> userIds = orders.stream().map(BizOrder::getUserId).collect(Collectors.toSet());
        Map<Long, SysUser> users = sysUserMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(SysUser::getId, Function.identity()));
        Map<Long, long[]> stats = new HashMap<>();
        for (BizOrder order : orders) {
            SysUser user = users.get(order.getUserId());
            if (user == null || user.getDeptId() == null) {
                continue;
            }
            long[] counters = stats.computeIfAbsent(user.getDeptId(), ignored -> new long[2]);
            counters[1]++;
            if (order.getStatus() != null && order.getStatus() == 4) {
                counters[0]++;
            }
        }
        List<Map<String, Object>> deptRows = stats.entrySet().stream()
                .sorted(Map.Entry.<Long, long[]>comparingByKey())
                .map(entry -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("deptId", entry.getKey());
                    row.put("breach", entry.getValue()[0]);
                    row.put("total", entry.getValue()[1]);
                    return row;
                })
                .collect(Collectors.toList());
        return enrichDeptNames(deptRows);
    }

    private Map<String, Object> emptySummary() {
        Map<String, Object> result = new HashMap<>();
        result.put("todayOrders", 0);
        result.put("todaySignRate", 0);
        result.put("vacancyRate", 0);
        result.put("hotRegions", Collections.emptyList());
        result.put("hotWorkstations", Collections.emptyList());
        result.put("deptBreach", Collections.emptyList());
        result.put("breach30d", 0);
        return result;
    }

    @GetMapping("/export-summary")
    public void export(@AuthenticationPrincipal LoginUser admin, HttpServletResponse response) throws IOException {
        if (!adminScopeService.isSuper(admin)) {
            throw new BusinessException("无权限");
        }
        Map<String, Object> summary = buildSummary(admin);
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("dashboard-summary", "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
        List<List<String>> rows = new ArrayList<>();
        rows.add(Arrays.asList("指标", "值"));
        rows.add(Arrays.asList("今日预约", String.valueOf(summary.get("todayOrders"))));
        rows.add(Arrays.asList("今日签到率", String.valueOf(summary.get("todaySignRate"))));
        rows.add(Arrays.asList("空置率", String.valueOf(summary.get("vacancyRate"))));
        EasyExcel.write(response.getOutputStream()).sheet("汇总").doWrite(rows);
    }
}
