package com.eugene.service;


import com.eugene.controller.request.CouponTemplateRemoteRequest;
import com.eugene.controller.request.CouponTemplateRequest;
import com.eugene.pojo.CouponTemplate;

import java.util.List;

/**
 * @Description TODO
 * @Author 郭爽
 * @Data 2023/4/4 15:19
 */
//@Deprecated
public interface ICouponTemplateService {

    /**
     * 添加优惠券模板信息
     *
     * @param request 优惠券模板参数
     * @return 返回添加是否成功
     */
    boolean addCouponTemplate(CouponTemplateRequest request);

    /**
     * 查询优惠券模板信息
     *
     * @param couponTemplateCode 优惠券模板code
     * @return 返回优惠券模板信息
     */
    CouponTemplate getCouponTemplate(String couponTemplateCode);


    List<CouponTemplate> list(CouponTemplateRemoteRequest request);
}
