package com.jy.sharework.controller.app;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jy.sharework.common.R;
import com.jy.sharework.entity.BizMessage;
import com.jy.sharework.mapper.BizMessageMapper;
import com.jy.sharework.security.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/app/messages")
@RequiredArgsConstructor
public class AppMessageController {
    private final BizMessageMapper bizMessageMapper;

    @GetMapping
    public R<Page<BizMessage>> page(@AuthenticationPrincipal LoginUser lu,
                                    @RequestParam(defaultValue = "1") int page,
                                    @RequestParam(defaultValue = "20") int size) {
        return R.ok(bizMessageMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<BizMessage>()
                        .eq(BizMessage::getUserId, lu.getUserId())
                        .orderByDesc(BizMessage::getCreateTime)));
    }

    @PostMapping("/{id}/read")
    public R<Void> read(@AuthenticationPrincipal LoginUser lu, @PathVariable Long id) {
        BizMessage m = bizMessageMapper.selectById(id);
        if (m == null || !m.getUserId().equals(lu.getUserId())) {
            return R.fail("消息不存在");
        }
        m.setIsRead(1);
        bizMessageMapper.updateById(m);
        return R.ok();
    }
}
