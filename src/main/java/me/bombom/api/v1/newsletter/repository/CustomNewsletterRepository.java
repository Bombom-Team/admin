package me.bombom.api.v1.newsletter.repository;

import java.util.List;
import java.util.Optional;
import me.bombom.api.v1.newsletter.dto.GetNewsletterResponse;
import me.bombom.api.v1.newsletter.dto.GetNewsletterSummaryResponse;
import me.bombom.api.v1.newsletter.dto.GetNewslettersRequest;

public interface CustomNewsletterRepository {

    List<GetNewsletterSummaryResponse> findNewsletters(GetNewslettersRequest request);

    Optional<GetNewsletterResponse> findNewsletter(Long id);
}
