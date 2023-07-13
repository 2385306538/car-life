package com.eugene.controller;

import com.eugene.controller.request.AddCouponActivityRequest;
import com.eugene.response.Response;
import com.eugene.service.ICouponActivityService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * @Description 优惠券活动相关
 * @Author 郭爽
 */
@RestController
@RequestMapping("/couponActivity")
public class CouponActivityController {

    @Resource
    private ICouponActivityService couponActivityService;

    @PostMapping("/addCouponActivity")
    @Operation(summary = "新建优惠券活动", description = "新建优惠券活动")
    public Response addCouponActivity(@RequestBody @Valid AddCouponActivityRequest request) {
        boolean flag = couponActivityService.addCouponActivity(request);
        return Response.success(flag);
    }

}
