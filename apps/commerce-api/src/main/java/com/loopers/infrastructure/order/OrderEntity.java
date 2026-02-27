package com.loopers.infrastructure.order;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.ZonedDateTime;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

/**
 * Order DB 엔티티
 */
@Entity
@Table(name = "orders")
@NoArgsConstructor
public class OrderEntity extends BaseEntity {

    @Comment("유저 id (ref)")
    @Column(name = "ref_user_id", nullable = false, updatable = false)
    private Long refUserId;

    @Comment("주문 상태")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status;

    @Comment("총 주문 금액")
    @Column(name = "total_price", nullable = false)
    private Integer totalPrice;

    @Comment("주문 일시")
    @Column(name = "order_dt", nullable = false, updatable = false)
    private ZonedDateTime orderDt;

    private OrderEntity(Long refUserId, OrderStatus status, Integer totalPrice, ZonedDateTime orderDt) {
        this.refUserId = refUserId;
        this.status = status;
        this.totalPrice = totalPrice;
        this.orderDt = orderDt;
    }

    public static OrderEntity create(Order order) {
        return new OrderEntity(
            order.getRefUserId(),
            order.getStatus(),
            order.getTotalPrice().value(),
            order.getOrderDt()
        );
    }

    public static Order toDomain(OrderEntity entity) {
        return Order.create(
            entity.getId(),
            entity.refUserId,
            entity.status,
            entity.totalPrice,
            entity.orderDt
        );
    }

    public void updateStatus(OrderStatus status) {
        this.status = status;
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
