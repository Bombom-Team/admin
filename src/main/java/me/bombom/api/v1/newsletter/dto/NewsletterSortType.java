package me.bombom.api.v1.newsletter.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NewsletterSortType {

    LATEST("최신순"),
    POPULAR("인기순");

    private final String description;
}
