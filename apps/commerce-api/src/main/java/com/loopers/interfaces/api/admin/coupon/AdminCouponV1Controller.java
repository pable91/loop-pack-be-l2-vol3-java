package com.loopers.interfaces.api.admin.coupon;

import com.loopers.application.admin.AdminPrincipal;
import com.loopers.application.coupon.CouponFacade;
import com.loopers.application.coupon.CouponInfo;
import com.loopers.application.coupon.CreateCouponTemplateCommand;
import com.loopers.application.coupon.UpdateCouponTemplateCommand;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.admin.AdminUser;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api-admin/v1/coupons")
public class AdminCouponV1Controller {

    private final CouponFacade couponFacade;

    @GetMapping
    public ApiResponse<AdminCouponV1Dto.CouponTemplateListResponse> getCouponTemplates(
        @AdminUser AdminPrincipal admin,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        List<CouponInfo> couponInfos = couponFacade.getCouponTemplates(page, size);
        return ApiResponse.success(AdminCouponV1Dto.CouponTemplateListResponse.from(couponInfos));
    }

    @GetMapping("/{couponId}")
    public ApiResponse<AdminCouponV1Dto.CouponTemplateResponse> getCouponTemplate(
        @AdminUser AdminPrincipal admin,
        @PathVariable Long couponId
    ) {
        CouponInfo couponInfo = couponFacade.getCouponTemplate(couponId);
        return ApiResponse.success(AdminCouponV1Dto.CouponTemplateResponse.from(couponInfo));
    }

    @PostMapping
    public ApiResponse<AdminCouponV1Dto.CouponTemplateResponse> createCouponTemplate(
        @AdminUser AdminPrincipal admin,
        @Valid @RequestBody AdminCouponV1Dto.CreateCouponTemplateRequest request
    ) {
        CreateCouponTemplateCommand command = new CreateCouponTemplateCommand(
            request.name(),
            request.discountType(),
            request.minOrderAmount(),
            request.expiredAt()
        );
        CouponInfo couponInfo = couponFacade.createCouponTemplate(command);
        return ApiResponse.success(AdminCouponV1Dto.CouponTemplateResponse.from(couponInfo));
    }

    @PutMapping("/{couponId}")
    public ApiResponse<AdminCouponV1Dto.CouponTemplateResponse> updateCouponTemplate(
        @AdminUser AdminPrincipal admin,
        @PathVariable Long couponId,
        @Valid @RequestBody AdminCouponV1Dto.UpdateCouponTemplateRequest request
    ) {
        UpdateCouponTemplateCommand command = new UpdateCouponTemplateCommand(
            couponId,
            request.name(),
            request.discountType(),
            request.minOrderAmount(),
            request.expiredAt()
        );
        CouponInfo couponInfo = couponFacade.updateCouponTemplate(command);
        return ApiResponse.success(AdminCouponV1Dto.CouponTemplateResponse.from(couponInfo));
    }

    @DeleteMapping("/{couponId}")
    public ApiResponse<Void> deleteCouponTemplate(
        @AdminUser AdminPrincipal admin,
        @PathVariable Long couponId
    ) {
        couponFacade.deleteCouponTemplate(couponId);
        return ApiResponse.success(null);
    }
}
