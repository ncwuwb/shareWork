package com.jy.sharework.controller.app;

import com.jy.sharework.common.R;
import com.jy.sharework.dto.AppFeedbackRequest;
import com.jy.sharework.entity.BizFeedback;
import com.jy.sharework.mapper.BizFeedbackMapper;
import com.jy.sharework.security.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/app/feedback")
@RequiredArgsConstructor
public class AppFeedbackController {
    private final BizFeedbackMapper bizFeedbackMapper;

    @PostMapping
    public R<Void> submit(@AuthenticationPrincipal LoginUser lu, @Valid @RequestBody AppFeedbackRequest request) {
        BizFeedback feedback = new BizFeedback();
        feedback.setUserId(lu.getUserId());
        feedback.setContent(request.getContent().trim());
        feedback.setStatus(0);
        bizFeedbackMapper.insert(feedback);
        return R.ok();
    }
}
