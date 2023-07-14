package com.order.service;

import com.order.vo.OrderConfirmVo;

/**
 * 订单
 */
public interface OrderService {
    /**
     * 返回订单确认页需要用的数据
     * @return
     */
    OrderConfirmVo confirmOrder();
}
