package com.eugene.cache;

import com.eugene.pojo.CouponActivity;

/**
 * 优惠券活动缓存服务
 */
public interface ICouponActivityCacheService {

    /**
     * 将优惠券活动信息设置到缓存中的接口
     * @param couponActivity
     */
    void setCouponActivityCache(CouponActivity couponActivity);
}
