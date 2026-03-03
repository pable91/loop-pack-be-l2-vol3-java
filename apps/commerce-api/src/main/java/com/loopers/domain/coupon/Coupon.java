package com.loopers.domain.coupon;

import com.loopers.domain.common.Money;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorMessage;
import com.loopers.support.error.ErrorType;
import java.time.ZonedDateTime;

/**
 *  Coupon 도메인 객체
 */
public class Coupon {

    private final Long id;
    private final DiscountType type;
    private final Money minOrderAmount;
    private final ZonedDateTime expiredAt;
    private final CouponUsageType usageType;

    private Coupon(Long id, DiscountType type, Money minOrderAmount, ZonedDateTime expiredAt, CouponUsageType usageType) {
        this.id = id;
        this.type = type;
        this.minOrderAmount = minOrderAmount;
        this.expiredAt = expiredAt;
        this.usageType = usageType;
    }

    public static Coupon create(DiscountType discountType, Integer minOrderAmount, ZonedDateTime expiredAt) {
        validateDiscountType(discountType);
        validateExpiredAt(expiredAt);

        return new Coupon(null, discountType, new Money(minOrderAmount), expiredAt, CouponUsageType.AVAILABLE);
    }

    private static void validateDiscountType(DiscountType discountType) {
        if (discountType == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, ErrorMessage.Coupon.DISCOUNT_TYPE_REQUIRED);
        }
    }

    private static void validateExpiredAt(ZonedDateTime expiredAt) {
        if (expiredAt == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, ErrorMessage.Coupon.EXPIRED_AT_REQUIRED);
        }
        if (expiredAt.isBefore(ZonedDateTime.now())) {
            throw new CoreException(ErrorType.BAD_REQUEST, ErrorMessage.Coupon.EXPIRED_AT_MUST_BE_FUTURE);
        }
    }

    public static Coupon restore(Long id, DiscountType type, Integer minOrderAmount, ZonedDateTime expiredAt,
        CouponUsageType usageType) {
        return new Coupon(id, type, new Money(minOrderAmount), expiredAt, usageType);
    }

    public Long getId() {
        return id;
    }

    public DiscountType getType() {
        return type;
    }

    public Money getMinOrderAmount() {
        return minOrderAmount;
    }

    public ZonedDateTime getExpiredAt() {
        return expiredAt;
    }

    public CouponUsageType getUsageType() {
        return usageType;
    }
}
