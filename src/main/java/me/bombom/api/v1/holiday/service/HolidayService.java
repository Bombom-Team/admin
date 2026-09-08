package me.bombom.api.v1.holiday.service;

import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorContextKeys;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.holiday.domain.Holiday;
import me.bombom.api.v1.holiday.dto.CreateHolidayRequest;
import me.bombom.api.v1.holiday.dto.CreateHolidaysRequest;
import me.bombom.api.v1.holiday.dto.GetHolidayResponse;
import me.bombom.api.v1.holiday.dto.GetHolidaysRequest;
import me.bombom.api.v1.holiday.dto.UpdateHolidayRequest;
import me.bombom.api.v1.holiday.repository.HolidayRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HolidayService {

    private static final String ENTITY_TYPE = "holiday";

    private final HolidayRepository holidayRepository;

    public List<GetHolidayResponse> getHolidays(GetHolidaysRequest request) {
        return findHolidays(request.year())
                .stream()
                .map(GetHolidayResponse::from)
                .toList();
    }

    @Transactional
    public void createHoliday(CreateHolidayRequest request) {
        validateDuplicatedDate(request.date());

        Holiday holiday = Holiday.builder()
                .date(request.date())
                .name(request.name())
                .build();
        holidayRepository.save(holiday);
    }

    @Transactional
    public void createHolidays(CreateHolidaysRequest request) {
        List<LocalDate> dates = request.holidays()
                .stream()
                .map(CreateHolidayRequest::date)
                .toList();
        validateDuplicatedDatesInRequest(dates);
        dates.forEach(this::validateDuplicatedDate);

        List<Holiday> holidays = request.holidays()
                .stream()
                .map(holiday -> Holiday.builder()
                        .date(holiday.date())
                        .name(holiday.name())
                        .build())
                .toList();
        holidayRepository.saveAll(holidays);
    }

    @Transactional
    public void updateHoliday(Long id, UpdateHolidayRequest request) {
        Holiday holiday = getHolidayById(id);
        if (request.date() != null) {
            validateDuplicatedDate(request.date(), id);
        }

        holiday.update(request.date(), request.name());
    }

    @Transactional
    public void deleteHoliday(Long id) {
        holidayRepository.delete(getHolidayById(id));
    }

    private List<Holiday> findHolidays(Integer year) {
        if (year == null) {
            return holidayRepository.findAllByOrderByDate();
        }
        return holidayRepository.findByDateBetweenOrderByDate(
                LocalDate.of(year, 1, 1),
                LocalDate.of(year, 12, 31));
    }

    private Holiday getHolidayById(Long id) {
        return holidayRepository.findById(id)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext(ErrorContextKeys.ENTITY_TYPE, ENTITY_TYPE));
    }

    private void validateDuplicatedDate(LocalDate date) {
        if (holidayRepository.existsByDate(date)) {
            throw duplicatedDateException(date);
        }
    }

    private void validateDuplicatedDatesInRequest(List<LocalDate> dates) {
        Set<LocalDate> distinctDates = new HashSet<>();
        for (LocalDate date : dates) {
            if (distinctDates.add(date)) {
                continue;
            }
            throw duplicatedDateException(date);
        }
    }

    private void validateDuplicatedDate(LocalDate date, Long excludedId) {
        if (holidayRepository.existsByDateAndIdNot(date, excludedId)) {
            throw duplicatedDateException(date);
        }
    }

    private CIllegalArgumentException duplicatedDateException(LocalDate date) {
        return new CIllegalArgumentException(ErrorDetail.DUPLICATED_DATA)
                .addContext(ErrorContextKeys.ENTITY_TYPE, ENTITY_TYPE)
                .addContext("date", date);
    }
}
