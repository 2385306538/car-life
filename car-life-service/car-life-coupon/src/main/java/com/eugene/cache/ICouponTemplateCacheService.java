package com.eugene.cache;

import com.eugene.pojo.CouponTemplate;

/**
 * @Description 券模版Guava缓存
 * @Author 郭爽
 * @Data 2023/4/5 18:38
 */
public interface ICouponTemplateCacheService {

    /**
     * 将优惠券模板信息保存到 Guava 缓存中
     *
     * @param couponTemplateCode 优惠券模板Code
     * @param couponTemplate 优惠券模板信息
     */
    void setCouponTemplateCache(String couponTemplateCode, CouponTemplate couponTemplate);

    /**
     * 从 Guava 缓存中获取优惠券模板信息
     * @param couponTemplateCode 优惠券模板Code
     * @return
     */
    CouponTemplate getCouponTemplateCache(String couponTemplateCode);

    /**
     * 根据优惠券模板ID删除优惠券模板缓存
     *
     * @param couponTemplateCode
     */
    void invalidateCouponTemplateCache(String couponTemplateCode);
}
