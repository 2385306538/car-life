package com.order.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 封装订单提交的数据
 */
@Data
public class OrderSubmitVo {
    private Long addrId;//收货地址id
    private Integer payType;//支付方式

    //优惠券
    //积分
    private String orderToken;//防重令牌
    private BigDecimal payPrice;//应付价格 验价
    private String node;//订单备注
    //用户相关信息，去Token中取出
}
