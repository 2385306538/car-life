package com.eugene.controller;

import com.eugene.controller.request.AddCouponActivityRequest;
import com.eugene.controller.request.CouponActivityRequest;
import com.eugene.controller.request.ReceiveCouponRequest;
import com.eugene.controller.request.UserCouponRequest;
import com.eugene.controller.response.CouponActivityResponse;
import com.eugene.controller.response.CouponResponse;
import com.eugene.response.Response;
import com.eugene.service.ICouponActivityService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.SneakyThrows;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

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


    @PostMapping("/getCouponCenterList")
    @Operation(summary = "查询领券中心活动列表", description = "查询领券中心活动列表")
    public Response getCouponCenterList(@RequestBody @Valid UserCouponRequest request) {
        List<CouponActivityResponse> couponCenterList = couponActivityService.getCouponCenterList(request);
        return Response.success(couponCenterList);
    }

    @PostMapping("/getCouponActivityDetail")
    @Operation(summary = "查询领券活动详情", description = "查询领券活动详情")
    public Response getCouponActivityDetail(@RequestBody @Valid CouponActivityRequest request) {
        CouponActivityResponse couponActivityResponse = couponActivityService.getCouponActivityDetail(request);
        return Response.success(couponActivityResponse);
    }

    @SneakyThrows
    @PostMapping("/receive")
    @Operation(summary = "用户手动领取优惠券", description = "用户手动领取优惠券")
    public Response receive(@RequestBody @Valid ReceiveCouponRequest request) {
        CouponResponse receive = couponActivityService.receive(request);
        return Response.success(receive);
    }
}
