package me.bombom.api.v1.newsletterrequest.collector;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.util.List;

/**
 * LLM이 뉴스레터 페이지에서 뽑아낸 정보. 필드 설명은 그대로 JSON 스키마 설명이 된다.
 */
public record ExtractedNewsletterInfo(

        @JsonPropertyDescription("뉴스레터 이름. 페이지에 표기된 공식 이름")
        String name,

        @JsonPropertyDescription("봄봄 사용자에게 보여줄 소개. 존댓말 2~3문장, 120자 이내. 광고 문구는 빼고 어떤 내용을 받는지 설명")
        String description,

        @JsonPropertyDescription("주어진 카테고리 목록 중 가장 알맞은 이름 하나를 그대로. 고를 수 없으면 null")
        String categoryName,

        @JsonPropertyDescription("발행 주기. 예: 매주 월요일, 평일 매일, 격주 금요일. 알 수 없으면 null")
        String issueCycle,

        @JsonPropertyDescription("발행인 또는 발행처 이름. 알 수 없으면 null")
        String sender,

        @JsonPropertyDescription("뉴스레터 소개 홈페이지 URL")
        String mainPageUrl,

        @JsonPropertyDescription("구독 신청 페이지 URL. 홈페이지에서 바로 구독할 수 있으면 홈페이지 URL")
        String subscribeUrl,

        @JsonPropertyDescription("구독 방법 한 줄 설명. 예: 이메일 입력 후 구독하기 버튼. 알 수 없으면 null")
        String subscribeMethod,

        @JsonPropertyDescription("지난 호를 모아 볼 수 있는 아카이브 URL. 없으면 null")
        String previousNewsletterUrl,

        @JsonPropertyDescription("썸네일로 쓸 대표 이미지 URL(로고 우선). 페이지에 있는 절대 URL만. 없으면 null")
        String imageUrl
) {

    /**
     * LLM이 목록 밖의 카테고리를 답하면 버린다.
     */
    public String categoryNameIn(List<String> categoryNames) {
        if (categoryName == null) {
            return null;
        }
        String trimmed = categoryName.strip();
        return categoryNames.stream()
                .filter(trimmed::equals)
                .findFirst()
                .orElse(null);
    }
}
