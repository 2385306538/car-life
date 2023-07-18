package com.order.vo;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 订单确认页需要的数据
 */
public class OrderConfirmVo {
    // 收货地址
    @Getter @Setter
    List<MemberAddressVo> address;

    //所有选中的购物项，购物车服务中查询
    @Getter @Setter
    List<OrderItemVo> items;

    //防止重复提交令牌
    @Getter @Setter
    String orderToken;

    @Getter @Setter
    Map<Long,Boolean> stocks;
    //发票信息，弃了

    //优惠券信息，查询当前会员可用的优惠券

    //积分
    @Getter @Setter
    Integer integration;
    //计算多少件
    public Integer getCount() {
        Integer sum = 0;
        if (items != null) {
            for (OrderItemVo item : items) {
                sum += item.getCount();
            }
        }
        return sum;
    }


    //订单总额
    //BigDecimal totalPrice;
    public BigDecimal getTotalPrice() {
        BigDecimal total = new BigDecimal("0");
        if (items != null){
            for ( OrderItemVo item : items){
                BigDecimal multiply = item.getPrice().multiply(new BigDecimal(item.getCount().toString()));
                total = total.add(multiply);
            }
        }
        return total;
    }

    //应付金额
    //BigDecimal payPrice;
    public BigDecimal getPayPrice() {
        return getTotalPrice();
    }
    //运费、服务费、退换无忧、返现暂不计算

}
