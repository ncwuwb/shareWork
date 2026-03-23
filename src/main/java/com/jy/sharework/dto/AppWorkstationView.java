package com.jy.sharework.dto;

import lombok.Data;

import java.util.List;

@Data
public class AppWorkstationView {
    private Long id;
    private String code;
    private Long spaceId;
    private Integer coordX;
    private Integer coordY;
    private Integer baseStatus;
    private Integer runtimeStatus;
    private List<Long> facilityIds;
    private List<String> facilityNames;
    private String spacePath;
    private String campusName;
    private String buildingName;
    private String floorName;
    private String areaName;
    private String bgImage;
}
