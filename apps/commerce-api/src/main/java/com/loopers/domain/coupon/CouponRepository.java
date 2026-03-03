package com.loopers.domain.coupon;

import java.util.List;

public interface CouponRepository {

    Coupon save(Coupon coupon);

    Coupon findById(Long id);

    List<Coupon> findAll(CouponSearchCondition condition);
}
