package com.loopers.infrastructure.order;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderStatusHistoryJpaRepository extends JpaRepository<OrderStatusHistoryEntity, Long> {

}
