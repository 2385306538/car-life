package com.eugene.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eugene.common.enums.CouponStatusEnum;
import com.eugene.common.enums.StatusEnum;
import com.eugene.common.exception.BusinessException;
import com.eugene.controller.request.SendCouponRequest;
import com.eugene.controller.request.UserCouponRequest;
import com.eugene.controller.request.VerificationCouponRequest;
import com.eugene.controller.response.CouponResponse;
import com.eugene.mapper.CouponMapper;
import com.eugene.pojo.Coupon;
import com.eugene.pojo.CouponTemplate;
import com.eugene.service.ICouponCacheService;
import com.eugene.service.ICouponService;
import com.eugene.cache.ICouponTemplateCacheService;
import lombok.SneakyThrows;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.eugene.common.enums.Errors.NOT_COUPON_INFO_ERROR;
import static com.eugene.controller.response.CouponResponse.buildCouponResponse;

/**
 * @Description 用户优惠券相关实现类
 * @Author eugene
 * @Data 2023/4/5 15:01
 */
@Service
public class CouponServiceImpl extends ServiceImpl<CouponMapper, Coupon> implements ICouponService {

    private Logger log = LoggerFactory.getLogger(CouponServiceImpl.class);
    @Resource
    private ICouponCacheService couponCacheService;

    @Resource
    private ICouponTemplateCacheService couponTemplateCacheService;

    @Resource
    private CouponMapper couponMapper;

    @Resource
    private RedissonClient redissonClient;

    /**
     * 用户领取完成优惠券之后，可通过此接口查看个人所拥有的优惠券列表
     *
     * @param request 用户ID、手机号
     * @return 返回该用户拥有的优惠券集合
     */
    @Override
    public List<CouponResponse> getList(UserCouponRequest request) {
        /**
         * 查询用户拥有的优惠券Code列表
         * 从缓存中查询，Key为手机号，Value为券code集合
         */
        List<String> codes = couponCacheService.getUserCouponCodeList(request.getMobile());

        // 根据优惠券Code去查询对应的优惠券信息
        List<Coupon> coupons = couponCacheService.batchGetCouponCache(codes);

        /**
         * 再给服务端返回数据的时候，不能够把优惠券的实体类（Coupon）里面的字段全返回去，不要偷懒，
         * 因为这样做，维护起来很费劲。
         * 建议自己组装 Response 数据（CouponResponse）并返回，把我们想给前端返回的实体类对象放回去就OK了，
         * 给前端返回对象的时候不建议使用 Map，建议使用实体，实体类的好处就是方便阅读、维护。
         */
        // 组装优惠券结果返回
        List<CouponResponse> couponResponses = coupons.stream()
                .map(CouponResponse::buildCouponResponse)
                .collect(Collectors.toList());
        return couponResponses;
    }

    /**
     * 自动发放优惠券接口   TODO 优化改为多线程自动发券
     * 手动领券场景一般来说用户只会领取一张，几乎不存在一次领多张优惠券的场景，如果是一次领取多张券的场景，
     * 一定调用的是发送优惠券的接口。
     * 在自动发券的接口会涉及到同一张券发多张的场景，需要做一个批量操作。
     *
     * @param request 用户ID、手机号、券模板code、发放的优惠券数量
     * @return 返回是否发放成功
     */
    @Override
    public Boolean send(SendCouponRequest request) {
        /**
         * TODO 优化：券模版信息放到 Guava 缓存中提高性能。
         * 将不怎么会变动的数据缓存到 Guava 中，查询这些数据时从 Guava 缓存中去查询，降低 Redis 的压力，提高性能。
         * 券模板信息不怎么变更，为了进一步提高性能，采用多级缓存，放到 Guava 里面。
         */
        CouponTemplate couponTemplateCache = couponTemplateCacheService.getCouponTemplateCache(request.getCouponTemplateCode());
        // 如果从缓存中查询出来的券模板信息不存在，直接返回false。
        if (Objects.isNull(couponTemplateCache)) {
            log.error("券模版信息不存在");
            return false;
        }

        /**
         * 代码执行到这，说明券模板信息能够成功获取。
         * 自动发放优惠券券功能是程序配置调用，所以不需要校验券数量，直接发送。
         */
        List<Coupon> coupons = new ArrayList<>();
        /**
         * 判断发放的优惠券的张数，如果前端不传值的话，默认是1。
         * 如果传值了，它有几张我们就循环几遍，将优惠券的信息组装进去。
         * 组装券信息的时候，每一次组装都会重新生成一个券code，需要保证券code不重复
         */
        for (Integer i = 0; i < request.getNumber(); i++) {
            /**
             * 组装自动发券的优惠券信息，需要保证用户优惠券code唯一
             *
             * TODO 解决方案：解决code重复问题
             * coupons.add(getCoupon(request, couponTemplateCache, i));
             *
             * 方案是：再后面追加一个发券发放张数（第几张）
             * 这里的第几张如何实现？
             * 在service组装的时候，我们会根据发放的张数来循环相对应的次数，可以将
             * for循环的次数 i 参数传递过来，作为当前发放张数的标识。
             *
             * 如果不这样做的话，前期也能够保证不重复，只不过当并发量很大的时候，ID可能就会重复，
             * 如果出现这种情况，加上一个发放张数，就能完美解决问题。
             */
            Coupon coupon = Coupon.getCoupon(request, couponTemplateCache);
            coupons.add(coupon);
        }
        /**
         * 自动发券功能与手动领券功能对比：
         *      自动发券功能：先保存数据库，后保存Redis
         *
         *      手动领取功能：先保存Redis，异步MQ保存数据库
         *      可能会有问题，我们应该优先保证数据库的准确性，Redis数据丢了，还有数据库兜底。
         */

        try {
            /**
             * 批量保存优惠券，可能会发多张优惠券
             * 注意：
             *      此处不要用 for 循环，直接使用Mybatis的动态sql批量添加（原子性）
             *      使用 for 循环不能保证命令执行的原子性。
             *
             *  for (int i = 0; i < request.getNumber(); i++) {
             *      Coupon coupon = coupons.get(i);
             *      // 保存优惠券信息
             *      couponCacheService.setCouponCache(coupon);
             *      // 保存用户拥有的优惠券列表
             *      couponCacheService.addUserCouponCode(request.getMobile(), coupon.getCode());
             * }
             */
            couponMapper.saveBatch(coupons);
        } catch (Exception e) {
            log.error("保存DB优惠券信息失败", e);
            return false;
        }

        // TODO 优化：优惠券信息保存到 Redis 中,同一批次一起保存，保障数据的准确性
        boolean redisResult = couponCacheService.batchSetCouponCache(coupons);
        if (!redisResult) {
            // 告警通知、邮件、钉钉、等等
            log.warn("优惠券缓存保存失败, coupons:{}", coupons);
        }
        return true;
    }

    /**
     * 当用户查询到了优惠券列表信息之后，点击某张优惠券，可以查看当前选中的优惠券的具体详情信息
     *
     * @param code 用户优惠券code
     * @return 组装返回优惠券信息
     */
    @Override
    public CouponResponse getCoupon(String code) {
        CouponResponse response = null;

        // TODO 优化为从 Guava 缓存中查询优惠券数据
        Coupon couponCache = couponCacheService.getCouponCache(code);

        // TODO 重点关注：缓存穿透问题，直接返回不查询数据库  ---> 未解决，需测试一下
        if (Objects.nonNull(couponCache) && couponCache.getCode().equals("default")) {
            /**
             * 此处缓存穿透的解决方案采用的是缓存默认值default。
             * 当查询数据库没有查询到的话，就会缓存设置默认值default，过期时间1分钟。
             * 后续相同的请求查询缓存，如果查询出来的Key为default的话，说明数据库中没有该数据，就不让它去查询DB，直接返回null。
             * 命中了自定义的缓存Key ---> default直接返回。
             */
            return null;
        }

        /**
         * 如果缓存不存在，就会以当前的 code 来加锁
         * 此处为啥要加分布式锁？
         *      使用分布式锁的目的是为了解决缓存穿透问题。
         *      保证只有一个线程能够执行查询数据库的操作。其他线程在等待锁释放期间，
         *      则不会进行数据库查询操作，而是等待被唤醒或超时返回。
         */
        if (Objects.isNull(couponCache)) {
            // 获取锁对象 TODO 加锁KEY
            RLock lock = redissonClient.getLock(code);
            // 尝试获取锁
            if (lock.tryLock()) {
                try {
                    /**
                     * 查询数据库，如果数据库中没有查询到数据，则设置默认值default，过期时间1分钟
                     */
                    couponCache = getCouponDB(code);
                } finally {
                    if (lock.isLocked()) {
                        // 严谨一点，防止当前线程释放掉其他线程的锁
                        if (lock.isHeldByCurrentThread()) {
                            // 释放锁
                            lock.unlock();
                        }
                    }
                }
            } else {
                // 重试获取锁，幂等、防重校验、告警、日志记录、友好的提示等等
            }
        }
        // 如果缓存数据不为空，组装数据并向前端返回
        if (Objects.nonNull(couponCache)) {
            response = buildCouponResponse(couponCache);
        }
        return response;
    }

    /**
     * 用户使用优惠券
     *
     * @param request 用户ID、手机号、优惠券code
     * @return 返回是否使用优惠券成功
     */
    @Override
    @SneakyThrows
    public Boolean verification(VerificationCouponRequest request) {
        /**
         * 使用优惠券需要添加分布式锁，为啥？
         *      因为如果有用户同时在多笔订单中使用同一张优惠券，不添加分布式锁会出现问题
         *      多个地方都用到了，多个地方也都会使用成功 ---> 出现优惠券超用。
         *
         * 加锁，只要涉及到并发的问题一定要上锁，以优惠券code作为分布式锁的粒度，
         * 保证只能有一个线程使用该优惠券
         */
        // 创建锁对象
        RLock lock = redissonClient.getLock(request.getCouponCode());
        // 尝试获取锁
        if (lock.tryLock()) {
            try {
                // 根据手机号从缓存中拿到用户拥有的优惠券列表
                List<String> userCouponCodeList = couponCacheService.getUserCouponCodeList(request.getMobile());

                /**
                 * 风控校验：判断当前用户和优惠券是否是同一人，优惠券是否可用。
                 * 也就是判断当前优惠券是否是该用户的。
                 * 因为如果有人把数据库攻击了，拿到了所有的优惠券，此时就可以
                 * 注册一个用户，把其他人的优惠券用到自己身上。就会有这个风险，所以此处需要进行判断。
                 */
                if (!userCouponCodeList.contains(request.getCouponCode())) {
                    throw new BusinessException(NOT_COUPON_INFO_ERROR.getCode(), NOT_COUPON_INFO_ERROR.getMsg());
                }

                // 从缓存中获取当前优惠券信息
                Coupon couponCache = couponCacheService.getCouponCache(request.getCouponCode());
                // 检查用户的该优惠券是否可用
                if (checkIsCanVerification(request, couponCache)) {
                    Coupon coupon = new Coupon();
                    coupon.setId(couponCache.getId());
                    coupon.setCode(couponCache.getCode());
                    coupon.setMobile(couponCache.getMobile());
                    //将优惠券状态更新为已使用状态
                    coupon.setStatus(CouponStatusEnum.USED.getCode());
                    coupon.setUseTime(new Date());
                    coupon.setUpdateTime(new Date());
                    // TODO 因为分片键mobile不能更新，mobile暂时设置为null, 后续优化
                    coupon.setMobile(null);
                    // TODO 改为MQ异步更新Mysql，增加消息不丢失机制
                    int result = couponMapper.updateById(coupon);
                    /**
                     * 如果 result > 0 认为核销券成功
                     * 如果 result < 0 数据库没更新，核销券失败，缓存不应该删除
                     */
                    if (result > 0) {
                        // 核销优惠券信息，也就是清除当前已使用优惠券缓存信息
                        couponCacheService.delUserCouponCode(coupon);
                        couponCacheService.delCouponCache(coupon);
                    }
                    return true;

                } else {
                    throw new BusinessException(NOT_COUPON_INFO_ERROR.getCode(), NOT_COUPON_INFO_ERROR.getMsg());
                }
            } finally {
                if (lock.isLocked()) {
                    // 严谨一点，防止当前线程释放掉其他线程的锁
                    if (lock.isHeldByCurrentThread()) {
                        lock.unlock();
                    }
                }
            }
        }
        return false;
    }

    /**
     * 检查优惠券是否可用
     */
    private boolean checkIsCanVerification(VerificationCouponRequest request, Coupon couponCache) throws BusinessException {
        // 判断优惠券信息是否存在或已使用
        if (Objects.isNull(couponCache)) {
            throw new BusinessException(NOT_COUPON_INFO_ERROR.getCode(), NOT_COUPON_INFO_ERROR.getMsg());
        }

        // 检查是不是本人核销优惠券、是否在可用时间范围内
        if (couponCache.getMobile().equals(request.getMobile())
                && couponCache.getBeginTime().before(new Date())
                && couponCache.getEndTime().after(new Date())) {
            return true;
        }
        return false;
    }

    /**
     * 根据用户优惠券code从数据库中查询优惠券信息
     */
    private Coupon getCouponDB(String code) {
        // 查询数据库
        QueryWrapper<Coupon> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("code", code);
        queryWrapper.eq("status", StatusEnum.AVAILABLE.getCode());
        Coupon coupon = couponMapper.selectOne(queryWrapper);
        // 如果数据库中的数据为空，则发生缓存穿透问题 ---> 可能是恶意攻击，访问缓存与数据库中不存在的数据
        if (Objects.isNull(coupon)) {
            // 缓存穿透解决方案 ---> 设置默认值default，key过期时间1分钟
            // 如果此时发生缓存雪崩怎么解决啊？过期时间改为随机数就好了
            couponCacheService.setCouponCache(new Coupon("default"), 1L, TimeUnit.MINUTES);
            return null;
        } else {
            // 说明缓存丢失了，在将数据库中查询出来的结果添加到缓存中
            couponCacheService.setCouponCache(coupon);
        }
        return coupon;
    }
}
