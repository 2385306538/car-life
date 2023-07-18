package com.order.to;


import com.order.entity.OrderEntity;
import com.order.entity.OrderItemEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderCreateTo {
    private OrderEntity order;
    private List<OrderItemEntity> orderItems;
    private BigDecimal payPrice;//订单应付价格
    private BigDecimal fare;//运费

}
