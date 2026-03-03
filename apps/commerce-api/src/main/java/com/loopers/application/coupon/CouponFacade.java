package com.loopers.application.coupon;

import com.loopers.domain.coupon.Coupon;
import com.loopers.domain.coupon.CouponService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CouponFacade {

    private final CouponService couponService;

    @Transactional
    public CouponInfo createCouponTemplate(CreateCouponTemplateCommand command) {
        Coupon coupon = couponService.createCouponTemplate(
            command.name(),
            command.discountType(),
            command.minOrderAmount(),
            command.expiredAt()
        );
        return CouponInfo.from(coupon);
    }

    @Transactional
    public CouponInfo updateCouponTemplate(UpdateCouponTemplateCommand command) {
        Coupon coupon = couponService.updateCouponTemplate(
            command.couponId(),
            command.name(),
            command.discountType(),
            command.minOrderAmount(),
            command.expiredAt()
        );
        return CouponInfo.from(coupon);
    }

    @Transactional
    public void deleteCouponTemplate(Long couponId) {
        couponService.deleteCouponTemplate(couponId);
    }

    @Transactional(readOnly = true)
    public CouponInfo getCouponTemplate(Long couponId) {
        Coupon coupon = couponService.getById(couponId);
        return CouponInfo.from(coupon);
    }

    @Transactional(readOnly = true)
    public List<CouponInfo> getCouponTemplates(int page, int size) {
        List<Coupon> coupons = couponService.findCouponTemplates(page, size);
        return coupons.stream()
            .map(CouponInfo::from)
            .toList();
    }

    @Transactional
    public CouponInfo issueCoupon(Long userId, Long couponId) {
        Coupon coupon = couponService.issueCoupon(userId, couponId);
        return CouponInfo.from(coupon);
    }

    @Transactional(readOnly = true)
    public List<CouponInfo> getMyCoupons(Long userId) {
        List<Coupon> coupons = couponService.findByUserId(userId);
        return coupons.stream()
            .map(CouponInfo::from)
            .toList();
    }
}
