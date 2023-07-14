package com.eugene.cache.impl;

import cn.hutool.json.JSONUtil;
import com.eugene.cache.ICouponActivityCacheService;
import com.eugene.common.constant.CouponActivityKeyConstant;
import com.eugene.common.constant.CouponCacheKeyConstant;
import com.eugene.pojo.CouponActivity;
import com.eugene.utils.RedisUtil;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

public class CouponActivityCacheServiceImpl implements ICouponActivityCacheService {

    @Resource
    private RedisUtil redisUtil;

    /**
     * 将优惠券活动信息设置到缓存中的接口
     * @param couponActivity
     */
    @Override
    public void setCouponActivityCache(CouponActivity couponActivity) {
        // 优惠券活动信息在Redis中存储的是Hash结构
        Map<String, Object> couponActivityCacheMap = new HashMap<>();

        /**
         * ACTIVITY_INFO ---> 活动基础信息
         * TOTAL_NUMBER ---> 活动的总库存数量
         * 为啥要把活动总库存数量单独的拧出来？我们手动领取，当每一位用户参与之后，活动库存数就应该减少一个
         * 所以每一次用户领券后活动的总库存数量都会变动，目的是为了方便变更。
         * 我们当然也可以只存储活动基础信息的Hash，但是很多活动的字段是不怎么会发生变化的，真正变化的一般就
         * 只有总库存数，所以就单独的提了出来。
         *
         * JSONUtil.toJsonStr(couponActivity)：将实体类对象序列化为 JSON 字符串表示形式。
         */
        couponActivityCacheMap.put(CouponActivityKeyConstant.ACTIVITY_INFO, JSONUtil.toJsonStr(couponActivity));
        couponActivityCacheMap.put(CouponActivityKeyConstant.TOTAL_NUMBER, String.valueOf(couponActivity.getTotalNumber()));
        redisUtil.hmset(CouponCacheKeyConstant.getCouponActivityKey(couponActivity.getId()), couponActivityCacheMap);
    }
}
