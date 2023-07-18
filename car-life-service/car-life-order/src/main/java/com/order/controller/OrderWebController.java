package com.order.controller;

import com.carlife.common.result.Result;
import com.order.service.OrderService;
import com.order.vo.OrderConfirmVo;
import com.order.vo.OrderSubmitVo;
import com.order.vo.SubmitOrderResponseVo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;

@Controller
@RequestMapping("/order")
public class OrderWebController {

    @Resource
    OrderService orderService;

    /**
     * 结算确认页
     * @return
     */
    @GetMapping("/toTrade")
    public Result toTrade(){
        OrderConfirmVo orderConfirmVo = orderService.confirmOrder();
        //展示订单确认的数据
        return Result.ok("查询成功",orderConfirmVo);
    }

    /**
     * 去下单功能
     * @return
     */
    @PostMapping("/submitOrder")
    public Result submitOrder(OrderSubmitVo submitVo){
        SubmitOrderResponseVo responseVo = orderService.submitOrder(submitVo);
        if (responseVo.getCode() == 0){
            //下单成功:下单成功到支付选项页
            return Result.ok("pay");
        }else {
            //下单失败返回到订单确认页
            return Result.fail("toTrade");
        }
    }
}
