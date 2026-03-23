package com.jy.sharework.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 工位管理等处下拉：办公区域（type=4）及层级路径展示。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpaceAreaOption {
    private Long id;
    private String name;
    /** 从根到当前节点的名称路径，如：江南创新园 / A座 / 3层 / 开放办公东翼 */
    private String path;
}
