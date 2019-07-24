package com.example.demo.jobs.elasticsearch;

import lombok.Data;

/**
 * 查询项对象
 *
 * @author felix
 */
@Data
public class SearchItem {
    /**
     * 查询项code
     */
    private String itemCode;

    /**
     * 查询项名称
     */
    private String itemName;

    /**
     * 查询项分类code
     */
    private String childCateCode;

    /**
     * 查询项分类名称
     */
    private String childCateName;

    /**
     * 医嘱名称
     */
    private String ordName;

    /**
     * 医嘱编码
     */
    private String ordCode;
}
