package com.loopers.domain.order;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorMessage;
import com.loopers.support.error.ErrorType;
import java.time.ZonedDateTime;

/**
 *  Order 도메인
 */
public class Order {

    private final Long id;
    private final Long refUserId;

    private OrderStatus status;
    private Integer totalPrice;
    private ZonedDateTime orderDt;

    private Order(Long id, Long refUserId, OrderStatus status, Integer totalPrice, ZonedDateTime orderDt) {
        this.id = id;
        this.refUserId = refUserId;
        this.status = status;
        this.totalPrice = totalPrice;
        this.orderDt = orderDt;
    }

    public static Order create(Long id, Long refUserId, OrderStatus status, Integer totalPrice, ZonedDateTime orderDt) {
        validateRefUserId(refUserId);
        validateTotalPrice(totalPrice);
        validateOrderDt(orderDt);

        return new Order(id, refUserId, status, totalPrice, orderDt);
    }

    private static void validateRefUserId(Long refUserId) {
        if (refUserId == null || refUserId <= 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, ErrorMessage.Order.USER_ID_INVALID);
        }
    }

    private static void validateTotalPrice(Integer totalPrice) {
        if (totalPrice == null || totalPrice < 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, ErrorMessage.Order.TOTAL_ORDER_AMOUNT_INVALID);
        }
    }

    private static void validateOrderDt(ZonedDateTime orderDt) {
        if (orderDt == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, ErrorMessage.Order.ORDER_DT_REQUIRED);
        }
    }

    public void cancel() {
        if (this.status != OrderStatus.ORDERED) {
            throw new CoreException(ErrorType.BAD_REQUEST, ErrorMessage.Order.CANCEL_ONLY_WHEN_COMPLETED);
        }
        this.status = OrderStatus.CANCELLED;
    }

    public Long getId() {
        return id;
    }

    public Long getRefUserId() {
        return refUserId;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public Integer getTotalPrice() {
        return totalPrice;
    }

    public ZonedDateTime getOrderDt() {
        return orderDt;
    }
}
