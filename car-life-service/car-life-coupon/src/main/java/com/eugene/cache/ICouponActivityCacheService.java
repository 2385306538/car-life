package com.eugene.cache;

import com.eugene.pojo.CouponActivity;

/**
 * 优惠券活动缓存服务
 */
public interface ICouponActivityCacheService {

    /**
     * 将优惠券活动信息设置到缓存中的接口
     * Redis ---> Hash 结构
     *
     * @param couponActivity
     */
    void setCouponActivityCache(CouponActivity couponActivity);

    /**
     * 根据优惠券活动ID查询优惠券活动详情缓存
     * Redis ---> Hash 结构
     *
     * @param couponActivityId
     * @return
     */
    CouponActivity getCouponActivityCache(Long couponActivityId);
}
