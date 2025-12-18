package me.bombom.api.v1.member.dto;

import com.querydsl.core.annotations.QueryProjection;

public record GetMemberResponse(
        Long id,
        String nickname,
        String email,
        String role
) {

    @QueryProjection
    public GetMemberResponse {
    }
}
