package me.bombom.api.v1.holiday.fixture;

import me.bombom.api.v1.holiday.domain.Holiday;

import static org.instancio.Select.field;

import java.time.LocalDate;
import org.instancio.Instancio;

public class HolidayFixture {

    public static Holiday createHoliday(LocalDate date, String name) {
        return Instancio.of(Holiday.class)
                .set(field(Holiday::getId), null)
                .set(field(Holiday::getDate), date)
                .set(field(Holiday::getName), name)
                .create();
    }
}
