package com.loopers.infrastructure.event;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EventHandledJpaRepository extends JpaRepository<EventHandledEntity, Long> {

    boolean existsByEventIdAndEventType(String eventId, String eventType);
}
