package com.jy.sharework.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sys_admin_space")
public class SysAdminSpace {
    private Long userId;
    private Long spaceId;
}
