package com.eugene.common.constant;

/**
 * @Description TODO
 * @Author eugene
 * @Data 2023/4/7 15:30
 */
public class CouponCacheKeyConstant {

    /**
     * 券信息KEY
     * String结构
     */
    public static final String COUPON_KEY = "mall_coupon:coupon:%s";
    ;


    /**
     * 用户拥有的券列表KEY
     * List结构
     */
    public static final String USER_COUPON_KEY = "mall_coupon:userCoupon:%s";

    /**
     * 用户拥有的券列表KEY：String结构
     * 优惠券活动KEY：Hash结构
     */
    public static final String COUPON_ACTIVITY_KEY = "mall_coupon:couponActivity:%s";


    public static String getCouponKey(String code) {
        return String.format(CouponCacheKeyConstant.COUPON_KEY, code);
    }

    public static String getUserCouponKey(Long mobile) {
        return String.format(CouponCacheKeyConstant.USER_COUPON_KEY, mobile);
    }

    public static String getCouponActivityKey(Long couponActivityId) {
        /**
         * 使用 String.format() 方法将 couponActivityId 替换到字符串模板中，生成一个新的字符串。
         */
        return String.format(CouponCacheKeyConstant.COUPON_ACTIVITY_KEY, couponActivityId);
    }


}
