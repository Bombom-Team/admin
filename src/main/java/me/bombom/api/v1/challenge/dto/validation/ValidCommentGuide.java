package me.bombom.api.v1.challenge.dto.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CommentGuideValidator.class)
public @interface ValidCommentGuide {

    String message() default "type이 COMMENT인 경우 notice는 필수입니다.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
