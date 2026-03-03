package com.loopers.domain.coupon;

import java.util.List;
import java.util.Optional;

public interface CouponRepository {

    Coupon save(Coupon coupon);

    Coupon saveWithUser(Long userId, Coupon coupon);

    Optional<Coupon> findById(Long id);

    List<Coupon> findAll(CouponSearchCondition condition);

    List<Coupon> findByUserId(Long userId);

    Coupon update(Coupon coupon);

    void delete(Long id);
}
