package me.bombom.api.v1.challenge.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record AssignTeamsRequest(
        @Schema(description = "팀 당 최대 인원 수 (기본값: 15)", example = "15", defaultValue = "15") Integer maxTeamSize) {
    public AssignTeamsRequest {
        if (maxTeamSize == null) {
            maxTeamSize = 15;
        }
    }
}
