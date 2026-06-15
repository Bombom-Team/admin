package me.bombom.api.v1.holiday.service;

import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.common.config.QuerydslConfig;
import me.bombom.api.v1.holiday.domain.Holiday;
import me.bombom.api.v1.holiday.dto.request.CreateHolidayRequest;
import me.bombom.api.v1.holiday.dto.request.GetHolidaysRequest;
import me.bombom.api.v1.holiday.dto.request.UpdateHolidayRequest;
import me.bombom.api.v1.holiday.dto.response.GetHolidayResponse;
import me.bombom.api.v1.holiday.repository.HolidayRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.TestPropertySource;

@DataJpaTest
@Import({ HolidayService.class, QuerydslConfig.class })
@EnableJpaAuditing
@TestPropertySource(properties = "spring.main.allow-bean-definition-overriding=true")
class HolidayServiceTest {

    @Autowired
    private HolidayService holidayService;

    @Autowired
    private HolidayRepository holidayRepository;

    @Test
    @DisplayName("공휴일을 등록한다.")
    void 공휴일_등록_성공() {
        // given
        CreateHolidayRequest request = new CreateHolidayRequest(LocalDate.of(2026, 3, 1), "삼일절");

        // when
        holidayService.createHoliday(request);

        // then
        List<Holiday> holidays = holidayRepository.findAll();

        assertSoftly(softly -> {
            softly.assertThat(holidays).hasSize(1);
            softly.assertThat(holidays.getFirst().getDate()).isEqualTo(LocalDate.of(2026, 3, 1));
            softly.assertThat(holidays.getFirst().getName()).isEqualTo("삼일절");
        });
    }

    @Test
    @DisplayName("이미 등록된 날짜로 공휴일 등록 시 예외가 발생한다.")
    void 중복_날짜_공휴일_등록_시_예외_발생() {
        // given
        holidayRepository.save(Holiday.builder()
                .date(LocalDate.of(2026, 3, 1))
                .name("삼일절")
                .build());

        CreateHolidayRequest request = new CreateHolidayRequest(LocalDate.of(2026, 3, 1), "중복 공휴일");

        // when & then
        assertThatThrownBy(() -> holidayService.createHoliday(request))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasMessage(ErrorDetail.DUPLICATED_DATA.getMessage());
    }

    @Test
    @DisplayName("연도별 공휴일 목록을 조회한다.")
    void 연도별_공휴일_목록_조회_성공() {
        // given
        holidayRepository.save(Holiday.builder()
                .date(LocalDate.of(2026, 3, 1))
                .name("삼일절")
                .build());
        holidayRepository.save(Holiday.builder()
                .date(LocalDate.of(2026, 1, 1))
                .name("신정")
                .build());
        holidayRepository.save(Holiday.builder()
                .date(LocalDate.of(2027, 1, 1))
                .name("신정")
                .build());

        GetHolidaysRequest request = new GetHolidaysRequest(2026);

        // when
        List<GetHolidayResponse> responses = holidayService.getHolidays(request);

        // then
        assertSoftly(softly -> {
            softly.assertThat(responses).hasSize(2);
            softly.assertThat(responses.get(0).date()).isEqualTo(LocalDate.of(2026, 1, 1));
            softly.assertThat(responses.get(1).date()).isEqualTo(LocalDate.of(2026, 3, 1));
        });
    }

    @Test
    @DisplayName("공휴일을 수정한다.")
    void 공휴일_수정_성공() {
        // given
        Holiday holiday = holidayRepository.save(Holiday.builder()
                .date(LocalDate.of(2026, 3, 1))
                .name("삼일절")
                .build());

        UpdateHolidayRequest request = new UpdateHolidayRequest(LocalDate.of(2026, 3, 2), "대체공휴일");

        // when
        holidayService.updateHoliday(holiday.getId(), request);

        // then
        Holiday updatedHoliday = holidayRepository.findById(holiday.getId()).orElseThrow();

        assertSoftly(softly -> {
            softly.assertThat(updatedHoliday.getDate()).isEqualTo(LocalDate.of(2026, 3, 2));
            softly.assertThat(updatedHoliday.getName()).isEqualTo("대체공휴일");
        });
    }

    @Test
    @DisplayName("이미 등록된 날짜로 공휴일 수정 시 예외가 발생한다.")
    void 중복_날짜_공휴일_수정_시_예외_발생() {
        // given
        Holiday holiday = holidayRepository.save(Holiday.builder()
                .date(LocalDate.of(2026, 3, 1))
                .name("삼일절")
                .build());
        holidayRepository.save(Holiday.builder()
                .date(LocalDate.of(2026, 5, 5))
                .name("어린이날")
                .build());

        UpdateHolidayRequest request = new UpdateHolidayRequest(LocalDate.of(2026, 5, 5), "중복 공휴일");

        // when & then
        assertThatThrownBy(() -> holidayService.updateHoliday(holiday.getId(), request))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasMessage(ErrorDetail.DUPLICATED_DATA.getMessage());
    }

    @Test
    @DisplayName("공휴일을 삭제한다.")
    void 공휴일_삭제_성공() {
        // given
        Holiday holiday = holidayRepository.save(Holiday.builder()
                .date(LocalDate.of(2026, 3, 1))
                .name("삼일절")
                .build());

        // when
        holidayService.deleteHoliday(holiday.getId());

        // then
        assertThat(holidayRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 공휴일 수정 시 예외가 발생한다.")
    void 존재하지_않는_공휴일_수정_시_예외_발생() {
        // given
        UpdateHolidayRequest request = new UpdateHolidayRequest(LocalDate.of(2026, 3, 1), "삼일절");

        // when & then
        assertThatThrownBy(() -> holidayService.updateHoliday(999L, request))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasMessage(ErrorDetail.ENTITY_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("존재하지 않는 공휴일 삭제 시 예외가 발생한다.")
    void 존재하지_않는_공휴일_삭제_시_예외_발생() {
        // when & then
        assertThatThrownBy(() -> holidayService.deleteHoliday(999L))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasMessage(ErrorDetail.ENTITY_NOT_FOUND.getMessage());
    }
}
