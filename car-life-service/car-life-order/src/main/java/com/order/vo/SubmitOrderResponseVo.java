package com.order.vo;

import com.order.entity.OrderEntity;
import lombok.Data;

/**
 * 下单操作的返回数据
 */

@Data
public class SubmitOrderResponseVo {
    private OrderEntity order;//订单信息实体类
    private Integer code;//错误状态吗，0成功,1失败

}
