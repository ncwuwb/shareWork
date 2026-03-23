package com.jy.sharework.controller.admin;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jy.sharework.common.BusinessException;
import com.jy.sharework.common.R;
import com.jy.sharework.entity.SysUser;
import com.jy.sharework.mapper.SysUserMapper;
import com.jy.sharework.security.LoginUser;
import com.jy.sharework.service.AdminScopeService;
import com.jy.sharework.service.SysConfigService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {
    private final SysUserMapper sysUserMapper;
    private final AdminScopeService adminScopeService;
    private final PasswordEncoder passwordEncoder;
    private final SysConfigService sysConfigService;

    @GetMapping("/page")
    public R<Page<SysUser>> page(@AuthenticationPrincipal LoginUser admin,
                                 @RequestParam(defaultValue = "1") int page,
                                 @RequestParam(defaultValue = "10") int size,
                                 @RequestParam(required = false) String phone) {
        if (!adminScopeService.isSuper(admin) && !admin.isAreaAdmin()) {
            throw new BusinessException("无权限");
        }
        LambdaQueryWrapper<SysUser> q = new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getRoleType, 3)
                .orderByDesc(SysUser::getId);
        if (phone != null && !phone.isEmpty()) {
            q.like(SysUser::getPhone, phone);
        }
        return R.ok(sysUserMapper.selectPage(new Page<>(page, size), q));
    }

    @PostMapping
    public R<SysUser> save(@AuthenticationPrincipal LoginUser admin, @RequestBody SysUser u) {
        if (!adminScopeService.isSuper(admin)) {
            throw new BusinessException("仅超级管理员可维护员工");
        }
        u.setRoleType(3);
        if (u.getCreditScore() == null) {
            u.setCreditScore(sysConfigService.getInt("initial_credit_score", 100));
        }
        if (u.getId() == null) {
            if (u.getPassword() != null) {
                u.setPassword(passwordEncoder.encode(u.getPassword()));
            }
            sysUserMapper.insert(u);
        } else {
            if (u.getPassword() != null && !u.getPassword().isEmpty()) {
                u.setPassword(passwordEncoder.encode(u.getPassword()));
            } else {
                u.setPassword(null);
                SysUser old = sysUserMapper.selectById(u.getId());
                if (old != null) {
                    u.setPassword(old.getPassword());
                }
            }
            sysUserMapper.updateById(u);
        }
        u.setPassword(null);
        return R.ok(u);
    }

    @PostMapping("/{id}/ban")
    public R<Void> ban(@AuthenticationPrincipal LoginUser admin, @PathVariable Long id, @RequestParam int status) {
        if (!adminScopeService.isSuper(admin)) {
            throw new BusinessException("仅超级管理员可操作");
        }
        SysUser u = sysUserMapper.selectById(id);
        if (u == null || u.getRoleType() != 3) {
            return R.fail("用户不存在");
        }
        u.setStatus(status);
        sysUserMapper.updateById(u);
        return R.ok();
    }

    @GetMapping("/export")
    public void export(@AuthenticationPrincipal LoginUser admin, HttpServletResponse response) throws IOException {
        if (!adminScopeService.isSuper(admin)) {
            throw new BusinessException("无权限");
        }
        List<SysUser> list = sysUserMapper.selectList(new LambdaQueryWrapper<SysUser>().eq(SysUser::getRoleType, 3));
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("users", "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
        List<List<String>> rows = new ArrayList<>();
        rows.add(Arrays.asList("手机号", "姓名", "部门ID", "信用分", "状态"));
        for (SysUser u : list) {
            rows.add(Arrays.asList(u.getPhone(), u.getRealName(), u.getDeptId() == null ? "" : String.valueOf(u.getDeptId()),
                    String.valueOf(u.getCreditScore()), String.valueOf(u.getStatus())));
        }
        EasyExcel.write(response.getOutputStream()).sheet("员工").doWrite(rows);
    }

    @PostMapping("/import")
    public R<Integer> importUsers(@AuthenticationPrincipal LoginUser admin, @RequestParam("file") MultipartFile file) throws IOException {
        if (!adminScopeService.isSuper(admin)) {
            throw new BusinessException("无权限");
        }
        List<UserImportRow> buffer = new ArrayList<>();
        EasyExcel.read(file.getInputStream(), UserImportRow.class, new ReadListener<UserImportRow>() {
            @Override
            public void invoke(UserImportRow row, AnalysisContext analysisContext) {
                buffer.add(row);
            }

            @Override
            public void doAfterAllAnalysed(AnalysisContext analysisContext) {
            }
        }).sheet().doRead();
        int n = 0;
        for (UserImportRow row : buffer) {
            if (row.getPhone() == null || row.getPhone().isEmpty()) {
                continue;
            }
            SysUser exist = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getPhone, row.getPhone()));
            if (exist != null) {
                continue;
            }
            SysUser u = new SysUser();
            u.setPhone(row.getPhone());
            u.setRealName(row.getRealName());
            if (row.getDeptId() != null) {
                u.setDeptId(row.getDeptId());
            }
            u.setRoleType(3);
            u.setCreditScore(sysConfigService.getInt("initial_credit_score", 100));
            u.setStatus(1);
            sysUserMapper.insert(u);
            n++;
        }
        return R.ok(n);
    }

    @Data
    public static class UserImportRow {
        @ExcelProperty("手机号")
        private String phone;
        @ExcelProperty("姓名")
        private String realName;
        @ExcelProperty("部门ID")
        private Long deptId;
    }
}
