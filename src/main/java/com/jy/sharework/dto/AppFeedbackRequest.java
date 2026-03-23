package com.jy.sharework.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class AppFeedbackRequest {
    @NotBlank(message = "请输入反馈内容")
    @Size(max = 500, message = "反馈内容不能超过 500 字")
    private String content;
}
