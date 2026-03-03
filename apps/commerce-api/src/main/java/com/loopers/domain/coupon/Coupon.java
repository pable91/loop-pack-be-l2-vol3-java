package com.loopers.domain.coupon;

import java.time.ZonedDateTime;

public class Coupon {

    private Long id;
    private DiscountType type;
    private Integer minOrderAmount;
    private ZonedDateTime expiredAt;
    private CouponUsageType usageType;

    private Coupon(Long id, DiscountType type, Integer minOrderAmount, ZonedDateTime expiredAt, CouponUsageType usageType) {
        this.id = id;
        this.type = type;
        this.minOrderAmount = minOrderAmount;
        this.expiredAt = expiredAt;
        this.usageType = usageType;
    }

    public static Coupon restore(Long id, DiscountType type, Integer minOrderAmount, ZonedDateTime expiredAt, CouponUsageType usageType) {
        return new Coupon(id, type, minOrderAmount, expiredAt, usageType);
    }

    public Long getId() {
        return id;
    }

    public DiscountType getType() {
        return type;
    }

    public Integer getMinOrderAmount() {
        return minOrderAmount;
    }

    public ZonedDateTime getExpiredAt() {
        return expiredAt;
    }

    public CouponUsageType getUsageType() {
        return usageType;
    }
}
