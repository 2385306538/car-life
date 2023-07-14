package com.eugene.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.eugene.cache.ICouponActivityCacheService;
import com.eugene.common.enums.StatusEnum;
import com.eugene.controller.request.AddCouponActivityRequest;
import com.eugene.controller.request.UserCouponRequest;
import com.eugene.controller.response.CouponActivityResponse;
import com.eugene.mapper.CouponActivityMapper;
import com.eugene.pojo.CouponActivity;
import com.eugene.service.ICouponActivityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.eugene.controller.response.CouponActivityResponse.buildCouponActivityResponse;

@Service
public class CouponActivityServiceImpl implements ICouponActivityService {

    private static final Logger log = LoggerFactory.getLogger(CouponActivityServiceImpl.class);

    @Resource
    private CouponActivityMapper couponActivityMapper;

    @Resource
    private ICouponActivityCacheService couponActivityCacheService;

    /**
     * 新建优惠券活动接口
     *
     * @param request 新建优惠券活动的请求数据模型
     * @return 返回新建优惠券活动是否成功的布尔值
     */
    @Override
    public boolean addCouponActivity(AddCouponActivityRequest request) {
        // 新建优惠券活动表实体类，将活动信息封装到该实体类中
        CouponActivity couponActivity = new CouponActivity();
        couponActivity.setName(request.getName());
        couponActivity.setCouponTemplateCode(request.getCouponTemplateCode());
        couponActivity.setTotalNumber(request.getTotalNumber());
        couponActivity.setLimitNumber(request.getLimitNumber());
        couponActivity.setStatus(request.getStatus());
        couponActivity.setBeginTime(request.getBeginTime());
        couponActivity.setEndTime(request.getEndTime());
        couponActivity.setCreateTime(new Date());
        couponActivity.setUpdateTime(new Date());
        // 将新建的优惠券活动添加到数据库中
        int flag = couponActivityMapper.insert(couponActivity);

        // 将优惠券活动信息存储到缓存中
        if (flag > 0) {
            // 小系统可以直接存到数据库，但是为了提高性能，支持高并发系统，需要保存到Redis缓存中支撑高并发。
            couponActivityCacheService.setCouponActivityCache(couponActivity);
            return true;
        }
        return false;
    }

    /**
     * 查询领券中心活动列表。
     * 活动信息创建完成之后，用户就可以从领券中心查询该活动。
     * 查询领券中心可参与活动列表，该方法就是用来查询用户在领券中心可参与领券活动的活动列表方法，
     * 所以在访问该接口的时候，需要携带用户ID与手机号，因为每个用户在领券中心活动参与情况不一样。
     * 小技巧：
     * 返回数据的时候，建议不要直接使用实体类对象返回，虽然这样可以偷懒，但是不方便维护、拓展，
     * 建议自己封装一个响应实体对象，尽管里面的字段跟实体类字段一模一样，但是方便后期维护、拓展。
     *
     * @param request
     * @return 返回封装的响应实体对象。
     */
    @Override
    public List<CouponActivityResponse> getCouponCenterList(UserCouponRequest request) {
        /**
         * 从数据库中查询优惠券活动表
         *  todo 优化改为查询Redis，需在新建优惠券活动接口将优惠券活动信息存储到缓存中
         *
         * 将所有活动为有效状态的活动信息查询出来。
         * 优惠券状态：0：不可用    1：可用
         * 为啥在新建优惠券活动时需要给优惠券活动设置一个活动状态？
         *      在优惠券系统中，运营人员在添加优惠券时通常需要设置该优惠券的可用状态。
         *      可用状态是指优惠券是否可以被用户领取和使用的状态。运营人员可以通过设置可用状态
         *      来控制优惠券的发布和生效时间，以及是否对特定用户或用户组进行限制。
         *
         *      举例来说，当一个优惠券被创建后，它的初始状态可能是不可用的。然后，运营人员可以
         *      设置优惠券的可用状态为 "可用"，使得用户可以在指定的时间范围内领取和使用该优惠券。
         *      类似地，当某个优惠活动结束后，运营人员可以将该优惠券的可用状态设置为 "不可用"，
         *      使得用户无法再使用该优惠券。
         *
         *      通过设置优惠券的可用状态，运营人员可以更加灵活地控制优惠券的发放和使用，以满足
         *      业务需求和活动策划。
         */
        QueryWrapper<CouponActivity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", StatusEnum.AVAILABLE.getCode());
        List<CouponActivity> couponActivities = couponActivityMapper.selectList(queryWrapper);

        /**
         * receivedNumber：当前已领取数量。
         * 当我们从数据库查询出来活动信息之后，需要判断当前该用户还能不能够参与该活动，也就是该用户以前有没有领取过
         * 比如活动列表中的某个活动设置的就是每个人只能领取一次，此时如果该用户已经参与过了，进入到该前端页面的时候
         * 前端页码应该显示已参与或已领取的标识，从业务场景下需要在前端对用户做一些提示。
         *
         * 所以在包装数据并向前端返回数据结果的时候，要重新判断一次，该活动用户还能够领取几次，还能否参与该活动。
         * 此处为了简化起见，当前已领取数量填写的是0， buildCouponActivityResponse(couponActivity, 0L)。
         * 如果是真实场景的话，需要改成当前用户真实已领取的数量。
         * 如果用户可领取数量是1个，前已领取数量是0个，证明用户还可以参与该活动。
         * 如果用户可领取数量是1个，前已领取数量是1个，证明用户已经参与过了该活动，前端展示相对应的样式即可。
         *      limitNumber：每人可领取数量
         *      receivedNumber：当前已领取数量
         *
         * buildCouponActivityResponse()：将查询出来的优惠券活动集合数据进行包装
         *
         *   todo receivedNumber（当前已领取数量） 取真实领取数量 改为查询Redis
         *                 a.如下代码只是示例，目前还没找到合适的方法从活动日志记录表中，获取每一场活动该用户当前已领取数量。
         *                 b.当然这只是一种业务场景：
         *                      i.用户可以多次参与该活动，只有它还有领券次数。
         *                 c.大部分的业务场景是：
         *                      i.用户只能参与一次该领券活动，此时就可以通过设置一个字段，来判断
         *                        用户有没有参与该活动，根据该字段的值，前端给出对应的提示信息即可。
         *               for (CouponActivity couponActivity : couponActivities) {
         *                   QueryWrapper<CouponActivityLog> queryWrapper = new QueryWrapper<>();
         *                   queryWrapper.eq("coupon_activity_id",  couponActivity.getId());
         *                   queryWrapper.eq("mobile", request.mobile());
         *                   Integer receivedNumber = couponActivityLogMapper.selectCount(queryWrapper);
         *               }
         *
         *  stream流代码含义：根据 couponActivities 列表中的每个 CouponActivity 对象，生成
         *  相应的 CouponActivityResponse 对象，并将它们放入一个新的列表中返回。
         */
        List<CouponActivityResponse> couponActivityResponses = couponActivities.stream()
                .map(couponActivity -> buildCouponActivityResponse(couponActivity, 0L))
                .collect(Collectors.toList());

        return couponActivityResponses;
    }
}
