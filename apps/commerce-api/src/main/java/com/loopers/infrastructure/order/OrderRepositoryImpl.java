package com.loopers.infrastructure.order;

import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderItem;
import com.loopers.domain.order.OrderRepository;
import com.loopers.domain.order.OrderStatusHistory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepository {

    private final OrderJpaRepository orderJpaRepository;
    private final OrderItemJpaRepository orderItemJpaRepository;
    private final OrderStatusHistoryJpaRepository orderStatusHistoryJpaRepository;

    @Override
    public Order save(Order order) {
        // 1. Order 저장
        OrderEntity orderEntity = OrderEntity.create(order);
        OrderEntity savedOrderEntity = orderJpaRepository.save(orderEntity);

        Long orderId = savedOrderEntity.getId();

        // 2. OrderItem 저장 (애그리거트 내부 엔티티)
        List<OrderItem> items = order.getItems();
        if (items != null && !items.isEmpty()) {
            List<OrderItemEntity> itemEntities = items.stream()
                .map(item -> {
                    // refOrderId가 null이면 저장된 orderId 사용
                    Long refOrderId = item.refOrderId() != null ? item.refOrderId() : orderId;
                    return OrderItemEntity.create(
                        new OrderItem(
                            item.id(),
                            refOrderId,
                            item.refProductId(),
                            item.quantity(),
                            item.price()
                        )
                    );
                })
                .toList();
            orderItemJpaRepository.saveAll(itemEntities);
        }

        // 3. OrderStatusHistory 저장 (애그리거트 내부 엔티티)
        List<OrderStatusHistory> histories = order.getHistories();
        if (histories != null && !histories.isEmpty()) {
            List<OrderStatusHistoryEntity> historyEntities = histories.stream()
                .map(history -> {
                    // refOrderId가 null이면 저장된 orderId 사용
                    Long refOrderId = history.refOrderId() != null ? history.refOrderId() : orderId;
                    return OrderStatusHistoryEntity.create(
                        new OrderStatusHistory(
                            history.id(),
                            refOrderId,
                            history.status(),
                            history.changedAt()
                        )
                    );
                })
                .toList();
            orderStatusHistoryJpaRepository.saveAll(historyEntities);
        }

        // 4. 저장된 Order 반환 (items, histories 포함)
        return toDomainWithItemsAndHistories(savedOrderEntity, orderId);
    }

    private Order toDomainWithItemsAndHistories(OrderEntity orderEntity, Long orderId) {
        // OrderItem 조회
        List<OrderItem> items = orderItemJpaRepository.findByRefOrderId(orderId).stream()
            .map(OrderItemEntity::toDomain)
            .toList();

        // OrderStatusHistory 조회
        List<OrderStatusHistory> histories = orderStatusHistoryJpaRepository.findByRefOrderId(orderId).stream()
            .map(OrderStatusHistoryEntity::toDomain)
            .toList();

        // Order 복원 (items, histories 포함)
        return Order.restore(
            orderEntity.getId(),
            orderEntity.getRefUserId(),
            orderEntity.getStatus(),
            orderEntity.getTotalPrice(),
            orderEntity.getOrderDt(),
            items,
            histories
        );
    }
}
