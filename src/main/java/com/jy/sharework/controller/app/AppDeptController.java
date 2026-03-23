package com.jy.sharework.controller.app;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jy.sharework.common.R;
import com.jy.sharework.entity.SysDept;
import com.jy.sharework.mapper.SysDeptMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户端可选部门列表（登录后补绑部门等场景）。
 */
@RestController
@RequestMapping("/api/app/depts")
@RequiredArgsConstructor
public class AppDeptController {
    private final SysDeptMapper sysDeptMapper;

    @GetMapping
    public R<List<Map<String, Object>>> list() {
        List<SysDept> all = sysDeptMapper.selectList(
                new LambdaQueryWrapper<SysDept>().orderByAsc(SysDept::getParentId).orderByAsc(SysDept::getSort));
        Map<Long, String> idToName = new HashMap<>();
        for (SysDept d : all) {
            idToName.put(d.getId(), d.getName());
        }
        List<Map<String, Object>> out = new ArrayList<>();
        for (SysDept d : all) {
            Map<String, Object> row = new HashMap<>();
            row.put("id", d.getId());
            row.put("parentId", d.getParentId());
            String label = d.getName();
            Long pid = d.getParentId();
            if (pid != null && pid > 0) {
                String pn = idToName.get(pid);
                if (pn != null) {
                    label = pn + " · " + label;
                }
            }
            row.put("name", label);
            out.add(row);
        }
        return R.ok(out);
    }
}
