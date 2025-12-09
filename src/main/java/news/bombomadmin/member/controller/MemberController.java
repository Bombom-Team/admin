package news.bombomadmin.member.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import news.bombomadmin.member.dto.UpdateRoleRequest;
import news.bombomadmin.member.service.MemberService;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/members")
public class MemberController {

    private final MemberService memberService;

    @PatchMapping("/{id}/role")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateRole(
            @PathVariable @Positive(message = "id는 1 이상이어야 합니다.") Long id,
            @RequestBody @Validated UpdateRoleRequest request
    ) {
        memberService.updateRole(id, request);
    }
}
