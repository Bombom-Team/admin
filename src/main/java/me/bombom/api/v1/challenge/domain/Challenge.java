package me.bombom.api.v1.challenge.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.DayOfWeek;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import me.bombom.api.v1.common.BaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Challenge extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int generation;

    private LocalDate startDate;

    private LocalDate endDate;

    @Column(nullable = false)
    private int totalDays;

    @Column(nullable = false)
    private boolean isBadgeIssued = false;

    @Column(nullable = false)
    private Long newsletterGroupId;

    @Builder
    public Challenge(
            Long id,
            @NonNull String name,
            int generation,
            LocalDate startDate,
            LocalDate endDate,
            int totalDays,
            boolean isBadgeIssued,
            @NonNull Long newsletterGroupId
    ) {
        this.id = id;
        this.name = name;
        this.generation = generation;
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalDays = (startDate != null && endDate != null) ? countWeekdays(startDate, endDate) : 0;
        this.isBadgeIssued = isBadgeIssued;
        this.newsletterGroupId = newsletterGroupId;
    }

    public ChallengeStatus getStatus(LocalDate now) {
        if (now.isBefore(this.startDate)) {
            return ChallengeStatus.BEFORE_START;
        }
        if (now.isAfter(this.endDate)) {
            return ChallengeStatus.COMPLETED;
        }
        return ChallengeStatus.ONGOING;
    }

    public void update(String name, Integer generation, LocalDate startDate, LocalDate endDate, Long newsletterGroupId) {
        if (name != null) {
            this.name = name;
        }
        if (generation != null) {
            this.generation = generation;
        }
        if (startDate != null || endDate != null) {
            LocalDate effectiveStart = startDate != null ? startDate : this.startDate;
            LocalDate effectiveEnd = endDate != null ? endDate : this.endDate;
            this.startDate = effectiveStart;
            this.endDate = effectiveEnd;
            this.totalDays = (effectiveStart != null && effectiveEnd != null) ? countWeekdays(effectiveStart, effectiveEnd) : 0;
        }
        if (newsletterGroupId != null) {
            this.newsletterGroupId = newsletterGroupId;
        }
    }

    private static int countWeekdays(LocalDate startDate, LocalDate endDate) {
        int count = 0;
        LocalDate date = startDate;
        while (!date.isAfter(endDate)) {
            DayOfWeek dayOfWeek = date.getDayOfWeek();
            if (dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY) {
                count++;
            }
            date = date.plusDays(1);
        }
        return count;
    }
}
