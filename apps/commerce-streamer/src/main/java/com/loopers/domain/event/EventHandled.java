package com.loopers.domain.event;

public class EventHandled {

    private final Long id;
    private final String eventId;
    private final String eventType;

    private EventHandled(Long id, String eventId, String eventType) {
        this.id = id;
        this.eventId = eventId;
        this.eventType = eventType;
    }

    public static EventHandled create(String eventId, String eventType) {
        return new EventHandled(null, eventId, eventType);
    }

    public static EventHandled restore(Long id, String eventId, String eventType) {
        return new EventHandled(id, eventId, eventType);
    }

    public Long getId() {
        return id;
    }

    public String getEventId() {
        return eventId;
    }

    public String getEventType() {
        return eventType;
    }
}
