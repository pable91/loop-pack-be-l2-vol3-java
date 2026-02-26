package com.loopers.domain.order;

import java.time.ZonedDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    public Order createOrder(Long refUserId, Integer totalPrice) {
        Order order = Order.create(
            null,
            refUserId,
            OrderStatus.ORDERED,
            totalPrice,
            ZonedDateTime.now()
        );
        return orderRepository.save(order);
    }
}
