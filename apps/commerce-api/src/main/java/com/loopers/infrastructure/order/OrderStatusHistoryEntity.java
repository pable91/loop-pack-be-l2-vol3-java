package com.loopers.infrastructure.order;

import com.loopers.domain.order.OrderStatus;
import com.loopers.domain.order.OrderStatusHistory;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.ZonedDateTime;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

/**
 * OrderStatusHistory DB 엔티티
 */
@Entity
@Table(name = "order_status_history")
@NoArgsConstructor
public class OrderStatusHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Comment("주문 id (ref)")
    @Column(name = "ref_order_id", nullable = false, updatable = false)
    private Long refOrderId;

    @Comment("변경 전 상태")
    @Enumerated(EnumType.STRING)
    @Column(name = "from_status", updatable = false)
    private OrderStatus fromStatus;

    @Comment("변경 후 상태")
    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", nullable = false, updatable = false)
    private OrderStatus toStatus;

    @Comment("상태 변경 일시")
    @Column(name = "changed_at", nullable = false, updatable = false)
    private ZonedDateTime changedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    private OrderStatusHistoryEntity(Long refOrderId, OrderStatus fromStatus, OrderStatus toStatus, ZonedDateTime changedAt) {
        this.refOrderId = refOrderId;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.changedAt = changedAt;
    }

    @PrePersist
    private void prePersist() {
        this.createdAt = ZonedDateTime.now();
    }

    public static OrderStatusHistoryEntity create(OrderStatusHistory history) {
        return new OrderStatusHistoryEntity(
            history.refOrderId(),
            history.fromStatus(),
            history.toStatus(),
            history.changedAt()
        );
    }

    public static OrderStatusHistory toDomain(OrderStatusHistoryEntity entity) {
        return OrderStatusHistory.create(
            entity.id,
            entity.refOrderId,
            entity.fromStatus,
            entity.toStatus,
            entity.changedAt
        );
    }

    public Long getId() {
        return id;
    }

    public Long getRefOrderId() {
        return refOrderId;
    }

    public OrderStatus getFromStatus() {
        return fromStatus;
    }

    public OrderStatus getToStatus() {
        return toStatus;
    }

    public ZonedDateTime getChangedAt() {
        return changedAt;
    }
}
