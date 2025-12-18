package me.bombom.api.v1.member.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.member.dto.GetMemberResponse;
import me.bombom.api.v1.member.dto.MembersOptionsRequest;
import me.bombom.api.v1.member.dto.UpdateRoleRequest;
import me.bombom.api.v1.member.service.MemberService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/api/v1/members")
public class MemberController implements MemberControllerApi {

    private final MemberService memberService;

    @Override
    @GetMapping
    public Page<GetMemberResponse> getMembers(
            @Valid @ModelAttribute MembersOptionsRequest query,
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return memberService.getMembers(query, pageable);
    }

    @Override
    @PatchMapping("/{id}/role")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateRole(
            @PathVariable @Positive(message = "id는 1 이상이어야 합니다.") Long id,
            @RequestBody @Validated UpdateRoleRequest request) {
        memberService.updateRole(id, request);
    }
}
