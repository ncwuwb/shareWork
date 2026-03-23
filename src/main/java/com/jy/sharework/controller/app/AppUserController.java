package com.jy.sharework.controller.app;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jy.sharework.common.R;
import com.jy.sharework.entity.BizCreditLog;
import com.jy.sharework.entity.SysDept;
import com.jy.sharework.entity.SysUser;
import com.jy.sharework.mapper.BizCreditLogMapper;
import com.jy.sharework.mapper.SysDeptMapper;
import com.jy.sharework.mapper.SysUserMapper;
import com.jy.sharework.security.LoginUser;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/app/me")
@RequiredArgsConstructor
public class AppUserController {
    private final SysUserMapper sysUserMapper;
    private final SysDeptMapper sysDeptMapper;
    private final BizCreditLogMapper bizCreditLogMapper;

    @GetMapping("/profile")
    public R<Map<String, Object>> profile(@AuthenticationPrincipal LoginUser lu) {
        SysUser u = sysUserMapper.selectById(lu.getUserId());
        Map<String, Object> m = new HashMap<>();
        m.put("user", u);
        if (u.getDeptId() != null) {
            SysDept d = sysDeptMapper.selectById(u.getDeptId());
            m.put("deptName", d != null ? d.getName() : null);
        }
        return R.ok(m);
    }

    @GetMapping("/credit-logs")
    public R<Page<BizCreditLog>> creditLogs(@AuthenticationPrincipal LoginUser lu,
                                              @RequestParam(defaultValue = "1") int page,
                                              @RequestParam(defaultValue = "20") int size) {
        Page<BizCreditLog> p = bizCreditLogMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<BizCreditLog>()
                        .eq(BizCreditLog::getUserId, lu.getUserId())
                        .orderByDesc(BizCreditLog::getCreateTime));
        return R.ok(p);
    }

    @GetMapping("/faq")
    public R<List<Map<String, String>>> faq() {
        Map<String, String> m1 = new HashMap<>();
        m1.put("q", "如何取消预约？");
        m1.put("a", "在订单列表进入详情，待签到状态可取消。开始前1小时以上取消不扣分。");
        Map<String, String> m2 = new HashMap<>();
        m2.put("q", "签到失败？");
        m2.put("a", "请确认已开启定位，且在办公区范围内（默认500米），并在允许的时间窗口内操作。");
        return R.ok(Arrays.asList(m1, m2));
    }

    /**
     * 首次补绑部门与姓名：仅当当前用户尚未设置 dept_id 时允许；已绑定需由管理端修改。
     */
    @PostMapping("/dept")
    public R<SysUser> bindDept(@AuthenticationPrincipal LoginUser lu, @RequestBody BindDeptBody body) {
        if (body == null || body.getDeptId() == null) {
            return R.fail("请选择部门");
        }
        String realName = body.getRealName() == null ? "" : body.getRealName().trim();
        if (realName.isEmpty()) {
            return R.fail("请输入姓名");
        }
        if (realName.length() > 50) {
            return R.fail("姓名过长");
        }
        SysUser u = sysUserMapper.selectById(lu.getUserId());
        if (u == null) {
            return R.fail("用户不存在");
        }
        if (u.getDeptId() != null) {
            return R.fail("已绑定部门，如需修改请联系管理员");
        }
        SysDept d = sysDeptMapper.selectById(body.getDeptId());
        if (d == null) {
            return R.fail("部门不存在");
        }
        SysUser patch = new SysUser();
        patch.setId(u.getId());
        patch.setDeptId(body.getDeptId());
        patch.setRealName(realName);
        sysUserMapper.updateById(patch);
        SysUser fresh = sysUserMapper.selectById(u.getId());
        if (fresh != null) {
            fresh.setPassword(null);
        }
        return R.ok(fresh);
    }

    @Data
    public static class BindDeptBody {
        private Long deptId;
        private String realName;
    }
}
