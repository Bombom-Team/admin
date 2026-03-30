package me.bombom.api.v1.blog.dto;

import jakarta.validation.constraints.Size;
import java.util.List;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorDetail;

public record UpdateBlogDraftRequest(

        @Size(max = 200)
        String title,

        String content,

        @Size(max = 500)
        String description,

        Long categoryId,
        List<String> hashTags,
        List<Long> referencedImageIds
) {

    private static final int MAX_CONTENT_LENGTH = 16_777_215;
    private static final int MAX_HASHTAG_LENGTH = 50;

    public void validate() {
        validateContentLength();
        validateHashTags();
        validateReferencedImageIds();
    }

    public List<String> normalizedHashTags() {
        if (hashTags == null) {
            return List.of();
        }

        return hashTags.stream()
                .map(this::trim)
                .filter(this::isNotBlank)
                .distinct()
                .toList();
    }

    public List<Long> distinctReferencedImageIds() {
        if (referencedImageIds == null) {
            return List.of();
        }

        return referencedImageIds.stream()
                .distinct()
                .toList();
    }

    private void validateContentLength() {
        if (content == null) {
            return;
        }

        if (content.length() <= MAX_CONTENT_LENGTH) {
            return;
        }

        throw invalidInput("content");
    }

    private void validateHashTags() {
        if (hashTags == null) {
            return;
        }

        boolean hasNullTag = hashTags.stream()
                .anyMatch(this::isNullTag);

        if (hasNullTag) {
            throw invalidInput("hashTags");
        }

        boolean hasTooLongTag = normalizedHashTags().stream()
                .anyMatch(this::isTooLongTag);

        if (hasTooLongTag) {
            throw invalidInput("hashTags");
        }
    }

    private void validateReferencedImageIds() {
        if (referencedImageIds == null) {
            return;
        }

        boolean hasNullImageId = referencedImageIds.stream()
                .anyMatch(this::isNullImageId);

        if (hasNullImageId) {
            throw invalidInput("referencedImageIds");
        }
    }

    private String trim(String value) {
        return value.trim();
    }

    private boolean isNotBlank(String value) {
        return !value.isBlank();
    }

    private boolean isNullTag(String tag) {
        return tag == null;
    }

    private boolean isTooLongTag(String tag) {
        return tag.length() > MAX_HASHTAG_LENGTH;
    }

    private boolean isNullImageId(Long imageId) {
        return imageId == null;
    }

    private CIllegalArgumentException invalidInput(String field) {
        return new CIllegalArgumentException(ErrorDetail.INVALID_INPUT_VALUE)
                .addContext("field", field);
    }
}
