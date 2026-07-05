package me.bombom.api.v1.challenge.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import me.bombom.api.v1.challenge.domain.DailyGuideType;
import me.bombom.api.v1.challenge.dto.CreateDailyGuideRequest;
import me.bombom.api.v1.challenge.dto.GetDailyGuideResponse;
import me.bombom.api.v1.challenge.dto.UpdateDailyGuideRequest;
import me.bombom.api.v1.challenge.service.ChallengeDailyGuideService;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.common.support.ControllerTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTest(controllers = ChallengeDailyGuideController.class)
class ChallengeDailyGuideControllerTest extends ControllerTestSupport {

    @MockitoBean
    private ChallengeDailyGuideService dailyGuideService;

    @Test
    void S3_이미지_목록을_조회한다() throws Exception {
        // given
        given(dailyGuideService.getChallengeImages()).willReturn(List.of(
                "https://bombom-challenge.s3.ap-northeast-2.amazonaws.com/day1.jpg",
                "https://bombom-challenge.s3.ap-northeast-2.amazonaws.com/day2.jpg"
        ));

        // when // then
        mockMvc.perform(get("/admin/api/v1/challenges/1/daily-guides/images"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void 새_이미지를_업로드해서_데일리_가이드를_생성한다() throws Exception {
        // given
        MockMultipartFile image = new MockMultipartFile("image", "test.jpg", "image/jpeg", "content".getBytes());
        CreateDailyGuideRequest request = new CreateDailyGuideRequest(1, DailyGuideType.READ, "day1-guide", null, "안내");
        MockMultipartFile requestPart = new MockMultipartFile("request", "", "application/json",
                objectMapper.writeValueAsBytes(request));

        // when // then
        mockMvc.perform(multipart("/admin/api/v1/challenges/1/daily-guides")
                        .file(image)
                        .file(requestPart))
                .andExpect(status().isCreated());
    }

    @Test
    void 기존_이미지_URL로_데일리_가이드를_생성한다() throws Exception {
        // given
        CreateDailyGuideRequest request = new CreateDailyGuideRequest(
                1, DailyGuideType.READ, null,
                "https://bombom-challenge.s3.ap-northeast-2.amazonaws.com/day1.jpg",
                "안내");
        MockMultipartFile requestPart = new MockMultipartFile("request", "", "application/json",
                objectMapper.writeValueAsBytes(request));

        // when // then
        mockMvc.perform(multipart("/admin/api/v1/challenges/1/daily-guides")
                        .file(requestPart))
                .andExpect(status().isCreated());
    }

    @Test
    void 생성_시_필수_필드가_없으면_400을_반환한다() throws Exception {
        // given - dayIndex 없음
        CreateDailyGuideRequest request = new CreateDailyGuideRequest(null, DailyGuideType.READ, null, null, null);
        MockMultipartFile requestPart = new MockMultipartFile("request", "", "application/json",
                objectMapper.writeValueAsBytes(request));

        // when // then
        mockMvc.perform(multipart("/admin/api/v1/challenges/1/daily-guides")
                        .file(requestPart))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 생성_시_존재하지_않는_챌린지이면_404를_반환한다() throws Exception {
        // given
        CreateDailyGuideRequest request = new CreateDailyGuideRequest(
                1, DailyGuideType.READ, null,
                "https://bombom-challenge.s3.ap-northeast-2.amazonaws.com/day1.jpg",
                null);
        MockMultipartFile requestPart = new MockMultipartFile("request", "", "application/json",
                objectMapper.writeValueAsBytes(request));
        willThrow(new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND))
                .given(dailyGuideService).create(any(), any(), any());

        // when // then
        mockMvc.perform(multipart("/admin/api/v1/challenges/999/daily-guides")
                        .file(requestPart))
                .andExpect(status().isNotFound());
    }

    @Test
    void 데일리_가이드_목록을_조회한다() throws Exception {
        // given
        given(dailyGuideService.getDailyGuides(any())).willReturn(List.of(
                new GetDailyGuideResponse(1L, 1L, 1, DailyGuideType.READ,
                        "https://bombom-challenge.s3.ap-northeast-2.amazonaws.com/day1.jpg", "안내", true),
                new GetDailyGuideResponse(2L, 1L, 2, DailyGuideType.COMMENT,
                        "https://bombom-challenge.s3.ap-northeast-2.amazonaws.com/day2.jpg", null, false)
        ));

        // when // then
        mockMvc.perform(get("/admin/api/v1/challenges/1/daily-guides"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].dayIndex").value(1))
                .andExpect(jsonPath("$[1].dayIndex").value(2));
    }

    @Test
    void 데일리_가이드_단건을_조회한다() throws Exception {
        // given
        given(dailyGuideService.getDailyGuide(any(), any())).willReturn(
                new GetDailyGuideResponse(1L, 1L, 1, DailyGuideType.READ,
                        "https://bombom-challenge.s3.ap-northeast-2.amazonaws.com/day1.jpg", "안내", true));

        // when // then
        mockMvc.perform(get("/admin/api/v1/challenges/1/daily-guides/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.type").value("READ"));
    }

    @Test
    void 존재하지_않는_가이드를_조회하면_404를_반환한다() throws Exception {
        // given
        given(dailyGuideService.getDailyGuide(any(), any()))
                .willThrow(new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND));

        // when // then
        mockMvc.perform(get("/admin/api/v1/challenges/1/daily-guides/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void dayIndex로_데일리_가이드를_조회한다() throws Exception {
        // given
        given(dailyGuideService.getDailyGuideByDayIndex(any(), anyInt())).willReturn(
                new GetDailyGuideResponse(1L, 1L, 3, DailyGuideType.READ,
                        "https://bombom-challenge.s3.ap-northeast-2.amazonaws.com/day3.jpg", "안내", false));

        // when // then
        mockMvc.perform(get("/admin/api/v1/challenges/1/daily-guides/days/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dayIndex").value(3));
    }

    @Test
    void dayIndex에_해당하는_가이드가_없으면_404를_반환한다() throws Exception {
        // given
        given(dailyGuideService.getDailyGuideByDayIndex(any(), anyInt()))
                .willThrow(new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND));

        // when // then
        mockMvc.perform(get("/admin/api/v1/challenges/1/daily-guides/days/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void 새_이미지를_업로드해서_데일리_가이드를_수정한다() throws Exception {
        // given
        MockMultipartFile image = new MockMultipartFile("image", "new.jpg", "image/jpeg", "new".getBytes());
        UpdateDailyGuideRequest request = new UpdateDailyGuideRequest(2, DailyGuideType.COMMENT, "new-guide", null, "새 안내");
        MockMultipartFile requestPart = new MockMultipartFile("request", "", "application/json",
                objectMapper.writeValueAsBytes(request));

        // when // then
        mockMvc.perform(multipart("/admin/api/v1/challenges/1/daily-guides/1")
                        .file(image)
                        .file(requestPart)
                        .with(req -> { req.setMethod("PATCH"); return req; }))
                .andExpect(status().isOk());
    }

    @Test
    void 기존_이미지_URL로_데일리_가이드를_수정한다() throws Exception {
        // given
        UpdateDailyGuideRequest request = new UpdateDailyGuideRequest(
                2, null, null, "https://bombom-challenge.s3.ap-northeast-2.amazonaws.com/other.jpg", null);
        MockMultipartFile requestPart = new MockMultipartFile("request", "", "application/json",
                objectMapper.writeValueAsBytes(request));

        // when // then
        mockMvc.perform(multipart("/admin/api/v1/challenges/1/daily-guides/1")
                        .file(requestPart)
                        .with(req -> { req.setMethod("PATCH"); return req; }))
                .andExpect(status().isOk());
    }

    @Test
    void 데일리_가이드를_삭제한다() throws Exception {
        // when // then
        mockMvc.perform(delete("/admin/api/v1/challenges/1/daily-guides/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void 존재하지_않는_가이드를_삭제하면_404를_반환한다() throws Exception {
        // given
        willThrow(new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND))
                .given(dailyGuideService).delete(any(), any());

        // when // then
        mockMvc.perform(delete("/admin/api/v1/challenges/1/daily-guides/999"))
                .andExpect(status().isNotFound());
    }
}
