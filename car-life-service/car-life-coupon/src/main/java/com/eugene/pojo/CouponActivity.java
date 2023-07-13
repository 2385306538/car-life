package com.eugene.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @Description 优惠券活动表
 */
@Data
@TableName("t_coupon_activity")
@EqualsAndHashCode
public class CouponActivity implements Serializable {
    private static final long serialVersionUID = 3022424369475697485L;

    @TableId(type = IdType.AUTO)
    @Schema(description = "id", required = true)
    private Long id;
    @Schema(description = "活动名称", required = true)
    private String name;
    @Schema(description = "优惠券模版code", required = true)
    private String couponTemplateCode;

    // 从业务场景出发，券的总数量可以进行设置，还有一种场景是券可以不设置总数量
    // 有些活动是常年进行，用户参与完了之后还可以继续参与，不需要设置券的总数量，否则还得去给该优惠券补库存
    @Schema(description = "券总数量    -999999999：不限制", required = true)
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
    @Schema(description = "创建时间", required = true)
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    @Schema(description = "更新时间", required = true)
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    /**
     * 判断该优惠券是否存在数量限制，如果是 -99999999就不会扣库存了
     * true：说明券有总数量限制 ---> 需要扣减库存
     * false：说明券总数量不限制 ---> 不需要扣减库存
     * @return
     */
    public boolean existLimit() {
        return !Long.valueOf(-999999999).equals(this.totalNumber);
    }

}
