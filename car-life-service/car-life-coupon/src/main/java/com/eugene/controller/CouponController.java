package com.eugene.controller;

import com.eugene.controller.request.SendCouponRequest;
import com.eugene.controller.request.UserCouponRequest;
import com.eugene.controller.request.VerificationCouponRequest;
import com.eugene.controller.response.CouponResponse;
import com.eugene.response.Response;
import com.eugene.service.ICouponService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.SneakyThrows;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * @Description 优惠券相关
 * @Author 郭爽
 * @Data 2023/4/5 14:56
 */
@RestController
@RequestMapping("/coupon")
public class CouponController {
    @Resource
    private ICouponService couponService;

    @PostMapping("/getList")
    @Operation(summary = "查询用户优惠券列表", description = "查询用户优惠券列表")
    public Response getList(@RequestBody @Valid UserCouponRequest request) {
        List<CouponResponse> list = couponService.getList(request);
        return Response.success(list);
    }

    @PostMapping("/send")
    @Operation(summary = "自动发放优惠券", description = "自动发放优惠券")
    public Response send(@RequestBody SendCouponRequest request) {
        Boolean flag = couponService.send(request);
        return Response.success(flag);
    }

    @GetMapping("/getCoupon")
    @Operation(summary = "查询优惠券信息", description = "查询优惠券信息")
    public Response getCoupon(@RequestParam("code") @NotBlank String code) {
        CouponResponse coupon = couponService.getCoupon(code);
        return Response.success(coupon);
    }

    @SneakyThrows
    @PostMapping("/verification")
    @Operation(summary = "去使用核销优惠券", description = "去使用核销优惠券")
    public Response verification(@RequestBody @Valid VerificationCouponRequest request) {
        Boolean flag = couponService.verification(request);
        return Response.success(flag);
    }
}
