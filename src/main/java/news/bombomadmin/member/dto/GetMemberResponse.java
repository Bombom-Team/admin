package news.bombomadmin.member.dto;

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
