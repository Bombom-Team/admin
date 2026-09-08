package me.bombom.api.v1.holiday.service;

import me.bombom.api.v1.common.config.QuerydslConfig;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.holiday.domain.Holiday;
import me.bombom.api.v1.holiday.dto.CreateHolidayRequest;
import me.bombom.api.v1.holiday.dto.CreateHolidaysRequest;
import me.bombom.api.v1.holiday.dto.GetHolidayResponse;
import me.bombom.api.v1.holiday.dto.GetHolidaysRequest;
import me.bombom.api.v1.holiday.dto.UpdateHolidayRequest;
import me.bombom.api.v1.holiday.fixture.HolidayFixture;
import me.bombom.api.v1.holiday.repository.HolidayRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
import java.util.List;

@DataJpaTest
@Import({ HolidayService.class, QuerydslConfig.class })
class HolidayServiceTest {

    @Autowired
    private HolidayService holidayService;

    @Autowired
    private HolidayRepository holidayRepository;

    @Test
    void 공휴일_등록() {
        // given
        CreateHolidayRequest request = new CreateHolidayRequest(LocalDate.of(2026, 1, 1), "신정");

        // when
        holidayService.createHoliday(request);

        // then
        Holiday holiday = holidayRepository.findAll().getFirst();

        assertSoftly(softly -> {
            softly.assertThat(holiday.getDate()).isEqualTo(LocalDate.of(2026, 1, 1));
            softly.assertThat(holiday.getName()).isEqualTo("신정");
        });
    }

    @Test
    void 이미_등록된_날짜로_등록_시_예외() {
        // given
        holidayRepository.save(HolidayFixture.createHoliday(LocalDate.of(2026, 1, 1), "신정"));
        CreateHolidayRequest request = new CreateHolidayRequest(LocalDate.of(2026, 1, 1), "중복된 신정");

        // when & then
        assertThatThrownBy(() -> holidayService.createHoliday(request))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasMessage(ErrorDetail.DUPLICATED_DATA.getMessage());
    }

    @Test
    void 공휴일_일괄_등록() {
        // given
        CreateHolidaysRequest request = new CreateHolidaysRequest(List.of(
                new CreateHolidayRequest(LocalDate.of(2026, 3, 1), "삼일절"),
                new CreateHolidayRequest(LocalDate.of(2026, 1, 1), "신정")));

        // when
        holidayService.createHolidays(request);

        // then
        List<Holiday> holidays = holidayRepository.findAllByOrderByDate();

        assertSoftly(softly -> {
            softly.assertThat(holidays).hasSize(2);
            softly.assertThat(holidays.get(0).getName()).isEqualTo("신정");
            softly.assertThat(holidays.get(1).getName()).isEqualTo("삼일절");
        });
    }

    @Test
    void 일괄_등록_요청에_중복된_날짜가_있으면_예외() {
        // given
        CreateHolidaysRequest request = new CreateHolidaysRequest(List.of(
                new CreateHolidayRequest(LocalDate.of(2026, 1, 1), "신정"),
                new CreateHolidayRequest(LocalDate.of(2026, 1, 1), "중복된 신정")));

        // when & then
        assertThatThrownBy(() -> holidayService.createHolidays(request))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasMessage(ErrorDetail.DUPLICATED_DATA.getMessage());
    }

    @Test
    void 일괄_등록_중_한_건이라도_이미_등록된_날짜면_전체가_저장되지_않음() {
        // given
        holidayRepository.save(HolidayFixture.createHoliday(LocalDate.of(2026, 3, 1), "삼일절"));
        CreateHolidaysRequest request = new CreateHolidaysRequest(List.of(
                new CreateHolidayRequest(LocalDate.of(2026, 1, 1), "신정"),
                new CreateHolidayRequest(LocalDate.of(2026, 3, 1), "중복된 삼일절")));

        // when
        assertThatThrownBy(() -> holidayService.createHolidays(request))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasMessage(ErrorDetail.DUPLICATED_DATA.getMessage());

        // then
        assertThat(holidayRepository.findAll()).hasSize(1);
    }

    @Test
    void 공휴일_수정() {
        // given
        Holiday holiday = holidayRepository.save(HolidayFixture.createHoliday(LocalDate.of(2026, 3, 1), "삼일절"));
        UpdateHolidayRequest request = new UpdateHolidayRequest(LocalDate.of(2026, 3, 2), "삼일절 대체공휴일");

        // when
        holidayService.updateHoliday(holiday.getId(), request);

        // then
        Holiday updatedHoliday = holidayRepository.findById(holiday.getId()).orElseThrow();

        assertSoftly(softly -> {
            softly.assertThat(updatedHoliday.getDate()).isEqualTo(LocalDate.of(2026, 3, 2));
            softly.assertThat(updatedHoliday.getName()).isEqualTo("삼일절 대체공휴일");
        });
    }

    @Test
    void 공휴일_수정_null_필드는_변경되지_않음() {
        // given
        Holiday holiday = holidayRepository.save(HolidayFixture.createHoliday(LocalDate.of(2026, 3, 1), "삼일절"));
        UpdateHolidayRequest request = new UpdateHolidayRequest(null, "수정된 이름");

        // when
        holidayService.updateHoliday(holiday.getId(), request);

        // then
        Holiday updatedHoliday = holidayRepository.findById(holiday.getId()).orElseThrow();

        assertSoftly(softly -> {
            softly.assertThat(updatedHoliday.getDate()).isEqualTo(LocalDate.of(2026, 3, 1));
            softly.assertThat(updatedHoliday.getName()).isEqualTo("수정된 이름");
        });
    }

    @Test
    void 다른_공휴일이_사용_중인_날짜로_수정_시_예외() {
        // given
        holidayRepository.save(HolidayFixture.createHoliday(LocalDate.of(2026, 1, 1), "신정"));
        Holiday holiday = holidayRepository.save(HolidayFixture.createHoliday(LocalDate.of(2026, 3, 1), "삼일절"));
        UpdateHolidayRequest request = new UpdateHolidayRequest(LocalDate.of(2026, 1, 1), "삼일절");

        // when & then
        assertThatThrownBy(() -> holidayService.updateHoliday(holiday.getId(), request))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasMessage(ErrorDetail.DUPLICATED_DATA.getMessage());
    }

    @Test
    void 자기_자신과_같은_날짜로_수정_시_예외가_발생하지_않음() {
        // given
        Holiday holiday = holidayRepository.save(HolidayFixture.createHoliday(LocalDate.of(2026, 3, 1), "삼일절"));
        UpdateHolidayRequest request = new UpdateHolidayRequest(LocalDate.of(2026, 3, 1), "수정된 이름");

        // when
        holidayService.updateHoliday(holiday.getId(), request);

        // then
        assertThat(holidayRepository.findById(holiday.getId()).orElseThrow().getName()).isEqualTo("수정된 이름");
    }

    @Test
    void 존재하지_않는_공휴일_수정_시_예외() {
        // given
        UpdateHolidayRequest request = new UpdateHolidayRequest(LocalDate.of(2026, 1, 1), "신정");

        // when & then
        assertThatThrownBy(() -> holidayService.updateHoliday(999L, request))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasMessage(ErrorDetail.ENTITY_NOT_FOUND.getMessage());
    }

    @Test
    void 공휴일_삭제() {
        // given
        Holiday holiday = holidayRepository.save(HolidayFixture.createHoliday(LocalDate.of(2026, 1, 1), "신정"));

        // when
        holidayService.deleteHoliday(holiday.getId());

        // then
        assertThat(holidayRepository.findAll()).isEmpty();
    }

    @Test
    void 존재하지_않는_공휴일_삭제_시_예외() {
        // when & then
        assertThatThrownBy(() -> holidayService.deleteHoliday(999L))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasMessage(ErrorDetail.ENTITY_NOT_FOUND.getMessage());
    }

    @Test
    void 공휴일_목록_조회_날짜_오름차순() {
        // given
        holidayRepository.save(HolidayFixture.createHoliday(LocalDate.of(2026, 3, 1), "삼일절"));
        holidayRepository.save(HolidayFixture.createHoliday(LocalDate.of(2026, 1, 1), "신정"));

        // when
        List<GetHolidayResponse> result = holidayService.getHolidays(new GetHolidaysRequest(null));

        // then
        assertSoftly(softly -> {
            softly.assertThat(result).hasSize(2);
            softly.assertThat(result.get(0).name()).isEqualTo("신정");
            softly.assertThat(result.get(1).name()).isEqualTo("삼일절");
        });
    }

    @Test
    void 공휴일_목록_조회_연도_필터_적용() {
        // given
        holidayRepository.save(HolidayFixture.createHoliday(LocalDate.of(2025, 12, 25), "성탄절"));
        holidayRepository.save(HolidayFixture.createHoliday(LocalDate.of(2026, 1, 1), "신정"));
        holidayRepository.save(HolidayFixture.createHoliday(LocalDate.of(2026, 12, 25), "성탄절"));
        holidayRepository.save(HolidayFixture.createHoliday(LocalDate.of(2027, 1, 1), "신정"));

        // when
        List<GetHolidayResponse> result = holidayService.getHolidays(new GetHolidaysRequest(2026));

        // then
        assertSoftly(softly -> {
            softly.assertThat(result).hasSize(2);
            softly.assertThat(result.get(0).date()).isEqualTo(LocalDate.of(2026, 1, 1));
            softly.assertThat(result.get(1).date()).isEqualTo(LocalDate.of(2026, 12, 25));
        });
    }
}
