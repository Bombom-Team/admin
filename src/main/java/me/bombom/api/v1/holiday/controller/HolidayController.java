package me.bombom.api.v1.holiday.controller;

import me.bombom.api.v1.holiday.dto.request.CreateHolidayRequest;
import me.bombom.api.v1.holiday.dto.request.GetHolidaysRequest;
import me.bombom.api.v1.holiday.dto.request.UpdateHolidayRequest;
import me.bombom.api.v1.holiday.dto.response.GetHolidayResponse;
import me.bombom.api.v1.holiday.service.HolidayService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/api/v1/holidays")
public class HolidayController implements HolidayControllerApi {

    private final HolidayService holidayService;

    @Override
    @GetMapping
    public List<GetHolidayResponse> getHolidays(@Valid @ModelAttribute GetHolidaysRequest request) {
        return holidayService.getHolidays(request);
    }

    @Override
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createHoliday(@Valid @RequestBody CreateHolidayRequest request) {
        holidayService.createHoliday(request);
    }

    @Override
    @PatchMapping("/{id}")
    public void updateHoliday(
            @PathVariable @Positive(message = "id는 1 이상의 값이어야 합니다.") Long id,
            @Valid @RequestBody UpdateHolidayRequest request
    ) {
        holidayService.updateHoliday(id, request);
    }

    @Override
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteHoliday(@PathVariable @Positive(message = "id는 1 이상의 값이어야 합니다.") Long id) {
        holidayService.deleteHoliday(id);
    }
}
