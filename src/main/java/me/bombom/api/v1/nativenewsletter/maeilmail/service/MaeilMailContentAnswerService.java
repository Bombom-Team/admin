package me.bombom.api.v1.nativenewsletter.maeilmail.service;

import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailContent;
import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailContentAnswer;
import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailTrack;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.CreateMaeilMailContentAnswerRequest;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.GetMaeilMailContentAnswerDetailResponse;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.GetMaeilMailContentAnswerResponse;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.GetMaeilMailContentAnswersRequest;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.UpdateMaeilMailContentAnswerRequest;
import me.bombom.api.v1.nativenewsletter.maeilmail.repository.MaeilMailContentAnswerRepository;
import me.bombom.api.v1.nativenewsletter.maeilmail.repository.MaeilMailContentRepository;
import me.bombom.api.v1.nativenewsletter.maeilmail.repository.MaeilMailTopicRepository;
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
        Long topicId = resolveTopicId(request.track());
        MaeilMailContent content = contentRepository.save(
                MaeilMailContent.builder()
                .topicId(topicId)
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

    @Transactional
    public void updateContentAnswer(Long id, UpdateMaeilMailContentAnswerRequest request) {
        MaeilMailContentAnswer answer = contentAnswerRepository.findById(id)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext("maeilMailContentAnswerId", id));

        Long newTopicId = resolveTopicId(request.track());
        MaeilMailContent content = contentRepository.findById(answer.getContentId())
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext("contentId", answer.getContentId()));
        content.update(newTopicId, request.title(), request.content(), request.contentsText(),
                request.contentsSummary(), request.expectedReadTime());

        if (request.answer() != null) {
            answer.update(request.answer());
        }
    }

    private Long resolveTopicId(MaeilMailTrack track) {
        if (track == null) {
            return null;
        }
        return topicRepository.findByTrack(track)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext("track", track))
                .getId();
    }

    @Transactional
    public void deleteContentAnswer(Long id) {
        if (!contentAnswerRepository.existsById(id)) {
            throw new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                    .addContext("maeilMailContentAnswerId", id);
        }
        contentAnswerRepository.deleteById(id);
    }
}
