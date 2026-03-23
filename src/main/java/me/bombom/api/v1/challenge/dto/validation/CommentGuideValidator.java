package me.bombom.api.v1.challenge.dto.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import me.bombom.api.v1.challenge.domain.DailyGuideType;

public class CommentGuideValidator implements ConstraintValidator<ValidCommentGuide, CommentGuideValidatable> {

    @Override
    public boolean isValid(CommentGuideValidatable value, ConstraintValidatorContext context) {
        if (value.type() != DailyGuideType.COMMENT) {
            return true;
        }

        if (value.notice() != null && !value.notice().isBlank()) {
            return true;
        }

        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate("type이 COMMENT인 경우 notice는 필수입니다.")
                .addPropertyNode("notice")
                .addConstraintViolation();
        return false;
    }
}
