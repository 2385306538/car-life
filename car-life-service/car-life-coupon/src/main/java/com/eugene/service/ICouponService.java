package com.eugene.service;


import com.eugene.controller.request.SendCouponRequest;
import com.eugene.controller.request.UserCouponRequest;
import com.eugene.controller.request.VerificationCouponRequest;
import com.eugene.controller.response.CouponResponse;

import java.util.List;

/**
 * @Description TODO
 * @Author eugene
 * @Data 2023/4/5 14:59
 */
public interface ICouponService {


    /**
     * 用户领取完成优惠券之后，可通过此接口查看个人所拥有的优惠券列表
     *
     * @param request 用户ID、手机号
     * @return 返回该用户拥有的优惠券集合
     */
    List<CouponResponse> getList(UserCouponRequest request);

    /**
     * 自动发放优惠券接口   TODO 优化改为多线程自动发券
     * 手动领券场景一般来说用户只会领取一张，几乎不存在一次领多张优惠券的场景，如果是一次领取多张券的场景，
     * 一定调用的是发送优惠券的接口。
     * 在自动发券的接口会涉及到同一张券发多张的场景，需要做一个批量操作。
     *
     * @param request 用户ID、手机号、券模板code、发放的优惠券数量
     * @return 返回是否发放成功
     */
    Boolean send(SendCouponRequest request);

    /**
     * 当用户查询到了优惠券列表信息之后，点击某张优惠券，可以查看当前选中的优惠券的具体详情信息
     *
     * @param code 用户优惠券code
     * @return 组装返回优惠券信息
     */
    CouponResponse getCoupon(String code);

    /**
     * 用户使用优惠券
     *
     * @param request 用户ID、手机号、优惠券code
     * @return 返回是否使用优惠券成功
     */
    Boolean verification(VerificationCouponRequest request);

}
