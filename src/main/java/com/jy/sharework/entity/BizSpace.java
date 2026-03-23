package com.jy.sharework.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("biz_space")
public class BizSpace {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long parentId;
    private String name;
    /** 1园区 2楼宇 3楼层 4办公区域 */
    private Integer type;
    private BigDecimal centerLongitude;
    private BigDecimal centerLatitude;
    private String bgImage;
    private Integer sort;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer isDeleted;
}
