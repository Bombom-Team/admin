package me.bombom.api.v1.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import me.bombom.api.v1.member.dto.GetMemberResponse;
import me.bombom.api.v1.member.dto.MembersOptionsRequest;
import me.bombom.api.v1.member.dto.UpdateRoleRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Member", description = "회원 관리 API")
@ApiResponses({
                @ApiResponse(responseCode = "401", description = "인증 실패 (로그인 필요)", content = @Content),
                @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content)
})
public interface MemberControllerApi {

        @Operation(summary = "회원 목록 조회", description = "검색 조건에 맞는 회원 목록을 페이징하여 조회합니다.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "회원 목록 조회 성공"),
                        @ApiResponse(responseCode = "400", description = "잘못된 요청 값", content = @Content)
        })
        Page<GetMemberResponse> getMembers(
                        @Parameter(description = "검색 옵션 (role: [ADMIN, USER], name: 이름 검색)") @Valid @ModelAttribute MembersOptionsRequest query,
                        @Parameter(description = "페이징 관련 요청 (예: ?page=0&size=20)") Pageable pageable);

        @Operation(summary = "회원 권한 수정", description = "특정 회원의 권한(Role)을 수정합니다.")
        @ApiResponses({
                        @ApiResponse(responseCode = "204", description = "회원 권한 수정 성공"),
                        @ApiResponse(responseCode = "400", description = "잘못된 요청 값", content = @Content),
                        @ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음", content = @Content)
        })
        void updateRole(
                        @Parameter(description = "회원 ID", example = "1") @PathVariable @Positive(message = "id는 1 이상이어야 합니다.") Long id,
                        @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "authority: [ADMIN, USER] 중 하나 선택") @Valid @RequestBody UpdateRoleRequest request);
}
