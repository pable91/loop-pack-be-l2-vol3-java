package com.loopers.domain.coupon;

import com.loopers.domain.common.Money;
import com.loopers.domain.common.Name;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorMessage;
import com.loopers.support.error.ErrorType;
import java.time.ZonedDateTime;

/**
 * 발급된 쿠폰 도메인 객체 (템플릿 스냅샷)
 */
public class Coupon {

    private final Long id;
    private final Long refUserId;
    private final Long refTemplateId;
    private final Name name;
    private final DiscountType type;
    private final Money minOrderAmount;
    private final ZonedDateTime expiredAt;
    private final CouponUsageType usageType;

    private Coupon(Long id, Long refUserId, Long refTemplateId, Name name, DiscountType type,
        Money minOrderAmount, ZonedDateTime expiredAt, CouponUsageType usageType) {
        this.id = id;
        this.refUserId = refUserId;
        this.refTemplateId = refTemplateId;
        this.name = name;
        this.type = type;
        this.minOrderAmount = minOrderAmount;
        this.expiredAt = expiredAt;
        this.usageType = usageType;
    }

    public static Coupon issue(Long refUserId, CouponTemplate template) {
        if (refUserId == null || refUserId <= 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, ErrorMessage.Coupon.USER_ID_INVALID);
        }
        return new Coupon(
            null, refUserId, template.getId(),
            template.getName(), template.getType(), template.getMinOrderAmount(), template.getExpiredAt(),
            CouponUsageType.AVAILABLE
        );
    }

    public static Coupon restore(Long id, Long refUserId, Long refTemplateId, String name, DiscountType type,
        Integer minOrderAmount, ZonedDateTime expiredAt, CouponUsageType usageType) {
        return new Coupon(id, refUserId, refTemplateId, new Name(name), type, new Money(minOrderAmount), expiredAt, usageType);
    }

    public Long getId() { return id; }
    public Long getRefUserId() { return refUserId; }
    public Long getRefTemplateId() { return refTemplateId; }
    public Name getName() { return name; }
    public DiscountType getType() { return type; }
    public Money getMinOrderAmount() { return minOrderAmount; }
    public ZonedDateTime getExpiredAt() { return expiredAt; }
    public CouponUsageType getUsageType() { return usageType; }
}
