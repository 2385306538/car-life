package com.eugene.service.impl;


import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eugene.common.enums.StatusEnum;
import com.eugene.controller.request.CouponTemplateRemoteRequest;
import com.eugene.controller.request.CouponTemplateRequest;
import com.eugene.mapper.CouponTemplateMapper;
import com.eugene.pojo.CouponTemplate;
import com.eugene.cache.ICouponTemplateCacheService;
import com.eugene.service.ICouponTemplateService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

import static com.eugene.utils.CouponUtil.getCouponTemplateCode;

/**
 * @Description 券模版相关实现类
 * @Author 郭爽
 * @Data 2023/4/4 15:20
 */
@Service("CouponTemplateServiceImpl")
@Primary
//@Deprecated
public class CouponTemplateServiceImpl extends ServiceImpl<CouponTemplateMapper, CouponTemplate> implements ICouponTemplateService {

    @Resource
    private CouponTemplateMapper couponTemplateMapper;

    @Resource
    private ICouponTemplateCacheService couponTemplateCacheService;

    /**
     * 添加优惠券模板信息
     *
     * @param request 优惠券模板参数
     * @return 返回添加是否成功
     */
    @Override
    public boolean addCouponTemplate(CouponTemplateRequest request) {
        CouponTemplate couponTemplate = new CouponTemplate();

        // 规则：CP + 雪花算法ID
        couponTemplate.setCode(getCouponTemplateCode());
        couponTemplate.setName(request.getName());
        couponTemplate.setPrice(request.getPrice());
        couponTemplate.setLimitNumber(request.getLimitNumber());
        couponTemplate.setLimitSku(request.getLimitSku());
        couponTemplate.setLimitSpu(request.getLimitSpu());
        couponTemplate.setValidityType(request.getValidityType());
        couponTemplate.setBeginTime(request.getBeginTime());
        couponTemplate.setEndTime(request.getEndTime());
        couponTemplate.setValidityDay(request.getValidityDay());
        couponTemplate.setStatus(StatusEnum.AVAILABLE.getCode());
        couponTemplate.setCreateTime(new Date());
        couponTemplate.setUpdateTime(new Date());
        int result = couponTemplateMapper.insert(couponTemplate);
        if (result > 0) {
            // 保存到Guava缓存中
            couponTemplateCacheService.setCouponTemplateCache(couponTemplate.getCode(), couponTemplate);
        }
        return result > 0;
    }

    /**
     * 查询优惠券模板信息
     *
     * @param couponTemplateCode 优惠券模板code
     * @return 返回优惠券模板信息
     */
    @Override
    public CouponTemplate getCouponTemplate(String couponTemplateCode) {
        return couponTemplateCacheService.getCouponTemplateCache(couponTemplateCode);
    }

    @Override
    public List<CouponTemplate> list(CouponTemplateRemoteRequest request) {
        QueryWrapper<CouponTemplate> queryWrapper = new QueryWrapper<>();
        if (ObjectUtil.isNotNull(request.getStartId())) {
            queryWrapper.gt("id", request.getStartId());
        }
        if (StringUtils.isNotBlank(request.getLimitTime())) {
            queryWrapper.lt("create_time", request.getLimitTime());
        }
        if (CollectionUtil.isNotEmpty(request.getCodes())) {
            queryWrapper.in("code", request.getCodes());
        }
        if (ObjectUtil.isNotNull(request.getLimitSize())) {
            queryWrapper.last("limit " + request.getLimitSize());
        }
        return couponTemplateMapper.selectList(queryWrapper);
    }
}
