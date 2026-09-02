package me.bombom.api.v1.dashboard.dto;

import java.time.LocalDate;

public record DailyJoinedMembersResponse(
        LocalDate date,
        long count
) {

    public static DailyJoinedMembersResponse of(LocalDate date, long count) {
        return new DailyJoinedMembersResponse(date, count);
    }
}
