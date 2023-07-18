package com.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.order.entity.OrderEntity;
import com.order.util.PageUtils;
import com.order.vo.OrderConfirmVo;
import com.order.vo.OrderSubmitVo;
import com.order.vo.SubmitOrderResponseVo;

import java.util.Map;

/**
 * 订单
 */
public interface OrderService extends IService<OrderEntity> {
    /**
     * 返回订单确认页需要用的数据
     * @return
     */
    OrderConfirmVo confirmOrder();

    SubmitOrderResponseVo submitOrder(OrderSubmitVo submitVo);

    /**
     * 关闭订单
     * @param orderEntity
     */
    void closeOrder(OrderEntity orderEntity);
    /**
     * 查询当前用户所有订单数据
     * @param params
     * @return
     */
    PageUtils queryPageWithItem(Map<String, Object> params);
}
