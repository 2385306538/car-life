package com.eugene.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 查询领券中心活动列表的请求数据模型
 *
 */
@Data
public class UserCouponRequest {

    @Schema(description = "用户id", required = true)
    @NotNull(message = "user is not null")
    private Long userId;
    @Schema(description = "手机号", required = true)
    @NotNull(message = "mobile is not null")
    private Long mobile;


}
