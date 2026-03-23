package me.bombom.api.v1.challenge.dto.validation;

import me.bombom.api.v1.challenge.domain.DailyGuideType;

public interface CommentGuideValidatable {

    DailyGuideType type();

    String notice();
}
