package com.eugene.service;

import com.eugene.controller.request.AddCouponActivityRequest;

public interface ICouponActivityService {

    /**
     * 新建优惠券活动接口
     *
     * @param request 新建优惠券活动的请求数据模型
     * @return 返回新建优惠券活动是否成功
     */
    boolean addCouponActivity(AddCouponActivityRequest request);
}
