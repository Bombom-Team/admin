package me.bombom.api.v1.nativenewsletter.maeilmail.service;

import lombok.RequiredArgsConstructor;
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

    public Page<GetMaeilMailContentAnswerResponse> getContentAnswers(
            GetMaeilMailContentAnswersRequest request,
            Pageable pageable
    ) {
        return contentAnswerRepository.getContentAnswers(request, pageable);
    }
}
