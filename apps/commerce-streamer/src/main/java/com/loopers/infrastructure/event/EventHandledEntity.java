package com.loopers.infrastructure.event;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.event.EventHandled;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

@Entity
@Table(
    name = "event_handled",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_event_handled_event_id_type", columnNames = {"event_id", "event_type"})
    },
    indexes = {
        @Index(name = "idx_event_handled_event_id", columnList = "event_id")
    }
)
@NoArgsConstructor
public class EventHandledEntity extends BaseEntity {

    @Comment("이벤트 고유 ID")
    @Column(name = "event_id", nullable = false, updatable = false)
    private String eventId;

    @Comment("이벤트 타입")
    @Column(name = "event_type", nullable = false, updatable = false)
    private String eventType;

    public EventHandledEntity(EventHandled eventHandled) {
        this.eventId = eventHandled.getEventId();
        this.eventType = eventHandled.getEventType();
    }

    public static EventHandled toDomain(EventHandledEntity entity) {
        return EventHandled.restore(
            entity.getId(),
            entity.eventId,
            entity.eventType
        );
    }
}
