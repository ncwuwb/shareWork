package com.jy.sharework.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class SignRequest {
    @NotNull
    private Double longitude;
    @NotNull
    private Double latitude;
}
