package com.jy.sharework.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class AdminLoginRequest {
    @NotBlank
    private String phone;
    @NotBlank
    private String password;
}
