package com.jy.sharework.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AppOrderView {
    private Long id;
    private String orderNo;
    private Long workstationId;
    private String workstationCode;
    /** 所属办公区域签到中心（GCJ-02），测试阶段可代替客户端定位提交 */
    private BigDecimal signCenterLongitude;
    private BigDecimal signCenterLatitude;
    private String spacePath;
    private String campusName;
    private String buildingName;
    private String floorName;
    private String areaName;
    private Integer status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime signTime;
    private LocalDateTime actualEndTime;
    private LocalDateTime createTime;
}
