package me.bombom.api.v1.challenge.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import me.bombom.api.v1.challenge.dto.GetChallengeParticipantResponse;
import me.bombom.api.v1.challenge.dto.GetChallengeResponse;
import me.bombom.api.v1.challenge.service.ChallengeService;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.common.support.ControllerTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@WebMvcTest(controllers = ChallengeController.class)
class ChallengeControllerTest extends ControllerTestSupport {

        @MockitoBean
        private ChallengeService challengeService;

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
                // when // then
                mockMvc.perform(MockMvcRequestBuilders.post("/admin/api/v1/challenges/1/teams/assignment"))
                                .andExpect(status().isOk());
        }
}
