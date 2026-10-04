package me.bombom.api.v1.newsletterrequest.domain;

import lombok.Builder;

/**
 * 초안에 채우는 뉴스레터 정보 묶음. 수집 결과와 운영자 수정 요청이 같이 쓴다.
 */
@Builder
public record DraftContent(
        String name,
        String description,
        String imageUrl,
        Long categoryId,
        String mainPageUrl,
        String subscribeUrl,
        String issueCycle,
        String sender,
        String subscribeMethod,
        String previousNewsletterUrl
) {
}
