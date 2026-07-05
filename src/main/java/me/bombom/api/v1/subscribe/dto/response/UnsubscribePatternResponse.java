package me.bombom.api.v1.subscribe.dto.response;

import me.bombom.api.v1.subscribe.domain.UnsubscribePattern;

public record UnsubscribePatternResponse(

        Long id,
        String patternKey,
        String patternValue
) {

    public static UnsubscribePatternResponse from(UnsubscribePattern entity) {
        return new UnsubscribePatternResponse(
                entity.getId(),
                entity.getPatternKey(),
                entity.getPatternValue()
        );
    }
}
