package me.bombom.api.v1.event;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "event_notification_schedule")
public class EventNotificationSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long eventId;

    @Column(nullable = false)
    private LocalDateTime scheduledAt;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private NotificationScheduleType type;

    private Integer minutesBefore;

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean sent = false;

    private LocalDateTime sentAt;

    @Builder
    public EventNotificationSchedule(
            @NonNull Long eventId,
            @NonNull LocalDateTime scheduledAt,
            @NonNull NotificationScheduleType type,
            Integer minutesBefore
    ) {
        this.eventId = eventId;
        this.scheduledAt = scheduledAt;
        this.type = type;
        this.minutesBefore = minutesBefore;
        this.sent = false;
    }

    public void markAsSent() {
        this.sent = true;
        this.sentAt = LocalDateTime.now();
    }
}
