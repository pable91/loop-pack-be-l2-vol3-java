package com.loopers.infrastructure.event;

import com.loopers.domain.event.EventHandled;
import com.loopers.domain.event.EventHandledRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class EventHandledRepositoryImpl implements EventHandledRepository {

    private final EventHandledJpaRepository jpaRepository;

    @Override
    public boolean existsByEventIdAndEventType(String eventId, String eventType) {
        return jpaRepository.existsByEventIdAndEventType(eventId, eventType);
    }

    @Override
    public EventHandled save(EventHandled eventHandled) {
        return EventHandledEntity.toDomain(jpaRepository.save(new EventHandledEntity(eventHandled)));
    }
}
