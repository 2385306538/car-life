package com.eugene.controller.response;

import com.eugene.common.enums.CouponStatusEnum;
import com.eugene.pojo.Coupon;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @Description 用户优惠券
 * @Author eugene
 * @Data 2023/4/4 14:32
 */
@Data
public class CouponResponse implements Serializable {

    private static final long serialVersionUID = -8291797299937978757L;

    /**
     * 优惠券模版code：可以理解为优惠券的模板，比如：满200元减20元，满100元减5元，
     *                它只是后台人员定义的一个模板，在新建优惠券活动的时候就可以选择其中一个优惠券模板。
     * 用户优惠券code：可以理解为用户具体领到手里的优惠券，哪怕是同一模板的优惠券，
     *                不同用户领取到手里的用户优惠券code都不相同。
     * 具体的优惠券发放的数量，是由运营人员创建领券活动，会选择使用的优惠券模板，然后指定该活动券总数量
     * 以及每人可领取的数量
     */
    @Schema(description = "优惠券模版code")
    private String coupon_template_code;
    @Schema(description = "用户优惠券code")
    private String code;

    @Schema(description = "用户id")
    private Long userId;
    @Schema(description = "手机号")
    private Long mobile;
    @Schema(description = "优惠券状态：0-不可用 1-可用 2-已使用 3-已过期")
    private Integer status;
    @Schema(description = "优惠券开始时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date beginTime;
    @Schema(description = "优惠券结束时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;
    @Schema(description = "优惠券领取时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    @Schema(description = "优惠券使用时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date useTime;

    /**
     * 组装优惠券结果返回
     *
     * @param coupon
     * @return
     */
    public static CouponResponse buildCouponResponse(Coupon coupon) {
        CouponResponse response = new CouponResponse();
        response.setCoupon_template_code(coupon.getCouponTemplateCode());
        response.setCode(coupon.getCode());
        response.setUserId(coupon.getUserId());
        response.setMobile(coupon.getMobile());

        /**
         * NOT_AVAILABLE(0, "不可用"),
         * AVAILABLE(1, "可用"),
         * USED(2, "已使用"),
         * EXPIRED(3, "已过期");
         *
         * 什么情况下优惠券会出现不可用状态？
         *      真实场景下：优惠券有开始使用时间，假设开始使用时间还没到，比如说双十一早上8点之后才能开始使用
         *      这张优惠券不一定会在双十一早上8点才发出来，可能在双十一前几天就将该优惠券发出来了。
         *      用来吸引用户，这是一张很大额的优惠券，但是就得到双十一那天才能开始使用，这叫宣传，提前埋伏笔。
         *      也就是说在双十一之前是用不了的，所以在这种场景下，要给用户返回不可用状态。
         *      此时用户只能查看，不能使用。
         */
        // 检查优惠券状态是否不可用
        response.setStatus(checkIsNotAvailable(coupon.getBeginTime()) ?
                Integer.valueOf(CouponStatusEnum.NOT_AVAILABLE.getCode()) : coupon.getStatus());
        response.setBeginTime(coupon.getBeginTime());
        response.setEndTime(coupon.getEndTime());
        response.setCreateTime(coupon.getCreateTime());
        response.setUseTime(coupon.getUseTime());
        return response;
    }

    /**
     * 校验优惠券是否不可用
     *
     * @param beginTime
     * @return
     */
    private static boolean checkIsNotAvailable(Date beginTime) {
        return beginTime.after(new Date());
    }

}
