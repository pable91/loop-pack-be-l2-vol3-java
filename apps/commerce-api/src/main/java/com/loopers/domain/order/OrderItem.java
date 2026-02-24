package com.loopers.domain.order;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;

/**
 * OrderItem 도메인
 */
public record OrderItem(Long id, Long refOrderId, Long refProductId, Integer quantity, Integer price) {

    public static OrderItem create(Long id, Long refOrderId, Long refProductId, Integer quantity, Integer price) {
        validateRefOrderId(refOrderId);
        validateRefProductId(refProductId);
        validateQuantity(quantity);
        validatePrice(price);

        return new OrderItem(id, refOrderId, refProductId, quantity, price);
    }

    private static void validateRefOrderId(Long refOrderId) {
        if (refOrderId == null || refOrderId <= 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "주문FK는 null이거나 0이하가 될 수 없습니다");
        }
    }

    private static void validateRefProductId(Long refProductId) {
        if (refProductId == null || refProductId <= 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "상품FK는 null이거나 0이하가 될 수 없습니다");
        }
    }

    private static void validateQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "수량은 양수여야 합니다");
        }
    }

    private static void validatePrice(Integer price) {
        if (price == null || price < 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "주문 금액은 null이거나 음수가 될 수 없습니다");
        }
    }
}
