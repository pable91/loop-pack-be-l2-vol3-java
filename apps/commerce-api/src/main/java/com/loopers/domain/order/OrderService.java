package com.loopers.domain.order;

import com.loopers.domain.product.Product;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;

    private Order createOrder(Long refUserId, Integer totalPrice) {
        Order order = Order.create(null, refUserId, OrderStatus.ORDERED, totalPrice, ZonedDateTime.now());
        Order savedOrder = orderRepository.save(order);

        recordHistory(savedOrder.getId(), OrderStatus.ORDERED);

        return savedOrder;
    }

    private void createOrderItems(Long orderId, List<Product> products, Map<Long, Integer> productQuantities) {
        List<OrderItem> orderItems = products.stream()
            .map(product -> OrderItem.create(
                null,
                orderId,
                product.getId(),
                productQuantities.get(product.getId()),
                product.getPrice()
            ))
            .toList();
        orderItemRepository.saveAll(orderItems);
    }

    private void recordHistory(Long orderId, OrderStatus status) {
        OrderStatusHistory history = OrderStatusHistory.create(null, orderId, status, ZonedDateTime.now());
        orderStatusHistoryRepository.save(history);
    }

    public Order createOrderWithItems(Long userId, List<Product> products, Map<Long, Integer> productQuantities) {
        int totalPrice = calculateTotalPrice(products, productQuantities);
        Order order = createOrder(userId, totalPrice);
        createOrderItems(order.getId(), products, productQuantities);
        return order;
    }

    private int calculateTotalPrice(List<Product> products, Map<Long, Integer> productQuantities) {
        return products.stream()
            .mapToInt(product -> OrderItem.calculateSubtotal(
                product.getPrice(),
                productQuantities.get(product.getId())
            ))
            .sum();
    }
}
