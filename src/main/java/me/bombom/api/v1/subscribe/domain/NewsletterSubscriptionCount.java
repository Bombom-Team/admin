package me.bombom.api.v1.subscribe.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NewsletterSubscriptionCount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    Long newsletterId;

    @Column(nullable = false)
    private int total;

    @Column(nullable = false)
    private int age0s;

    @Column(nullable = false)
    private int age10s;

    @Column(nullable = false)
    private int age20s;

    @Column(nullable = false)
    private int age30s;

    @Column(nullable = false)
    private int age40s;

    @Column(nullable = false)
    private int age50s;

    @Column(nullable = false)
    private int age60plus;

    @Builder
    public NewsletterSubscriptionCount(
            Long id,
            @NonNull Long newsletterId,
            int total,
            int age0s,
            int age10s,
            int age20s,
            int age30s,
            int age40s,
            int age50s,
            int age60plus) {
        this.id = id;
        this.newsletterId = newsletterId;
        this.total = total;
        this.age0s = age0s;
        this.age10s = age10s;
        this.age20s = age20s;
        this.age30s = age30s;
        this.age40s = age40s;
        this.age50s = age50s;
        this.age60plus = age60plus;
    }

    public static NewsletterSubscriptionCount from(Long newsletterId) {
        return NewsletterSubscriptionCount.builder()
                .newsletterId(newsletterId)
                .total(0)
                .age0s(0)
                .age10s(0)
                .age20s(0)
                .age30s(0)
                .age40s(0)
                .age50s(0)
                .age60plus(0)
                .build();
    }
}
