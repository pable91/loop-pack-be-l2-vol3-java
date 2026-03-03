package com.loopers.domain.coupon;

import java.time.ZonedDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;

    public Coupon createCouponTemplate(String name, DiscountType discountType, Integer minOrderAmount, ZonedDateTime expiredAt) {
        Coupon coupon = Coupon.create(name, discountType, minOrderAmount, expiredAt);
        return couponRepository.save(coupon);
    }

    public Coupon getById(Long couponId) {
        return couponRepository.findById(couponId);
    }

    public List<Coupon> findCouponTemplates(int page, int size) {
        CouponSearchCondition condition = CouponSearchCondition.of(page, size);
        return couponRepository.findAll(condition);
    }

    public Coupon updateCouponTemplate(Long couponId, String name, DiscountType discountType, Integer minOrderAmount,
        ZonedDateTime expiredAt) {
        Coupon coupon = couponRepository.findById(couponId);
        Coupon updated = coupon.updateTemplate(name, discountType, minOrderAmount, expiredAt);
        return couponRepository.update(updated);
    }

    public void deleteCouponTemplate(Long couponId) {
        couponRepository.delete(couponId);
    }
}
