package com.order.service.impl;

import com.order.feign.MemberFeignService;
import com.order.service.OrderService;
import com.order.vo.OrderConfirmVo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

@Service
public class OrderServiceImpl implements OrderService {
    @Resource
    MemberFeignService memberFeignService;

    @Resource
    ThreadPoolExecutor threadPoolExecutor;
    @Override
    public OrderConfirmVo confirmOrder() {
        OrderConfirmVo confirmVo = new OrderConfirmVo();
        System.out.println("从拦截器中获取当前登录用户");
        //1. 收货地址列表：根据当前用户id，查询之前保存的所有收货地址，远程调用，开启第一个异步任务
        CompletableFuture<Void> addressFuture = CompletableFuture.runAsync(() ->{
            //每一个线程都来共享之前的请求数据
            //远程查询所有的收获地址列表
            System.out.println("远程调用 收货地址服务");
        },threadPoolExecutor);

        //2. 远程查询购物车所有选中的购物项
        CompletableFuture<Void> cartInfoFuture  = CompletableFuture.runAsync(()->{
            System.out.println("远程调用 购物车服务");
        },threadPoolExecutor).thenRunAsync(()->{
            //获取全部商品的id
            //远程查询商品库存信息
        },threadPoolExecutor);

        //3. 查询用户积分
        System.out.println("远程调用 用户积分");
        //4. 查询用户使用优惠券
        System.out.println("远程调用 优惠券");

        //5. 在实体类中已封装订单总金额和应付金额,自动计算

        //6. 防止重复提交令牌
        return confirmVo;
    }
}
