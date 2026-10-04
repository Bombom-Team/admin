package me.bombom.api.v1.newsletterrequest.scheduler;

import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.newsletterrequest.collector.NewsletterRequestCollector;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "newsletter-request.collector", name = "enabled", havingValue = "true")
public class NewsletterRequestCollectScheduler {

    private final NewsletterRequestCollector newsletterRequestCollector;

    @Scheduled(fixedDelayString = "${newsletter-request.collector.fixed-delay-millis:60000}")
    public void collectPending() {
        newsletterRequestCollector.collectPending();
    }
}
