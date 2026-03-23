package com.jy.sharework.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("biz_workstation")
public class BizWorkstation {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long spaceId;
    private String code;
    /** 0正常 1维修锁定 2专属保留 */
    private Integer baseStatus;
    private Long reservedUserId;
    private Integer coordX;
    private Integer coordY;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer isDeleted;
}
