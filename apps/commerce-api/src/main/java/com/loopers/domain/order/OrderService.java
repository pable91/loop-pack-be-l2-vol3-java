package com.loopers.domain.order;

import java.time.ZonedDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;

    public Order createOrder(Long refUserId, Integer totalPrice) {
        Order order = Order.create(null, refUserId, OrderStatus.ORDERED, totalPrice, ZonedDateTime.now());
        Order savedOrder = orderRepository.save(order);

        recordHistory(savedOrder.getId(), OrderStatus.ORDERED);

        return savedOrder;
    }

    public List<OrderItem> createOrderItems(Long orderId, List<OrderItem> orderItems) {
        List<OrderItem> items = orderItems.stream()
            .map(item -> OrderItem.create(null, orderId, item.refProductId(), item.quantity(), item.price()))
            .toList();
        return orderItemRepository.saveAll(items);
    }

    private void recordHistory(Long orderId, OrderStatus status) {
        OrderStatusHistory history = OrderStatusHistory.create(null, orderId, status, ZonedDateTime.now());
        orderStatusHistoryRepository.save(history);
    }
}
