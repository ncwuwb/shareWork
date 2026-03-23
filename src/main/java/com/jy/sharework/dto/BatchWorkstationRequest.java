package com.jy.sharework.dto;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class BatchWorkstationRequest {
    @NotNull
    private Long spaceId;
    @NotBlank
    private String prefix;
    @NotNull
    @Min(1)
    private Integer startNum;
    @NotNull
    @Min(1)
    private Integer count;
}
