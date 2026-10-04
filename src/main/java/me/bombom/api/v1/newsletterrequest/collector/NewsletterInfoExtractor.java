package me.bombom.api.v1.newsletterrequest.collector;

import java.util.List;

public interface NewsletterInfoExtractor {

    ExtractedNewsletterInfo extract(
            String requestedName,
            FetchedNewsletterPage page,
            List<String> categoryNames
    );
}
