package com.eugene.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * 新建优惠券活动的请求数据模型
 */
@Data
public class AddCouponActivityRequest {

    @Schema(description = "活动名称", required = true)
    private String name;

    /**
     * 优惠券模版code：可以理解为优惠券的模板，比如：满200元减20元，满100元减5元，
     *                它只是后台人员定义的一个模板，在新建优惠券活动的时候就可以选择其中一个优惠券模板。
     * 用户优惠券code：可以理解为用户具体领到手里的优惠券，哪怕是同一模板的优惠券，
     *                不同用户领取到手里的用户优惠券code都不相同。
     * 具体的优惠券发放的数量，是由运营人员创建领券活动，会选择使用的优惠券模板，然后指定该活动券总数量
     * 以及每人可领取的数量
     */
    @Schema(description = "优惠券模版code", required = true)
    private String couponTemplateCode;
    @Schema(description = "券总数量 -1不限制", required = true)
    private Long totalNumber;
    @Schema(description = "每人可领取数量", required = true)
    private Long limitNumber;
    @Schema(description = "优惠券状态：0-不可用 1-可用", required = true)
    private Integer status;
    @Schema(description = "活动开始时间", required = true)
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date beginTime;
    @Schema(description = "活动结束时间", required = true)
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;
}
