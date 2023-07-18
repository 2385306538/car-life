package com.eugene.cache.impl;

import cn.hutool.json.JSONUtil;
import com.eugene.cache.ICouponActivityCacheService;
import com.eugene.common.constant.CouponActivityKeyConstant;
import com.eugene.common.constant.CouponCacheKeyConstant;
import com.eugene.pojo.CouponActivity;
import com.eugene.utils.RedisUtil;
import jodd.util.StringUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.eugene.common.constant.CouponCacheKeyConstant.getCouponActivityKey;

@Service
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

    /**
     * 根据优惠券活动ID查询优惠券活动详情缓存
     * Redis ---> Hash 结构
     *
     * @param couponActivityId
     * @return
     */
    @Override
    public CouponActivity getCouponActivityCache(Long couponActivityId) {
        Map<String, Object> couponActivityCacheMap = new HashMap<>();
        // 从 Redis 中查询优惠券活动详情数据
        couponActivityCacheMap = redisUtil.hmget(CouponCacheKeyConstant.getCouponActivityKey(couponActivityId));

        // TODO 判断缓存是否命中，如果未命中，直接返回null，防止缓存穿透
        if (Objects.isNull(couponActivityCacheMap) || StringUtil.isBlank((String) couponActivityCacheMap.get(CouponActivityKeyConstant.ACTIVITY_INFO))) {
            return null;
        }

        // 将缓存中的 JSON 字符串转换为 CouponActivity 对象。
        CouponActivity couponActivityCache = JSONUtil.toBean((String) couponActivityCacheMap.get(CouponActivityKeyConstant.ACTIVITY_INFO), CouponActivity.class);
        // 获取到缓存中的优惠券活动总库存数量，前端会根据这个总数量做相对应的页面展示
        Long totalNumber = Long.valueOf(String.valueOf(couponActivityCacheMap.get(CouponActivityKeyConstant.TOTAL_NUMBER)));

        // 将缓存中查询出来的优惠券活动信息封装并返回
        CouponActivity couponActivity = new CouponActivity();
        couponActivity.setId(couponActivityCache.getId());
        couponActivity.setName(couponActivityCache.getName());
        couponActivity.setCouponTemplateCode(couponActivityCache.getCouponTemplateCode());
        couponActivity.setTotalNumber(totalNumber);
        couponActivity.setLimitNumber(couponActivityCache.getLimitNumber());
        couponActivity.setStatus(couponActivityCache.getStatus());
        couponActivity.setBeginTime(couponActivityCache.getBeginTime());
        couponActivity.setEndTime(couponActivityCache.getEndTime());
        couponActivity.setCreateTime(couponActivityCache.getCreateTime());
        couponActivity.setUpdateTime(couponActivityCache.getUpdateTime());
        return couponActivity;
    }

    /**
     * 根据优惠券活动ID删除优惠券活动缓存
     *
     * @param couponActivityId
     */
    @Override
    public void invalidateCouponActivityCache(Long couponActivityId) {
        redisUtil.del(getCouponActivityKey(couponActivityId));
    }
}
