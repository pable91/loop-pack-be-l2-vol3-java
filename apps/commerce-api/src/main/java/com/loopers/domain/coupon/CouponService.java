package com.loopers.domain.coupon;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorMessage;
import com.loopers.support.error.ErrorType;
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
        return couponRepository.findById(couponId)
            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, ErrorMessage.Coupon.COUPON_NOT_FOUND));
    }

    public List<Coupon> findCouponTemplates(int page, int size) {
        CouponSearchCondition condition = CouponSearchCondition.of(page, size);
        return couponRepository.findAll(condition);
    }

    public Coupon updateCouponTemplate(Long couponId, String name, DiscountType discountType, Integer minOrderAmount,
        ZonedDateTime expiredAt) {
        Coupon coupon = getById(couponId);
        Coupon updated = coupon.updateTemplate(name, discountType, minOrderAmount, expiredAt);
        return couponRepository.update(updated);
    }

    public void deleteCouponTemplate(Long couponId) {
        couponRepository.delete(couponId);
    }

    public Coupon issueCoupon(Long userId, Long couponId) {
        Coupon template = getById(couponId);
        return couponRepository.saveWithUser(userId, template);
    }

    public List<Coupon> findByUserId(Long userId) {
        return couponRepository.findByUserId(userId);
    }
}
