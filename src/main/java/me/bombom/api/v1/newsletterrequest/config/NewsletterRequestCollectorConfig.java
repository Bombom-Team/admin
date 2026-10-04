package me.bombom.api.v1.newsletterrequest.config;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "newsletter-request.collector", name = "enabled", havingValue = "true")
public class NewsletterRequestCollectorConfig {

    @Bean
    public AnthropicClient newsletterRequestAnthropicClient(NewsletterRequestCollectProperties properties) {
        return AnthropicOkHttpClient.builder()
                .apiKey(properties.getAnthropicApiKey())
                .build();
    }
}
