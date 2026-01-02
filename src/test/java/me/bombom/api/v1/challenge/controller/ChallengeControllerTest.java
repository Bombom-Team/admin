package me.bombom.api.v1.challenge.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import me.bombom.api.v1.challenge.dto.GetChallengeParticipantResponse;
import me.bombom.api.v1.challenge.dto.GetChallengeResponse;
import me.bombom.api.v1.challenge.service.ChallengeService;
import me.bombom.api.v1.common.support.ControllerTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTest(controllers = ChallengeController.class)
class ChallengeControllerTest extends ControllerTestSupport {

        @MockitoBean
        private ChallengeService challengeService;

        @DisplayName("챌린지 목록을 조회한다.")
        @Test
        void getChallenges() throws Exception {
                // given
                given(challengeService.getChallenges(any(), any(Pageable.class)))
                                .willReturn(new PageImpl<>(List.of(GetChallengeResponse.builder().build())));

                // when // then
                mockMvc.perform(get("/admin/api/v1/challenges"))
                                .andExpect(status().isOk());
        }

        @DisplayName("상태별 챌린지 목록을 조회한다.")
        @Test
        void getChallengesByStatus() throws Exception {
                // given
                given(challengeService.getChallenges(any(), any(Pageable.class)))
                                .willReturn(new PageImpl<>(List.of(GetChallengeResponse.builder().build())));

                // when // then
                mockMvc.perform(get("/admin/api/v1/challenges")
                                .param("status", "ONGOING"))
                                .andExpect(status().isOk());
        }

        @DisplayName("챌린지 단건 상세 정보를 조회한다.")
        @Test
        void getChallenge() throws Exception {
                // given
                given(challengeService.getChallenge(any()))
                                .willReturn(me.bombom.api.v1.challenge.dto.GetChallengeDetailResponse.builder()
                                                .build());

                // when // then
                mockMvc.perform(get("/admin/api/v1/challenges/1"))
                                .andExpect(status().isOk());
        }

        @DisplayName("존재하지 않는 챌린지 상세 정보를 조회할 수 없다.")
        @Test
        void getChallengeNotFound() throws Exception {
                // given
                given(challengeService.getChallenge(any()))
                                .willThrow(new java.util.NoSuchElementException("Challenge not found"));

                // when // then
                mockMvc.perform(get("/admin/api/v1/challenges/1"))
                                .andExpect(status().isNotFound()); // Expecting 404, need to ensure exception handler
                                                                   // handles this
        }

        @DisplayName("챌린지 참여자 목록을 조회한다.")
        @Test
        void getChallengeParticipants() throws Exception {
                // given
                given(challengeService.getChallengeParticipants(any(), any(), any(Pageable.class)))
                                .willReturn(new PageImpl<>(List.of(GetChallengeParticipantResponse.builder().build())));

                // when // then
                mockMvc.perform(get("/admin/api/v1/challenges/1/participants")
                                .param("hasTeam", "true"))
                                .andExpect(status().isOk());
        }

        @DisplayName("팀이 없는 참여자 목록을 조회한다.")
        @Test
        void getChallengeParticipantsWithoutTeam() throws Exception {
                // given
                given(challengeService.getChallengeParticipants(any(), any(), any(Pageable.class)))
                                .willReturn(new PageImpl<>(List.of(GetChallengeParticipantResponse.builder().build())));

                // when // then
                mockMvc.perform(get("/admin/api/v1/challenges/1/participants")
                                .param("hasTeam", "false"))
                                .andExpect(status().isOk());
        }

        @DisplayName("특정 팀의 참여자 목록을 조회한다.")
        @Test
        void getChallengeParticipantsByTeam() throws Exception {
                // given
                given(challengeService.getChallengeParticipants(any(), any(), any(Pageable.class)))
                                .willReturn(new PageImpl<>(List.of(GetChallengeParticipantResponse.builder().build())));

                // when // then
                mockMvc.perform(get("/admin/api/v1/challenges/1/participants")
                                .param("challengeTeamId", "1")
                                .param("hasTeam", "true"))
                                .andExpect(status().isOk());
        }
}
