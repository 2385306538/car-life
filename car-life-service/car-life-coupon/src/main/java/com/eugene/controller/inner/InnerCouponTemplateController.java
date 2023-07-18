package com.eugene.controller.inner;


import com.eugene.controller.request.CouponTemplateRemoteRequest;
import com.eugene.pojo.CouponTemplate;
import com.eugene.response.Response;
import com.eugene.service.ICouponTemplateService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Description 优惠券模版相关
 * @Author 郭爽
 * @Data 2023/4/4 14:53
 */
@RestController
@RequestMapping("/inner/couponTemplate")
public class InnerCouponTemplateController {

    @Resource
    private ICouponTemplateService couponTemplateService;

    @PostMapping("/list")
    public Response list(@RequestBody CouponTemplateRemoteRequest request) {
        List<CouponTemplate> couponTemplates = couponTemplateService.list(request);
        return Response.success(couponTemplates);
    }
}
