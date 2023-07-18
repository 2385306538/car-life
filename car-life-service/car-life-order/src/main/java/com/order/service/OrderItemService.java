package com.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.order.util.PageUtils;
import com.order.entity.OrderItemEntity;



import java.util.Map;

/**
 * 订单项信息
 */
public interface OrderItemService extends IService<OrderItemEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

