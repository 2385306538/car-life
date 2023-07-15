package com.eugene.service.impl;

import com.eugene.pojo.CouponTemplate;
import com.eugene.service.ICouponTemplateCacheService;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CouponTemplateCacheServiceImpl implements ICouponTemplateCacheService {

    private static final Logger log = LoggerFactory.getLogger(CouponTemplateCacheServiceImpl.class);

    private LoadingCache<String, CouponTemplate> couponTemplateCache;

    /**
     * 从缓存中获取优惠券模板信息
     * @param couponTemplateCode 优惠券模板Code
     * @return
     */
    @Override
    public CouponTemplate getCouponTemplateCache(String couponTemplateCode) {
        return couponTemplateCache.getIfPresent(couponTemplateCode);
    }
}
