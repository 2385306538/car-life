package com.eugene.pojo;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.eugene.common.enums.CouponStatusEnum;
import com.eugene.common.enums.ValidityTypeEnum;
import com.eugene.controller.request.ReceiveCouponRequest;
import com.eugene.controller.request.SendCouponRequest;
import com.eugene.utils.CouponUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

import static com.eugene.utils.CouponUtil.getCouponCode;

/**
 * @Description 用户优惠券
 * @Author eugene
 * @Data 2023/4/4 14:32
 */
@Data
@TableName("t_coupon")
@EqualsAndHashCode
public class Coupon implements Serializable {
    private static final long serialVersionUID = -145234232599455037L;

    @TableId(type = IdType.AUTO)
    @Schema(description = "id", required = true)
    private Long id;
    @Schema(description = "优惠券模版code", required = true)
    private String couponTemplateCode;
    @Schema(description = "用户优惠券code", required = true)
    private String code;
    @Schema(description = "用户id", required = true)
    private Long userId;
    @Schema(description = "手机号", required = true)
    private Long mobile;
    @Schema(description = "优惠券状态：0-不可用 1-可用 2-已使用 3-已过期", required = true)
    private Integer status;
    @Schema(description = "优惠券开始时间", required = true)
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date beginTime;
    @Schema(description = "优惠券结束时间", required = true)
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;
    @Schema(description = "优惠券领取时间", required = true)
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    @Schema(description = "优惠券使用时间", required = true)
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date useTime;
    @Schema(description = "更新时间", required = true)
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    public Coupon() {
    }

    public Coupon(String couponCode) {
        this.code = couponCode;
    }

    /**
     * 组装手动领券的优惠券信息
     * @param request
     * @param CouponTemplate
     * @return
     */
    public static Coupon buildCoupon(ReceiveCouponRequest request, CouponTemplate CouponTemplate) {
        Coupon coupon = new Coupon();
        coupon.setCouponTemplateCode(CouponTemplate.getCode());

        /**
         * CouponUtil.getCouponCode(CouponTemplate.getId())：保证用户券code唯一
         * 生成优惠券模版Code：
         *          CP + 雪花算法ID
         * 生成优惠券Code：
         *          UCP + 券模版id % 10 + 雪花算法ID
         */
        coupon.setCode(CouponUtil.getCouponCode(CouponTemplate.getId()));
        coupon.setUserId(request.getUserId());
        coupon.setMobile(request.getMobile());
        coupon.setStatus(CouponStatusEnum.AVAILABLE.getCode());

        /**
         * 判断优惠券模板是否为起止日期
         * 也就是用户领取的这张优惠券是：
         *                   起止日期内生效？
         *                   有效天数内生效？
         */
        if (ValidityTypeEnum.isDeadline(CouponTemplate.getValidityType())) {
            // 说明起止日期内生效
            coupon.setBeginTime(CouponTemplate.getBeginTime()); // 设置优惠券开始使用时间 ---> 用户优惠券表
            coupon.setEndTime(CouponTemplate.getEndTime()); // 设置优惠券结束使用时间 ---> 用户优惠券表
        } else if (ValidityTypeEnum.isEffectiveDay(CouponTemplate.getValidityType())) {
            // 说明是有效天数内生效，计算有效天数
            Long validityDay = CouponTemplate.getValidityDay(); // 获取有效天数
            Date currentDate = new Date(); // 获取当前时间
            coupon.setBeginTime(currentDate); // 设置优惠券开始使用时间 ---> 用户优惠券表
            // 结束时间设置为当前日期 currentDate 加上 validityDay 天之后的日期。 ---> 用户优惠券表
            coupon.setEndTime(DateUtil.offsetDay(currentDate, Math.toIntExact(validityDay)));
        }
        coupon.setCreateTime(new Date());
        coupon.setUpdateTime(new Date());
        return coupon;
    }

    /**
     * 组装自动发券的优惠券信息
     * @param request
     * @param couponTemplateCache
     * @return
     */
    public static Coupon getCoupon(SendCouponRequest request, CouponTemplate couponTemplateCache) {
        Coupon coupon = new Coupon();
        coupon.setCouponTemplateCode(request.getCouponTemplateCode());

        /**
         * 自动发放优惠券，每一次都会重新生成一个优惠券code，需要保证券code不重复
         * CouponUtil.getCouponCode(CouponTemplate.getId())：保证用户券code唯一
         * 生成优惠券模版Code：
         *          CP + 雪花算法ID
         * 生成优惠券Code：
         *          UCP + 券模版id % 10 + 雪花算法ID
         */
        coupon.setCode(getCouponCode(couponTemplateCache.getId()));
        coupon.setUserId(request.getUserId());
        coupon.setMobile(request.getMobile());
        coupon.setStatus(CouponStatusEnum.AVAILABLE.getCode());
        /**
         * 判断优惠券模板是否为起止日期
         * 也就是用户领取的这张优惠券是：
         *                   起止日期内生效？
         *                   有效天数内生效？
         */
        if (ValidityTypeEnum.isDeadline(couponTemplateCache.getValidityType())) {
            // 说明起止日期内生效
            coupon.setBeginTime(couponTemplateCache.getBeginTime()); // 设置优惠券开始使用时间 ---> 用户优惠券表
            coupon.setEndTime(couponTemplateCache.getEndTime()); // 设置优惠券结束使用时间 ---> 用户优惠券表
        } else if (ValidityTypeEnum.isEffectiveDay(couponTemplateCache.getValidityType())) {
            // 说明是有效天数内生效，计算有效天数
            Long validityDay = couponTemplateCache.getValidityDay(); // 获取有效天数
            Date currentDate = new Date(); // 获取当前时间
            coupon.setBeginTime(currentDate); // 设置优惠券开始使用时间 ---> 用户优惠券表
            // 结束时间设置为当前日期 currentDate 加上 validityDay 天之后的日期。 ---> 用户优惠券表
            coupon.setEndTime(DateUtil.offsetDay(currentDate, Math.toIntExact(validityDay)));
        }
        coupon.setCreateTime(new Date());
        coupon.setUpdateTime(new Date());
        return coupon;
    }
}
