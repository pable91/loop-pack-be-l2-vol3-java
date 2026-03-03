package com.loopers.infrastructure.coupon;



import static com.loopers.infrastructure.coupon.QCouponEntity.couponEntity;

import com.loopers.domain.coupon.Coupon;
import com.loopers.domain.coupon.CouponRepository;
import com.loopers.domain.coupon.CouponSearchCondition;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CouponRepositoryImpl implements CouponRepository {

    private final CouponJpaRepository couponJpaRepository;
    private final JPAQueryFactory queryFactory;

    @Override
    public Coupon save(Coupon coupon) {
        CouponEntity entity = CouponEntity.create(null, coupon);
        CouponEntity savedEntity = couponJpaRepository.save(entity);
        return savedEntity.toDomain();
    }

    @Override
    public Coupon findById(Long id) {
        return couponJpaRepository.findById(id)
            .map(CouponEntity::toDomain)
            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "쿠폰을 찾을 수 없습니다."));
    }

    @Override
    public List<Coupon> findAll(CouponSearchCondition condition) {
        return queryFactory
            .selectFrom(couponEntity)
            .orderBy(couponEntity.createdAt.desc())
            .offset((long) condition.page() * condition.size())
            .limit(condition.size())
            .fetch()
            .stream()
            .map(CouponEntity::toDomain)
            .toList();
    }

    @Override
    public Coupon update(Coupon coupon) {
        CouponEntity entity = couponJpaRepository.findById(coupon.getId())
            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "쿠폰을 찾을 수 없습니다."));
        entity.updateFrom(coupon);
        return couponJpaRepository.save(entity).toDomain();
    }

    @Override
    public void delete(Long id) {
        if (!couponJpaRepository.existsById(id)) {
            throw new CoreException(ErrorType.NOT_FOUND, "쿠폰을 찾을 수 없습니다.");
        }
        couponJpaRepository.deleteById(id);
    }
}
