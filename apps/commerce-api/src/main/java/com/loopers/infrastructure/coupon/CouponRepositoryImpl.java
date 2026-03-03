package com.loopers.infrastructure.coupon;



import static com.loopers.infrastructure.coupon.QCouponEntity.couponEntity;

import com.loopers.domain.coupon.Coupon;
import com.loopers.domain.coupon.CouponRepository;
import com.loopers.domain.coupon.CouponSearchCondition;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorMessage;
import com.loopers.support.error.ErrorType;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
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
    public Coupon saveWithUser(Long userId, Coupon coupon) {
        CouponEntity entity = CouponEntity.create(userId, coupon);
        CouponEntity savedEntity = couponJpaRepository.save(entity);
        return savedEntity.toDomain();
    }

    @Override
    public Optional<Coupon> findById(Long id) {
        return couponJpaRepository.findById(id)
            .map(CouponEntity::toDomain);
    }

    @Override
    public List<Coupon> findAll(CouponSearchCondition condition) {
        return queryFactory
            .selectFrom(couponEntity)
            .where(couponEntity.refUserId.isNull())
            .orderBy(couponEntity.createdAt.desc())
            .offset((long) condition.page() * condition.size())
            .limit(condition.size())
            .fetch()
            .stream()
            .map(CouponEntity::toDomain)
            .toList();
    }

    @Override
    public List<Coupon> findByUserId(Long userId) {
        return queryFactory
            .selectFrom(couponEntity)
            .where(couponEntity.refUserId.eq(userId))
            .orderBy(couponEntity.createdAt.desc())
            .fetch()
            .stream()
            .map(CouponEntity::toDomain)
            .toList();
    }

    @Override
    public Coupon update(Coupon coupon) {
        CouponEntity entity = couponJpaRepository.findById(coupon.getId())
            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, ErrorMessage.Coupon.COUPON_NOT_FOUND));
        entity.updateFrom(coupon);
        return couponJpaRepository.save(entity).toDomain();
    }

    @Override
    public void delete(Long id) {
        if (!couponJpaRepository.existsById(id)) {
            throw new CoreException(ErrorType.NOT_FOUND, ErrorMessage.Coupon.COUPON_NOT_FOUND);
        }
        couponJpaRepository.deleteById(id);
    }
}
