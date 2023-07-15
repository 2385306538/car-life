package com.eugene.service;

import com.eugene.pojo.CouponTemplate;

public interface ICouponTemplateCacheService {



    /**
     * 从缓存中获取优惠券模板信息
     * @param couponTemplateCode 优惠券模板Code
     * @return
     */
    CouponTemplate getCouponTemplateCache(String couponTemplateCode);
}
