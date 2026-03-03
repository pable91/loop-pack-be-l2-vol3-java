package com.loopers.application.coupon;

import com.loopers.domain.coupon.Coupon;
import com.loopers.domain.coupon.CouponUsageType;
import com.loopers.domain.coupon.DiscountType;
import java.time.ZonedDateTime;

public record CouponInfo(
    Long id,
    String name,
    DiscountType discountType,
    Integer minOrderAmount,
    ZonedDateTime expiredAt,
    CouponUsageType usageType
) {
    public static CouponInfo from(Coupon coupon) {
        return new CouponInfo(
            coupon.getId(),
            coupon.getName().value(),
            coupon.getType(),
            coupon.getMinOrderAmount().value(),
            coupon.getExpiredAt(),
            coupon.getUsageType()
        );
    }
}
