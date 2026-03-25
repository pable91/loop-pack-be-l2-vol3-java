package com.loopers.domain.event;

public interface EventHandledRepository {

    boolean existsByEventIdAndEventType(String eventId, String eventType);

    EventHandled save(EventHandled eventHandled);
}
