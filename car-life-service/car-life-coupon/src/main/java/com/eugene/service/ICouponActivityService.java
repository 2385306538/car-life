package com.eugene.service;

import com.eugene.controller.request.AddCouponActivityRequest;
import com.eugene.controller.request.UserCouponRequest;
import com.eugene.controller.response.CouponActivityResponse;

import java.util.List;

public interface ICouponActivityService {

    /**
     * 新建优惠券活动接口。
     *
     * @param request 新建优惠券活动的请求数据模型。
     * @return 返回新建优惠券活动是否成功。
     */
    boolean addCouponActivity(AddCouponActivityRequest request);

    /**
     * 查询领券中心活动列表。
     * 活动信息创建完成之后，用户就可以从领券中心查询该活动。
     * 查询领券中心可参与活动列表，该方法就是用来查询用户在领券中心可参与领券活动的活动列表方法，
     * 所以在访问该接口的时候，需要携带用户ID与手机号，因为每个用户在领券中心活动参与情况不一样。
     * 小技巧：
     *      返回数据的时候，建议不要直接使用实体类对象返回，虽然这样可以偷懒，但是不方便维护、拓展，
     *      建议自己封装一个响应实体对象，尽管里面的字段跟实体类字段一模一样，但是方便后期维护、拓展。
     *      就不会去修改实体类，而是可以直接修改响应对象即可。
     *
     * @param request
     * @return 返回封装的响应实体对象。
     */
    List<CouponActivityResponse> getCouponCenterList(UserCouponRequest request);
}
