package me.bombom.api.v1.flyway.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import me.bombom.api.v1.flyway.domain.WorkKind;

/**
 * 작업중(예약) 등록 요청. 백엔드가 flyway-wip 이슈로 생성한다.
 */
public record CreateWipIssueRequest(
        @NotNull WorkKind workKind,
        String targetTable,
        @NotBlank String plannedVersion,
        @NotBlank String description,
        @NotBlank String assignee
) {
}
