package com.jy.sharework.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("biz_order")
public class BizOrder {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private Long userId;
    private Long workstationId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime signTime;
    private LocalDateTime actualEndTime;
    /** 0待签到 1进行中 2已完成 3已取消 4已违约 */
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer isDeleted;
}
