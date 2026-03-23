package com.jy.sharework.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("biz_workstation_facility")
public class BizWorkstationFacility {
    private Long workstationId;
    private Long facilityId;
}
