package com.eugene.service.impl;

import com.eugene.controller.request.AddCouponActivityRequest;
import com.eugene.service.ICouponActivityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CouponActivityServiceImpl implements ICouponActivityService {

    private static final Logger log = LoggerFactory.getLogger(CouponActivityServiceImpl.class);

    /**
     * 新建优惠券活动接口
     *
     * @param request 新建优惠券活动的请求数据模型
     * @return 返回新建优惠券活动是否成功
     */
    @Override
    public boolean addCouponActivity(AddCouponActivityRequest request) {

        return false;
    }
}
