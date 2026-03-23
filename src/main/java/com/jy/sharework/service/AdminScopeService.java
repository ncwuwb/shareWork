package com.jy.sharework.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jy.sharework.entity.BizSpace;
import com.jy.sharework.entity.BizWorkstation;
import com.jy.sharework.entity.SysAdminSpace;
import com.jy.sharework.mapper.BizSpaceMapper;
import com.jy.sharework.mapper.BizWorkstationMapper;
import com.jy.sharework.mapper.SysAdminSpaceMapper;
import com.jy.sharework.security.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminScopeService {
    private final SysAdminSpaceMapper sysAdminSpaceMapper;
    private final BizSpaceMapper bizSpaceMapper;
    private final BizWorkstationMapper bizWorkstationMapper;

    public boolean isSuper(LoginUser user) {
        return user != null && user.isSuperAdmin();
    }

    public Set<Long> managedSpaceIds(Long adminUserId) {
        List<SysAdminSpace> list = sysAdminSpaceMapper.selectList(
                new LambdaQueryWrapper<SysAdminSpace>().eq(SysAdminSpace::getUserId, adminUserId));
        Set<Long> roots = new HashSet<>();
        for (SysAdminSpace item : list) {
            roots.add(item.getSpaceId());
        }
        return roots;
    }

    public Set<Long> accessibleSpaceIds(LoginUser admin) {
        if (admin == null) {
            return Collections.emptySet();
        }
        if (isSuper(admin)) {
            return bizSpaceMapper.selectList(new LambdaQueryWrapper<BizSpace>())
                    .stream()
                    .map(BizSpace::getId)
                    .collect(Collectors.toSet());
        }
        if (!admin.isAreaAdmin()) {
            return Collections.emptySet();
        }

        Set<Long> roots = managedSpaceIds(admin.getUserId());
        if (roots.isEmpty()) {
            return Collections.emptySet();
        }

        List<BizSpace> allSpaces = bizSpaceMapper.selectList(new LambdaQueryWrapper<BizSpace>());
        Map<Long, List<Long>> childrenMap = allSpaces.stream()
                .filter(space -> space.getParentId() != null && space.getParentId() != 0)
                .collect(Collectors.groupingBy(BizSpace::getParentId,
                        Collectors.mapping(BizSpace::getId, Collectors.toList())));

        Set<Long> result = new HashSet<>(roots);
        ArrayDeque<Long> queue = new ArrayDeque<>(roots);
        while (!queue.isEmpty()) {
            Long parentId = queue.poll();
            List<Long> children = childrenMap.getOrDefault(parentId, Collections.emptyList());
            for (Long childId : children) {
                if (result.add(childId)) {
                    queue.offer(childId);
                }
            }
        }
        return result;
    }

    public boolean canAccessSpace(LoginUser admin, Long spaceId) {
        if (admin == null || spaceId == null) {
            return false;
        }
        if (isSuper(admin)) {
            return true;
        }
        if (!admin.isAreaAdmin()) {
            return false;
        }
        Set<Long> roots = managedSpaceIds(admin.getUserId());
        if (roots.isEmpty()) {
            return false;
        }
        BizSpace current = bizSpaceMapper.selectById(spaceId);
        while (current != null) {
            if (roots.contains(current.getId())) {
                return true;
            }
            if (current.getParentId() == null || current.getParentId() == 0) {
                break;
            }
            current = bizSpaceMapper.selectById(current.getParentId());
        }
        return false;
    }

    public boolean canAccessWorkstation(LoginUser admin, Long workstationId) {
        if (admin == null || workstationId == null) {
            return false;
        }
        if (isSuper(admin)) {
            return true;
        }
        BizWorkstation workstation = bizWorkstationMapper.selectById(workstationId);
        if (workstation == null) {
            return false;
        }
        return canAccessSpace(admin, workstation.getSpaceId());
    }
}
