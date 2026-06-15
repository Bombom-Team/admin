package me.bombom.api.v1.holiday.service;

import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorContextKeys;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.holiday.domain.Holiday;
import me.bombom.api.v1.holiday.dto.request.CreateHolidayRequest;
import me.bombom.api.v1.holiday.dto.request.GetHolidaysRequest;
import me.bombom.api.v1.holiday.dto.request.UpdateHolidayRequest;
import me.bombom.api.v1.holiday.dto.response.GetHolidayResponse;
import me.bombom.api.v1.holiday.repository.HolidayRepository;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HolidayService {

    private final HolidayRepository holidayRepository;

    public List<GetHolidayResponse> getHolidays(GetHolidaysRequest request) {
        return holidayRepository.findByDateBetweenOrderByDate(request.startDate(), request.endDate())
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
    public void updateHoliday(Long id, UpdateHolidayRequest request) {
        Holiday holiday = getHolidayEntity(id, "updateHoliday");
        validateDuplicatedDate(id, request.date());

        holiday.update(request.date(), request.name());
    }

    @Transactional
    public void deleteHoliday(Long id) {
        Holiday holiday = getHolidayEntity(id, "deleteHoliday");

        holidayRepository.delete(holiday);
    }

    private Holiday getHolidayEntity(Long id, String operation) {
        return holidayRepository.findById(id)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext(ErrorContextKeys.ENTITY_TYPE, "holiday")
                        .addContext(ErrorContextKeys.OPERATION, operation));
    }

    private void validateDuplicatedDate(LocalDate date) {
        if (holidayRepository.existsByDate(date)) {
            throw duplicatedHolidayDateException("createHoliday");
        }
    }

    private void validateDuplicatedDate(Long id, LocalDate date) {
        if (holidayRepository.existsByDateAndIdNot(date, id)) {
            throw duplicatedHolidayDateException("updateHoliday");
        }
    }

    private CIllegalArgumentException duplicatedHolidayDateException(String operation) {
        return new CIllegalArgumentException(ErrorDetail.DUPLICATED_DATA)
                .addContext(ErrorContextKeys.ENTITY_TYPE, "holiday")
                .addContext(ErrorContextKeys.OPERATION, operation);
    }
}
