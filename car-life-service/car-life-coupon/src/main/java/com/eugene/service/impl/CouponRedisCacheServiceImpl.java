package com.eugene.service.impl;

import cn.hutool.json.JSONUtil;
import com.eugene.pojo.Coupon;
import com.eugene.service.ICouponCacheService;
import com.eugene.utils.CouponRedisLuaUtil;
import com.eugene.utils.RedisUtil;
import jodd.util.StringUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


import static com.eugene.common.constant.CouponCacheKeyConstant.getCouponKey;
import static com.eugene.common.constant.CouponCacheKeyConstant.getUserCouponKey;
import static com.eugene.utils.CouponUtil.calcCouponExpireTime;

/**
 * @Description 用户优惠券实现类
 * @Author eugene
 * @Data 2023/4/7 10:17
 */
@Service
public class CouponRedisCacheServiceImpl implements ICouponCacheService {

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private CouponRedisLuaUtil couponRedisLuaUtil;

    /**
     * 保存优惠券缓存信息，设置失效时间为过期时间
     *
     * @param coupon 优惠券信息
     * @return
     */
    @Override
    public boolean setCouponCache(Coupon coupon) {
        return redisUtil.set(getCouponKey(coupon.getCode()), JSONUtil.toJsonStr(coupon), calcCouponExpireTime(coupon.getBeginTime(), coupon.getEndTime()));
    }

    /**
     * 保存优惠券缓存信息，设置指定的失效时间
     *
     * @param coupon
     * @param time
     * @param timeUnit
     * @return
     */
    @Override
    public boolean setCouponCache(Coupon coupon, Long time, TimeUnit timeUnit) {
        return redisUtil.set(getCouponKey(coupon.getCode()), JSONUtil.toJsonStr(coupon), time, timeUnit);
    }

    /**
     * 添加用户拥有的优惠券列表
     *
     * @param mobile 手机号
     * @return
     */
    @Override
    public boolean addUserCouponCode(Long mobile, String couponCode) {
        // Key：手机号，Value：券code
        return redisUtil.lSet(getUserCouponKey(mobile), couponCode);
    }

    /**
     * 查询用户拥有的优惠券列表
     *
     * @param mobile 手机号
     * @return 返回优惠券Code列表
     */
    @Override
    public List<String> getUserCouponCodeList(Long mobile) {
        return redisUtil.lGet(
                getUserCouponKey(mobile),
                0,
                500
        );
    }

    /**
     * 批量查询优惠券缓存
     *
     * @param codes 惠券Code列表
     * @return 返回优惠券列表
     */
    @Override
    public List<Coupon> batchGetCouponCache(List<String> codes) {
        List<Coupon> coupons = codes.stream()
                .map(code -> JSONUtil.toBean((String) redisUtil.get(getCouponKey(code)), Coupon.class))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        return coupons;
    }

    /**
     * 批量保存优惠券信息缓存，并设置券过期时间
     *
     * @param coupons
     * @return
     */
    @Override
    public boolean batchSetCouponCache(List<Coupon> coupons) {
        boolean flag = couponRedisLuaUtil.batchSet(coupons);
        return flag;
    }

    /**
     * 根据券code查询优惠券缓存
     *
     * @param code
     * @return
     */
    @Override
    public Coupon getCouponCache(String code) {
        // TODO 使用Guava缓存
        String result = (String) redisUtil.get(getCouponKey(code));
        return StringUtil.isNotEmpty(result) ? JSONUtil.toBean(result, Coupon.class) : null;
    }

    @Override
    public boolean delUserCouponCode(Coupon coupon) {
        return redisUtil.lRemove(getUserCouponKey(coupon.getMobile()), 0L, coupon.getCode()) > 0;
    }

    @Override
    public boolean delCouponCache(Coupon coupon) {
        return redisUtil.del(getCouponKey(coupon.getCode())) > 0;
    }
}
