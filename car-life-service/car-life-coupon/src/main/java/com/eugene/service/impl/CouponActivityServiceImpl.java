package com.eugene.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.eugene.cache.ICouponActivityCacheService;
import com.eugene.common.constant.RedisKeyConstant;
import com.eugene.common.enums.StatusEnum;
import com.eugene.common.exception.BusinessException;
import com.eugene.controller.request.AddCouponActivityRequest;
import com.eugene.controller.request.CouponActivityRequest;
import com.eugene.controller.request.ReceiveCouponRequest;
import com.eugene.controller.request.UserCouponRequest;
import com.eugene.controller.response.CouponActivityResponse;
import com.eugene.controller.response.CouponResponse;
import com.eugene.mapper.CouponActivityLogMapper;
import com.eugene.mapper.CouponActivityMapper;
import com.eugene.mapper.CouponMapper;
import com.eugene.pojo.Coupon;
import com.eugene.pojo.CouponActivity;
import com.eugene.pojo.CouponActivityLog;
import com.eugene.pojo.CouponTemplate;
import com.eugene.service.ICouponActivityService;
import com.eugene.service.ICouponCacheService;
import com.eugene.cache.ICouponTemplateCacheService;
import com.eugene.utils.CouponRedisLuaUtil;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.eugene.common.enums.Errors.NOT_JOIN_RECEIVE_COUPON_ERROR;
import static com.eugene.common.enums.Errors.RECEIVE_COUPON_ERROR;
import static com.eugene.controller.response.CouponActivityResponse.buildCouponActivityResponse;
import static com.eugene.controller.response.CouponResponse.buildCouponResponse;
import static com.eugene.pojo.Coupon.buildCoupon;

@Service
public class CouponActivityServiceImpl implements ICouponActivityService {

    private static final Logger log = LoggerFactory.getLogger(CouponActivityServiceImpl.class);

    @Resource
    private CouponActivityMapper couponActivityMapper;

    @Resource
    private ICouponActivityCacheService couponActivityCacheService;

    @Resource
    private CouponActivityLogMapper couponActivityLogMapper;

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private CouponRedisLuaUtil couponRedisLuaUtil;

    @Resource
    private ICouponCacheService couponCacheService;

    @Resource
    private CouponMapper couponMapper;

    @Resource
    private ICouponTemplateCacheService couponTemplateCacheService;

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
     * @param request 前端会携带用户ID与手机号
     * @return 返回封装的响应实体对象。
     */
    @Override
    public List<CouponActivityResponse> getCouponCenterList(UserCouponRequest request) {

        /**
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
        // 从数据库中查询优惠券活动表
        // TODO 优化改为查询Redis，需在新建优惠券活动接口将优惠券活动信息存储到缓存中
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
         * 如果用户可领取数量是1个，当前已领取数量是0个，证明用户还可以参与该活动。
         * 如果用户可领取数量是1个，当前已领取数量是1个，证明用户已经参与过了该活动，前端展示相对应的样式即可。
         *      limitNumber：每人可领取数量
         *      receivedNumber：当前已领取数量
         *
         * buildCouponActivityResponse()：将查询出来的优惠券活动集合数据进行包装
         *
         *   TODO receivedNumber（当前已领取数量） 取真实领取数量 优化改为查询Redis
         *                 a.如下代码只是示例，目前还没找到合适的方法从活动日志记录表中，获取每一场活动该用户当前已领取数量。
         *                 b.当然这只是一种业务场景：
         *                      i.用户可以多次参与该活动，只要它还有领券次数。
         *                 c.大部分的业务场景是：
         *                      i.用户只能参与一次该领券活动，此时就可以通过设置一个字段，来判断
         *                        用户有没有参与该活动，根据该字段的值，前端给出对应的提示信息即可。
         *                      ii.或者说领券中心只能查看活动信息，用户必须点击具体优惠券活动，
         *                      也就是查看活动详情，才可以查到当前活动是否还有参与次数。
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

        // TODO receivedNumber（当前已领取数量） 取真实领取数量 优化改为查询Redis
        List<CouponActivityResponse> couponActivityResponses = couponActivities.stream()
                .map(couponActivity -> buildCouponActivityResponse(couponActivity, 0L))
                .collect(Collectors.toList());

        return couponActivityResponses;
    }

    /**
     * 查询领券活动详情
     *
     * @param request 前端会携带用户ID、手机号、优惠券活动ID查询优惠券活动详情
     * @return 返回封装的响应实体对象。
     */
    @Override
    public CouponActivityResponse getCouponActivityDetail(CouponActivityRequest request) {
        // 根据优惠券活动ID查询优惠券活动详情缓存
        CouponActivity couponActivityCache = couponActivityCacheService.getCouponActivityCache(request.getCouponActivityId());

        // 查询当前用户已领取数量，查询优惠券活动参与记录表，
        // 用户每参与一次，就会将用户的信息、优惠券的信息记录到优惠券活动参与记录表中，
        // 统计参与该优惠券活动的次数即可获取用户已领取数量。
        // TODO 优化改为查询Redis
        QueryWrapper<CouponActivityLog> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("coupon_activity_id", request.getCouponActivityId());
        Integer receivedNumber = couponActivityLogMapper.selectCount(queryWrapper);

        CouponActivityResponse couponActivityResponse = buildCouponActivityResponse(couponActivityCache, Long.valueOf(receivedNumber));
        return couponActivityResponse;
    }

    /**
     * 领取优惠券
     * 1、请求量大、并发写，多用户抢券
     * 2、优惠券要控制不能超发、每人不能超领，【防刷】
     * 手动领券场景一般来说用户只会领取一张，几乎不存在一次领多张优惠券的场景，如果是一次领取多张券的场景，
     * 一定调用的是发送优惠券的接口 ---> 详见自动发放优惠券接口。
     *
     * @param request 前端会携带用户ID、手机号、优惠券活动ID、优惠券数量领取优惠券
     * @return 返回封装的响应实体对象。
     */
    @Override
    public CouponResponse receive(ReceiveCouponRequest request) throws BusinessException {
        CouponResponse couponResponse = null;

        // 从缓存中查询抢券活动的活动信息
        CouponActivity couponActivityCache = couponActivityCacheService.getCouponActivityCache(request.getCouponActivityID());

        /**
         *  检查是否可参与领券活动，因为活动有很多种场景
         *  可能活动未开始，活动已开始，可能活动进行中，或者活动已失效，活动是否还有库存？用户是否参与过？
         *  判断是否可以参加活动：
         *            a.优惠券状态是否可用？
         *            b.优惠券活动是否在有效期范围内？
         *            c.该用户是否能参与该活动，还有没有参与次数？
         */
        boolean canJoin = checkIsCanJoinActivity(couponActivityCache);
        if (!canJoin) {
            throw new BusinessException(NOT_JOIN_RECEIVE_COUPON_ERROR.getCode(), NOT_JOIN_RECEIVE_COUPON_ERROR.getMsg());
        }

        /**
         *  分布式锁使用细节：
         *      小技巧一：加锁的时候使用的是：用户的ID + 券活动ID 两个key来组装的。
         *              这就代表同一个用户参加不同活动，我们是不给它加锁的。不同的用户参与同一个活动，我们也不加锁。
         *              此处是以 用户 + 活动 的维度来上的分布式锁，这样锁的粒度更细，性能更高。
         *              只需要保证当前用户参与该活动不出错即可，用户可以同时参与多个活动且不会加锁。
         *
         *      小技巧二：如果尝试获取锁成功，则一定要使用 try-catch 将业务逻辑代码包起来，在 finally 中去释放锁。
         *              因为已经获取到了锁，万一代码在执行过程中，出现异常，直接被捕获抛出去了， 这把锁就变成了死锁
         *             没有被释放掉，没有释放掉之后，下次用户在点击获取锁的时候，这把锁一直没有释放。就会有问题了。
         *             虽然说锁它有默认加锁时间，也可以指定加锁时间，他也会出问题，因为没有释放锁。
         *
         *      小技巧三：在 finally 中释放锁需注意释放锁的前提。
         *              需要判断一下，当前锁是否被锁住了，因为有很多场景下这把锁已经被释放过了，或者说没有加锁成功
         *              的时候，又或者说程序执行业务逻辑代码的时候死机了，服务重启了，这把锁在想去释放的时候，一个
         *              是没有释放掉，或者是锁自动过期了，此时直接去调用释放锁方法会报异常。
         *              总结：释放锁前要判断当前是否被锁住了lock.isLocked()，否则之间调用unlock会报异常。
         *              很多中大厂高并发的系统下，一定要注意的点
         *
         *   都是原子性的操作领券为啥要加分布式锁？
         *              原子性只有在操作redis时才能保证，后面还要有操作数据库的逻辑，如果不加锁用户重复点击的话，
         *              会导致超额领取的情况。
         */
        // 代码执行到这，说明用户可以参与活动，加锁防重复提交
        // 创建锁对象
        RLock lock = redissonClient.getLock(RedisKeyConstant.getReceiveCouponKey(request.getUserId(), request.getCouponActivityID()));
        /**
         * 尝试获取锁：
         *      1.如果成功：
         *          1.1.执行下面的业务逻辑代码。
         *      2.尝试获取锁失败：
         *          2.1.执行 else 中的代码重试获取锁。需进行幂等、防重校验、告警、日志记录、友好的提示等等。
         *          2.2.可以当然也可以不进行重试机制，直接返回结果即可。因为只要没有获取到锁，可能就是用户重复点击了。
         *          2.3.如果真实业务场景下遇到这种问题，就可以使用2.1的方法，都是一些可以在业务上进行优化的点。
         */
        if (lock.tryLock()) {
            try {
                /**
                 * 领取优惠券，received 用户是否已领取成功
                 * 初始值需要设置为true，因为优惠券如果不存在库存限制，需要继续执行业务逻辑
                 */
                boolean received = true;
                /**
                 * couponActivityCache.existLimit()：判断该优惠券是否存在数量限制，
                 * 如果是 -99999999就不会扣库存了
                 *      true：说明券有总数量限制 ---> 需要扣减库存
                 *      false：说明券总数量不限制 ---> 不需要扣减库存
                 */
                if (couponActivityCache.existLimit()) {
                    // 使用 Redis+Lua 脚本的方式保障库存超卖/超抢的问题，原子性问题等。
                    // TODO 更新完库存需要MQ异步更新数据库库存
                    received = couponRedisLuaUtil.receive(request);
                }
                // 判断领券中心中的优惠券扣减库存是否成功，如果成功了，需要将活动中心扣减的优惠券信息添加给用户 ---> 用户领取到了优惠券
                // 添加用户优惠券信息， TODO 优化：原子性
                if (received) {
                    // 从缓存中获取优惠券券模板信息，知道了券模板信息，才知道该给用户添加那张优惠券
                    CouponTemplate couponTemplateCache = couponTemplateCacheService.getCouponTemplateCache(couponActivityCache.getCouponTemplateCode());
                    // 组装券信息
                    Coupon coupon = buildCoupon(request, couponTemplateCache);

                    /**
                     * 以下两步操作执行完毕之后，Redis缓存中优惠券结构为：
                     *      用户信息（List结构）：
                     *              Key：手机号     Value：券code集合
                     *      券信息（String结构）：
                     *              Key：券code     Value：券的具体信息
                     */
                    // 保存优惠券信息，保存到Redis中，String结构
                    couponCacheService.setCouponCache(coupon);
                    // 用户拥有的券列表，Redis的List数据结构
                    couponCacheService.addUserCouponCode(request.getMobile(), coupon.getCode());

                    /**
                     *  如果说已经使用了缓存，但是性能还是上不去，原因在于代码最终还要去操作Mysql
                     *  操作Mysql的话，性能就又慢了，此时可以通过MQ消息队列来提高性能
                     *  更新完缓存之后，通过MQ去更新数据库，这样的好处在于解耦合，也提高了接口的性能
                     *  因为接口之前的性能瓶颈是Mysql，现在把直接操作Mysql给优化了，异步更新数据库。
                     *  但是此时也会带来新问题：
                     *      MQ如何保障消息不丢失，如何保障 Redis 和 Mysql 之间的数据双写一致性?
                     *      如果MQ挂了怎么办？
                     *          MQ挂了可以做最终一致性，保证消息不丢失，后面在补。
                     */
                    // TODO 优化：改为MQ异步更新Mysql数据库，提高性能
                    // TODO 遇到了新问题，MQ如何保障消息不丢失，如何保障 Redis和 Mysql之间的数据一致性
                    // 保存用户优惠券到用户优惠券表中
                    couponMapper.insert(coupon);
                    // 保存领券活动参与记录 TODO 改为MQ异步更新Mysql数据库，提高性能
                    CouponActivityLog couponActivityLog = getCouponActivityLog(request, coupon);
                    couponActivityLogMapper.insert(couponActivityLog);
                    // TODO 发送优惠券过期延时队列，更新优惠券过期状态
                    // 组装返回领取的优惠券信息
                    couponResponse = buildCouponResponse(coupon);
                } else {
                    throw new BusinessException(RECEIVE_COUPON_ERROR.getCode(), RECEIVE_COUPON_ERROR.getMsg());
                }
            } finally {
                if (lock.isLocked()) {
                    // TODO 重点注意：释放锁前要判断当前是否被锁住了lock.isLocked()，否则之间调用unlock会报异常
                    // TODO 严谨一点，防止当前线程释放掉其他线程的锁
                    if (lock.isHeldByCurrentThread()) {
                        // 释放锁资源
                        lock.unlock();
                    }
                }
            }
        } else {
            // 重试获取锁，幂等、防重校验、告警、日志记录、友好的提示等等
        }
        return couponResponse;
    }

    /**
     * 组装领取活动记录信息 ---> 优惠券活动参与记录表
     *
     * @param request
     * @param coupon
     * @return
     */
    private static CouponActivityLog getCouponActivityLog(ReceiveCouponRequest request, Coupon coupon) {
        CouponActivityLog couponActivityLog = new CouponActivityLog();
        couponActivityLog.setCouponActivityId(request.getCouponActivityID());
        couponActivityLog.setCode(coupon.getCode());
        couponActivityLog.setUserId(request.getUserId());
        couponActivityLog.setMobile(request.getMobile());
        couponActivityLog.setCreateTime(new Date());
        couponActivityLog.setUpdateTime(new Date());
        return couponActivityLog;
    }


    /**
     * 判断是否可以参加活动，也就是判断用户是否参与过，如果参与过，判断还有没有参与的次数
     *
     * @param couponActivity 优惠券活动信息
     * @return
     */
    private boolean checkIsCanJoinActivity(CouponActivity couponActivity) {
        // 1、判断当前活动是否生效，也就是判断活动是否在有效时间范围，优惠券是否可用
        boolean available = checkActivityIsAvailable(couponActivity);
        if (available) {
            // 说明当前活动已生效
            // 查询当前用户已领取数量 TODO 优化改为查询Redis
            QueryWrapper<CouponActivityLog> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("coupon_activity_id", couponActivity.getId());
            Integer receivedNumber = couponActivityLogMapper.selectCount(queryWrapper);
            // 判断每人可领取的数量是否大于当前用户已领取的数量
            if (couponActivity.getLimitNumber() > receivedNumber) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断当前活动是否生效，也就是判断活动是否在有效时间范围
     *
     * @param couponActivity 优惠券活动信息
     * @return
     */
    private boolean checkActivityIsAvailable(CouponActivity couponActivity) {
        /**
         * 判断：
         *      a.优惠券状态是否可用
         *      b.当前时间是否大于活动开始时间
         *      c.当前时间是否小于活动结束时间
         */
        if (couponActivity.getStatus().equals(StatusEnum.AVAILABLE.getCode())
                && couponActivity.getBeginTime().before(new Date())
                && couponActivity.getEndTime().after(new Date())) {
            return true;
        }
        return false;
    }
}
