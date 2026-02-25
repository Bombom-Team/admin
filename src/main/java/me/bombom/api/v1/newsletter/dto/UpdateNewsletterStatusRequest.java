package me.bombom.api.v1.newsletter.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import me.bombom.api.v1.newsletter.domain.NewsletterPublicationStatus;

public record UpdateNewsletterStatusRequest(
        @NotNull(message = "status는 비어 있을 수 없습니다.")
        NewsletterPublicationStatus status,

        LocalDate suspendedAt
) {}
