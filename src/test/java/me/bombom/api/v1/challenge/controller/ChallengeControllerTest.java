package me.bombom.api.v1.challenge.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;
import me.bombom.api.v1.challenge.dto.AssignTeamsRequest;
import me.bombom.api.v1.challenge.dto.GetChallengeParticipantResponse;
import me.bombom.api.v1.challenge.dto.GetChallengeResponse;
import me.bombom.api.v1.challenge.dto.request.GrantShieldRequest;
import me.bombom.api.v1.challenge.dto.request.UpdateChallengeRequest;
import me.bombom.api.v1.challenge.service.ChallengeService;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.common.support.ControllerTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@WebMvcTest(controllers = ChallengeController.class)
class ChallengeControllerTest extends ControllerTestSupport {

        @MockitoBean
        private ChallengeService challengeService;

        @Test
        void 챌린지를_생성한다() throws Exception {
                // given
                String requestBody = """
                                {
                                    "name": "테스트 챌린지",
                                    "generation": 1,
                                    "startDate": "2025-01-06",
                                    "endDate": "2025-01-10"
                                }
                                """;

                // when & then
                mockMvc.perform(post("/admin/api/v1/challenges")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody))
                                .andExpect(status().isCreated());
        }

        @Test
        void 챌린지_생성_시_name이_없으면_400을_반환한다() throws Exception {
                // given
                String requestBody = """
                                {
                                    "generation": 1,
                                    "startDate": "2025-01-06",
                                    "endDate": "2025-01-10"
                                }
                                """;

                // when & then
                mockMvc.perform(post("/admin/api/v1/challenges")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void 챌린지_생성_시_generation이_0이면_400을_반환한다() throws Exception {
                // given
                String requestBody = """
                                {
                                    "name": "테스트 챌린지",
                                    "generation": 0,
                                    "startDate": "2025-01-06",
                                    "endDate": "2025-01-10"
                                }
                                """;

                // when & then
                mockMvc.perform(post("/admin/api/v1/challenges")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void 챌린지_생성_시_startDate가_없으면_400을_반환한다() throws Exception {
                // given
                String requestBody = """
                                {
                                    "name": "테스트 챌린지",
                                    "generation": 1,
                                    "endDate": "2025-01-10"
                                }
                                """;

                // when & then
                mockMvc.perform(post("/admin/api/v1/challenges")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void 챌린지_생성_시_endDate가_없으면_400을_반환한다() throws Exception {
                // given
                String requestBody = """
                                {
                                    "name": "테스트 챌린지",
                                    "generation": 1,
                                    "startDate": "2025-01-06"
                                }
                                """;

                // when & then
                mockMvc.perform(post("/admin/api/v1/challenges")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void 챌린지를_수정한다() throws Exception {
                // given
                UpdateChallengeRequest request = new UpdateChallengeRequest(
                                "수정된 챌린지", 2, LocalDate.of(2025, 1, 6), LocalDate.of(2025, 1, 10));

                // when & then
                mockMvc.perform(patch("/admin/api/v1/challenges/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk());
        }

        @Test
        void 존재하지_않는_챌린지_수정_시_404를_반환한다() throws Exception {
                // given
                doThrow(new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND))
                                .when(challengeService).updateChallenge(any(), any());
                UpdateChallengeRequest request = new UpdateChallengeRequest("수정된 챌린지", null, null, null);

                // when & then
                mockMvc.perform(patch("/admin/api/v1/challenges/999")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isNotFound());
        }

        @Test
        void 챌린지를_삭제한다() throws Exception {
                // when & then
                mockMvc.perform(delete("/admin/api/v1/challenges/1"))
                                .andExpect(status().isOk());
        }

        @Test
        void 참여자가_있는_챌린지_삭제_시_400을_반환한다() throws Exception {
                // given
                doThrow(new CIllegalArgumentException(ErrorDetail.CHALLENGE_HAS_PARTICIPANTS))
                                .when(challengeService).deleteChallenge(any());

                // when & then
                mockMvc.perform(delete("/admin/api/v1/challenges/1"))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void 존재하지_않는_챌린지_삭제_시_404를_반환한다() throws Exception {
                // given
                doThrow(new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND))
                                .when(challengeService).deleteChallenge(any());

                // when & then
                mockMvc.perform(delete("/admin/api/v1/challenges/999"))
                                .andExpect(status().isNotFound());
        }

        @Test
        void 챌린지_목록을_조회한다() throws Exception {
                // given
                given(challengeService.getChallenges(any(), any(Pageable.class)))
                                .willReturn(new PageImpl<>(List.of(GetChallengeResponse.builder().build())));

                // when // then
                mockMvc.perform(get("/admin/api/v1/challenges"))
                                .andExpect(status().isOk());
        }

        @Test
        void 상태별_챌린지_목록을_조회한다() throws Exception {
                // given
                given(challengeService.getChallenges(any(), any(Pageable.class)))
                                .willReturn(new PageImpl<>(List.of(GetChallengeResponse.builder().build())));

                // when // then
                mockMvc.perform(get("/admin/api/v1/challenges")
                                .param("status", "ONGOING"))
                                .andExpect(status().isOk());
        }

        @Test
        void 챌린지_단건_상세_정보를_조회한다() throws Exception {
                // given
                given(challengeService.getChallenge(any()))
                                .willReturn(me.bombom.api.v1.challenge.dto.GetChallengeDetailResponse.builder()
                                                .build());

                // when // then
                mockMvc.perform(get("/admin/api/v1/challenges/1"))
                                .andExpect(status().isOk());
        }

        @Test
        void 존재하지_않는_챌린지_상세_정보를_조회할_수_없다() throws Exception {
                // given
                given(challengeService.getChallenge(any()))
                                .willThrow(new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND));

                // when // then
                mockMvc.perform(get("/admin/api/v1/challenges/1"))
                                .andExpect(status().isNotFound());
        }

        @Test
        void 챌린지_참여자_목록을_조회한다() throws Exception {
                // given
                given(challengeService.getChallengeParticipants(any(), any(), any(Pageable.class)))
                                .willReturn(new PageImpl<>(List.of(GetChallengeParticipantResponse.builder().build())));

                // when // then
                mockMvc.perform(get("/admin/api/v1/challenges/1/participants")
                                .param("hasTeam", "true"))
                                .andExpect(status().isOk());
        }

        @Test
        void 팀이_없는_참여자_목록을_조회한다() throws Exception {
                // given
                given(challengeService.getChallengeParticipants(any(), any(), any(Pageable.class)))
                                .willReturn(new PageImpl<>(List.of(GetChallengeParticipantResponse.builder().build())));

                // when // then
                mockMvc.perform(get("/admin/api/v1/challenges/1/participants")
                                .param("hasTeam", "false"))
                                .andExpect(status().isOk());
        }

        @Test
        void 특정_팀의_참여자_목록을_조회한다() throws Exception {
                // given
                given(challengeService.getChallengeParticipants(any(), any(), any(Pageable.class)))
                                .willReturn(new PageImpl<>(List.of(GetChallengeParticipantResponse.builder().build())));

                // when // then
                mockMvc.perform(get("/admin/api/v1/challenges/1/participants")
                                .param("challengeTeamId", "1")
                                .param("hasTeam", "true"))
                                .andExpect(status().isOk());
        }

        @Test
        void 챌린지_팀을_자동_배정한다() throws Exception {
                // given
                AssignTeamsRequest request = new AssignTeamsRequest(15);

                // when // then
                mockMvc.perform(MockMvcRequestBuilders.post("/admin/api/v1/challenges/1/teams/assignment")
                                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk());
        }

        @Test
        void 생존자들에게_쉴드를_지급한다() throws Exception {
                // given
                GrantShieldRequest request = new GrantShieldRequest(2);

                // when // then
                mockMvc.perform(MockMvcRequestBuilders.post("/admin/api/v1/challenges/1/participants/shield")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk());
        }
}
