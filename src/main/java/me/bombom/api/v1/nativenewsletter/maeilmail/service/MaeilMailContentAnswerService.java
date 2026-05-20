package me.bombom.api.v1.nativenewsletter.maeilmail.service;

import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.GetMaeilMailContentAnswerDetailResponse;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.GetMaeilMailContentAnswerResponse;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.GetMaeilMailContentAnswersRequest;
import me.bombom.api.v1.nativenewsletter.maeilmail.repository.MaeilMailContentAnswerRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MaeilMailContentAnswerService {

    private final MaeilMailContentAnswerRepository contentAnswerRepository;
    private final MaeilMailContentRepository contentRepository;
    private final MaeilMailTopicRepository topicRepository;

    public Page<GetMaeilMailContentAnswerResponse> getContentAnswers(
            GetMaeilMailContentAnswersRequest request,
            Pageable pageable
    ) {
        return contentAnswerRepository.getContentAnswers(request, pageable);
    }

    public GetMaeilMailContentAnswerDetailResponse getContentAnswer(Long id) {
        return contentAnswerRepository.findDetailById(id)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext("maeilMailContentAnswerId", id));
    }

    @Transactional
    public void createContentAnswer(CreateMaeilMailContentAnswerRequest request) {
        MaeilMailTopic topic = topicRepository.findByTrack(request.track())
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext("track", request.track()));

        MaeilMailContent content = contentRepository.save(
                MaeilMailContent.builder()
                .topicId(topic.getId())
                .title(request.title())
                .content(request.content())
                .contentsText(request.contentsText())
                .contentsSummary(request.contentsSummary())
                .expectedReadTime(request.expectedReadTime())
                .build());

        contentAnswerRepository.save(
                MaeilMailContentAnswer.builder()
                .contentId(content.getId())
                .answer(request.answer())
                .build()
        );
    }
}
