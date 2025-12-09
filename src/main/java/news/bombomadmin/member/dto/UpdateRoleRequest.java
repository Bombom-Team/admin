package news.bombomadmin.member.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateRoleRequest(

        @NotBlank(message = "authority는 비어 있을 수 없습니다.")
        String authority
) {
}
