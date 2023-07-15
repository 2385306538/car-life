package com.eugene.utils;

import cn.hutool.json.JSONUtil;
import com.eugene.common.constant.CouponActivityKeyConstant;
import com.eugene.controller.request.ReceiveCouponRequest;
import com.eugene.pojo.Coupon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.eugene.common.constant.CouponCacheKeyConstant.*;
import static com.eugene.utils.CouponUtil.calcCouponExpireTime;

/**
 * redis工具类
 */
@Component
public class CouponRedisLuaUtil {

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 批量保存优惠券信息缓存，并设置券过期时间
     * todo 这里有两个不同的Key，所以操作了两次，后续需要优化成一次执行
     *
     * @param coupons
     * @return
     */
    public boolean batchSet(List<Coupon> coupons) {
        List<String> couponKeys = new ArrayList<>();
        List<String> userCouponKeys = new ArrayList<>();
        StringBuffer couponScript = new StringBuffer();
        StringBuffer userCouponScript = new StringBuffer();
        for (int i = 1; i <= coupons.size(); i++) {
            Coupon coupon = coupons.get(i - 1);
            // 1.保存用户优惠券信息缓存
            couponScript.append("redis.call('setnx', KEYS[" + i + "], '" + JSONUtil.toJsonStr(coupon) + "')");
            // 2.设置优惠券信息过期时间
            couponScript.append("redis.call('expire', KEYS[" + i + "], '" + calcCouponExpireTime(new Date(), coupon.getEndTime()) + "')");
            couponKeys.add(String.format(COUPON_KEY, coupon.getCode()));
            // 3.保存用户拥有的优惠券列表
            userCouponScript.append("redis.call('lpush', KEYS[" + i + "], '" + coupon.getCode() + "')");
            userCouponKeys.add(String.format(USER_COUPON_KEY, coupon.getMobile()));
        }
        DefaultRedisScript couponRedisScript = new DefaultRedisScript();
        couponRedisScript.setScriptText(couponScript.toString());
        DefaultRedisScript userCouponRedisScript = new DefaultRedisScript();
        userCouponRedisScript.setScriptText(userCouponScript.toString());
        try {
            redisTemplate.execute(couponRedisScript, couponKeys);
            redisTemplate.execute(userCouponRedisScript, userCouponKeys);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


    public boolean receive(ReceiveCouponRequest request) {
        /**
         * 领券分多步操作：
         *      1.扣减领券活动库存，
         *               如果失败：则回滚扣减领券活动库存
         *               如果成功：返回最新库存数量
         */
        // 多个人并发抢库存，如果当库存数量不够的时候，可能导致领券失败的场景，此时需要走回滚策略。
        // 所有跟库存相关的，如：优惠券库存，商品库存，订单库存等，都需要有回滚操作。
        // 这种回滚操作，如果只用Redis来做的话，因为Redis是单线程的，是原子性的，并不支持事务回滚。
        // 要想保证多个命令的原子性的话，此时就需要使用lua脚本，让它一起把多个命令统一执行。Redis + lua脚本结合起来非常好用
        String script = "local newTotalNumber = redis.call('HINCRBY', KEYS[1], KEYS[2], -ARGV[1]); " + // 扣减库存
                "if (newTotalNumber < 0) then " + // 判断是否扣减成功，小于0说明扣减失败，需要回滚
                "redis.call('HINCRBY', KEYS[1], KEYS[2], ARGV[1]); return -1; " + // 回滚操作，并返回-1，后续代码会使用该返回结果
                "else return newTotalNumber end;"; // 说明扣减库存成功，返回最新的库存数量
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript();
        redisScript.setScriptText(script);
        List<String> keys = new ArrayList<>();

        // 优惠券活动使用的是Hash结构。
        // Key：优惠券活动ID， Field：TOTAL_NUMBER活动的总库存数量 --- 详见添加新建优惠券活动缓存方法
        keys.add(getCouponActivityKey(request.getCouponActivityID()));
        keys.add(CouponActivityKeyConstant.TOTAL_NUMBER);
        redisScript.setResultType(Long.class);

        // request.getNumber()：需要变更的值大小
        Long totalNumber = (Long) redisTemplate.execute(redisScript, keys, String.valueOf(request.getNumber()));
        return totalNumber != null && totalNumber > -1;
    }
}