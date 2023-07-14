package com.order.vo;


import com.sun.org.apache.xpath.internal.operations.Bool;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 购物车选中商品数据封装，购物项信息
 */
@Data
public class OrderItemVo {
    private Long skuId;
    private String title;
    private String image;
    private List<String> skuAttr;
    private BigDecimal price;
    private Integer count;
    private BigDecimal totalPrice;

    //TODO 查询库存状态
    private Boolean hasStock;
    private BigDecimal weight;
}
