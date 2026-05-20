package me.bombom.api.v1.nativenewsletter.maeilmail.repository;

import java.util.Optional;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.GetMaeilMailContentAnswerDetailResponse;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.GetMaeilMailContentAnswerResponse;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.GetMaeilMailContentAnswersRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomMaeilMailContentAnswerRepository {

    Page<GetMaeilMailContentAnswerResponse> getContentAnswers(GetMaeilMailContentAnswersRequest request, Pageable pageable);

    Optional<GetMaeilMailContentAnswerDetailResponse> findDetailById(Long id);
}
