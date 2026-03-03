package com.loopers.infrastructure.coupon;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.coupon.Coupon;
import com.loopers.domain.coupon.CouponUsageType;
import com.loopers.domain.coupon.DiscountType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.ZonedDateTime;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

/**
 * 쿠폰 DB 엔티티
 */
@Entity
@Table(name = "coupons")
@NoArgsConstructor
public class CouponEntity extends BaseEntity {

    @Comment("유저 id (ref)")
    @Column(name = "ref_user_id", nullable = true)
    private Long refUserId;

    @Comment("쿠폰 이름")
    @Column(name = "name", nullable = false)
    private String name;

    @Comment("할인 타입")
    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false)
    private DiscountType type;

    @Comment("사용 상태")
    @Enumerated(EnumType.STRING)
    @Column(name = "usage_type", nullable = false)
    private CouponUsageType usageType;

    @Comment("최소 주문 금액")
    @Column(name = "min_order_amount", nullable = false)
    private Integer minOrderAmount;

    @Comment("만료 날짜")
    @Column(name = "expired_at", nullable = false)
    private ZonedDateTime expiredAt;

    private CouponEntity(Long refUserId, String name, DiscountType type, CouponUsageType usageType, Integer minOrderAmount, ZonedDateTime expiredAt) {
        this.refUserId = refUserId;
        this.name = name;
        this.type = type;
        this.usageType = usageType;
        this.minOrderAmount = minOrderAmount;
        this.expiredAt = expiredAt;
    }

    public static CouponEntity create(Long refUserId, Coupon coupon) {
        return new CouponEntity(
            refUserId,
            coupon.getName().value(),
            coupon.getType(),
            coupon.getUsageType(),
            coupon.getMinOrderAmount().value(),
            coupon.getExpiredAt()
        );
    }

    public Coupon toDomain() {
        return Coupon.restore(
            this.getId(),
            this.name,
            this.type,
            this.minOrderAmount,
            this.expiredAt,
            this.usageType
        );
    }

    public void updateFrom(Coupon coupon) {
        this.name = coupon.getName().value();
        this.type = coupon.getType();
        this.minOrderAmount = coupon.getMinOrderAmount().value();
        this.expiredAt = coupon.getExpiredAt();
        // usageType, refUserId 는 템플릿 수정에서는 변경하지 않음
    }
}
