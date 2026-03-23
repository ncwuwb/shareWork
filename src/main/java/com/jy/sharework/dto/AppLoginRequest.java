package com.jy.sharework.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class AppLoginRequest {
    @NotBlank
    private String phone;
    @NotBlank
    private String code;
}
