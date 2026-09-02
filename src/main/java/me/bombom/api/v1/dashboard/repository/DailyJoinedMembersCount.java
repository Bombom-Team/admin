package me.bombom.api.v1.dashboard.repository;

import java.time.LocalDate;

public interface DailyJoinedMembersCount {

    LocalDate getDate();

    long getCount();
}
