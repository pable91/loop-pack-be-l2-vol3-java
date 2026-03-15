package com.loopers.domain.order;

public enum OrderStatus {

    PENDING("결제 대기"),
    CONFIRMED("결제 완료"),
    CANCELLED("주문 취소");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
