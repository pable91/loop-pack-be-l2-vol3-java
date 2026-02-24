package com.loopers.domain.order;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import java.time.ZonedDateTime;

/**
 * OrderStatusHistory 도메인
 */
public record OrderStatusHistory(Long id, Long refOrderId, OrderStatus status, ZonedDateTime changedAt) {

    public static OrderStatusHistory create(Long id, Long refOrderId, OrderStatus status, ZonedDateTime changedAt) {
        validateRefOrderId(refOrderId);
        validateStatus(status);
        validateChangedAt(changedAt);

        return new OrderStatusHistory(id, refOrderId, status, changedAt);
    }

    private static void validateRefOrderId(Long refOrderId) {
        if (refOrderId == null || refOrderId <= 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "주문FK는 null이거나 0이하가 될 수 없습니다");
        }
    }

    private static void validateStatus(OrderStatus status) {
        if (status == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "주문 상태는 필수입니다");
        }
    }

    private static void validateChangedAt(ZonedDateTime changedAt) {
        if (changedAt == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "주문 상태 변경 일시는 필수입니다");
        }
    }
}
