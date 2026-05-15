package me.bombom.api.v1.subscribe.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorContextKeys;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.subscribe.domain.UnsubscribePattern;
import me.bombom.api.v1.subscribe.dto.request.UnsubscribePatternRequest;
import me.bombom.api.v1.subscribe.dto.request.UnsubscribePatternUpdateRequest;
import me.bombom.api.v1.subscribe.dto.request.UnsubscribePatternType;
import me.bombom.api.v1.subscribe.dto.response.UnsubscribePatternResponse;
import me.bombom.api.v1.subscribe.repository.UnsubscribePatternRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UnsubscribePatternService {

    private static final String PARSE_PATTERN_KEY_PREFIX = "parse.";
    private static final String PARSE_PATTERN_KEY_PATTERN = "parse.%";

    private final UnsubscribePatternRepository unsubscribePatternRepository;

    @Transactional
    public void createUnsubscribePattern(UnsubscribePatternRequest request) {
        UnsubscribePattern pattern = UnsubscribePattern.builder()
                .patternKey(request.patternKey())
                .patternValue(request.patternValue())
                .build();
        unsubscribePatternRepository.save(pattern);
    }

    public List<UnsubscribePatternResponse> getUnsubscribePatterns(UnsubscribePatternType patternType) {
        if (patternType.isParse()) {
            return getParseUnsubscribePatterns();
        }

        return unsubscribePatternRepository.findByPatternKeyNotLike(PARSE_PATTERN_KEY_PATTERN)
                .stream()
                .map(UnsubscribePatternResponse::from)
                .toList();
    }

    private List<UnsubscribePatternResponse> getParseUnsubscribePatterns() {
        return unsubscribePatternRepository.findByPatternKeyStartingWith(PARSE_PATTERN_KEY_PREFIX)
                .stream()
                .map(UnsubscribePatternResponse::from)
                .toList();
    }

    public UnsubscribePatternResponse getUnsubscribePattern(Long id) {
        return unsubscribePatternRepository.findById(id)
                .map(UnsubscribePatternResponse::from)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext(ErrorContextKeys.ENTITY_TYPE, "unsubscribePattern"));
    }

    @Transactional
    public void updateUnsubscribePattern(Long id, UnsubscribePatternUpdateRequest request) {
        UnsubscribePattern pattern = unsubscribePatternRepository.findById(id)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext(ErrorContextKeys.ENTITY_TYPE, "unsubscribePattern"));
        pattern.update(request.patternValue());
    }
}
