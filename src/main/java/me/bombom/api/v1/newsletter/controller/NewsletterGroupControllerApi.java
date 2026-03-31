package me.bombom.api.v1.newsletter.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import me.bombom.api.v1.newsletter.dto.GetNewsletterGroupResponse;

@Tag(name = "NewsletterGroup", description = "뉴스레터 그룹 관리 API")
@ApiResponses({
        @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content)
})
public interface NewsletterGroupControllerApi {

    @Operation(summary = "뉴스레터 그룹 목록 조회", description = "전체 뉴스레터 그룹 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    List<GetNewsletterGroupResponse> getNewsletterGroups();
}
