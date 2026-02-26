package com.loopers.domain.order;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorMessage;
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
            throw new CoreException(ErrorType.BAD_REQUEST, ErrorMessage.Order.ORDER_ID_INVALID);
        }
    }

    private static void validateRefProductId(Long refProductId) {
        if (refProductId == null || refProductId <= 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, ErrorMessage.Order.PRODUCT_ID_INVALID);
        }
    }

    private static void validateQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, ErrorMessage.Order.QUANTITY_MUST_BE_POSITIVE);
        }
    }

    private static void validatePrice(Integer price) {
        if (price == null || price < 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, ErrorMessage.Order.ORDER_AMOUNT_INVALID);
        }
    }

    public static int calculateSubtotal(int unitPrice, int quantity) {
        return unitPrice * quantity;
    }
}
