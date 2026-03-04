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
public class CouponTemplateService {

    private final CouponTemplateRepository couponTemplateRepository;

    public CouponTemplate create(String name, DiscountType discountType, Integer discountValue, Integer minOrderAmount, ZonedDateTime expiredAt) {
        CouponTemplate template = CouponTemplate.create(name, discountType, discountValue, minOrderAmount, expiredAt);
        return couponTemplateRepository.save(template);
    }

    public CouponTemplate getById(Long id) {
        return couponTemplateRepository.findById(id)
            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, ErrorMessage.Coupon.COUPON_TEMPLATE_NOT_FOUND));
    }

    public List<CouponTemplate> findAll(int page, int size) {
        return couponTemplateRepository.findAll(CouponSearchCondition.of(page, size));
    }

    public CouponTemplate update(Long id, String name, DiscountType discountType, Integer discountValue, Integer minOrderAmount, ZonedDateTime expiredAt) {
        CouponTemplate template = getById(id);
        CouponTemplate updated = template.updateTemplate(name, discountType, discountValue, minOrderAmount, expiredAt);
        return couponTemplateRepository.update(updated);
    }

    public void delete(Long id) {
        couponTemplateRepository.delete(id);
    }
}
