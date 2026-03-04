package com.loopers.domain.coupon;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;

    public Coupon issue(Long userId, CouponTemplate template) {
        Coupon coupon = Coupon.issue(userId, template);
        return couponRepository.save(coupon);
    }

    public List<Coupon> findByUserId(Long userId) {
        return couponRepository.findByUserId(userId);
    }
}
