package com.order.controller;

import com.carlife.common.result.Result;
import com.order.service.OrderService;
import com.order.vo.OrderConfirmVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/order")
public class OrderWebController {

    @Autowired
    OrderService orderService;
    @GetMapping("/toTrade")
    public Result toTrade(){
        OrderConfirmVo orderConfirmVo = orderService.confirmOrder();
        //展示订单确认的数据
        return Result.ok("查询成功",orderConfirmVo);
    }
}
