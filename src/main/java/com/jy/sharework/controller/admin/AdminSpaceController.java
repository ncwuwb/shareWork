package com.jy.sharework.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jy.sharework.common.BusinessException;
import com.jy.sharework.common.R;
import com.jy.sharework.config.ShareProperties;
import com.jy.sharework.dto.SpaceAreaOption;
import com.jy.sharework.entity.BizSpace;
import com.jy.sharework.mapper.BizSpaceMapper;
import com.jy.sharework.security.LoginUser;
import com.jy.sharework.service.AdminScopeService;
import com.jy.sharework.service.BizSpaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/spaces")
@RequiredArgsConstructor
public class AdminSpaceController {
    private final BizSpaceService bizSpaceService;
    private final BizSpaceMapper bizSpaceMapper;
    private final AdminScopeService adminScopeService;
    private final ShareProperties shareProperties;

    @GetMapping("/children")
    public R<List<BizSpace>> children(@AuthenticationPrincipal LoginUser admin,
                                      @RequestParam(defaultValue = "0") long parentId) {
        if (adminScopeService.isSuper(admin)) {
            return R.ok(bizSpaceService.listByParent(parentId));
        }
        if (parentId == 0) {
            Set<Long> roots = adminScopeService.managedSpaceIds(admin.getUserId());
            List<BizSpace> list = new ArrayList<>();
            for (Long rootId : roots) {
                BizSpace space = bizSpaceService.get(rootId);
                if (space != null) {
                    list.add(space);
                }
            }
            return R.ok(list);
        }
        if (!adminScopeService.canAccessSpace(admin, parentId)) {
            throw new BusinessException("无权限查看该空间");
        }
        return R.ok(bizSpaceService.listByParent(parentId));
    }

    /**
     * 工位管理下拉：所有「办公区域」(type=4)，按当前管理员数据范围过滤；带层级路径便于识别。
     */
    @GetMapping("/workstation-areas")
    public R<List<SpaceAreaOption>> workstationAreas(@AuthenticationPrincipal LoginUser admin) {
        Set<Long> allowed = adminScopeService.accessibleSpaceIds(admin);
        if (!adminScopeService.isSuper(admin) && allowed.isEmpty()) {
            return R.ok(Collections.emptyList());
        }
        List<BizSpace> areas = bizSpaceMapper.selectList(
                new LambdaQueryWrapper<BizSpace>()
                        .eq(BizSpace::getType, 4)
                        .orderByAsc(BizSpace::getSort)
                        .orderByAsc(BizSpace::getId));
        List<BizSpace> filtered = adminScopeService.isSuper(admin)
                ? areas
                : areas.stream().filter(s -> allowed.contains(s.getId())).collect(Collectors.toList());
        List<SpaceAreaOption> options = new ArrayList<>();
        for (BizSpace s : filtered) {
            options.add(new SpaceAreaOption(s.getId(), s.getName(), buildSpacePath(s.getId())));
        }
        return R.ok(options);
    }

    private String buildSpacePath(Long spaceId) {
        List<String> parts = new ArrayList<>();
        Long id = spaceId;
        int guard = 0;
        while (id != null && id != 0 && guard++ < 32) {
            BizSpace node = bizSpaceMapper.selectById(id);
            if (node == null) {
                break;
            }
            parts.add(0, node.getName());
            id = node.getParentId();
        }
        return String.join(" / ", parts);
    }

    @PostMapping
    public R<BizSpace> save(@AuthenticationPrincipal LoginUser admin, @RequestBody BizSpace space) {
        if (space.getId() == null) {
            Long parentId = space.getParentId();
            boolean rootNode = parentId == null || parentId == 0;
            if (rootNode && !adminScopeService.isSuper(admin)) {
                throw new BusinessException("仅超级管理员可创建根空间");
            }
            if (!rootNode && !adminScopeService.isSuper(admin)
                    && !adminScopeService.canAccessSpace(admin, parentId)) {
                throw new BusinessException("无权限在该父节点下创建空间");
            }
        }
        if (space.getId() != null && !adminScopeService.canAccessSpace(admin, space.getId())) {
            throw new BusinessException("无权限修改该空间");
        }
        bizSpaceService.save(space);
        return R.ok(space);
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@AuthenticationPrincipal LoginUser admin, @PathVariable Long id) {
        if (!adminScopeService.canAccessSpace(admin, id)) {
            throw new BusinessException("无权限删除该空间");
        }
        bizSpaceService.delete(id);
        return R.ok();
    }

    @PostMapping("/upload-bg")
    public R<String> upload(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return R.fail("空文件");
        }
        Path dir = Paths.get(shareProperties.getUpload().getDir());
        Files.createDirectories(dir);
        String originalName = safeFileName(file.getOriginalFilename());
        String name = UUID.randomUUID() + "_" + originalName;
        Path target = dir.resolve(name);
        file.transferTo(target.toFile());
        return R.ok("/uploads/" + name);
    }

    private String safeFileName(String originalName) {
        if (originalName == null || originalName.trim().isEmpty()) {
            return "file";
        }
        String normalized = originalName.replace("\\", "/");
        String fileName = normalized.substring(normalized.lastIndexOf('/') + 1);
        return fileName.replaceAll("[^A-Za-z0-9._-]", "_");
    }
}
