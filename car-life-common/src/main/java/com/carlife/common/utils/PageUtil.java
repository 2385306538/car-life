package com.carlife.common.utils;

import lombok.Data;

import java.util.List;

@Data
public class PageUtil<T> {
    /**
     * 通用的用于封装向前端返回分页结果的对象
     */
    private long totals;//总记录数
    //private Integer pageNum;//第几页
    //private Integer pageSize;//每页多少条数据
    private List<T> result;//分页结果
}
