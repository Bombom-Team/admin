package me.bombom.api.v1.faq.service;

import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorContextKeys;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.faq.domain.Faq;
import me.bombom.api.v1.faq.dto.CreateFaqRequest;
import me.bombom.api.v1.faq.dto.GetFaqDetailResponse;
import me.bombom.api.v1.faq.dto.GetFaqResponse;
import me.bombom.api.v1.faq.dto.UpdateFaqRequest;
import me.bombom.api.v1.faq.repository.FaqRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FaqService {

    private final FaqRepository faqRepository;

    public Page<GetFaqResponse> getFaqs(Pageable pageable) {
        return faqRepository.findAll(pageable)
                .map(GetFaqResponse::from);
    }

    public GetFaqDetailResponse getFaq(Long id) {
        return GetFaqDetailResponse.from(getFaqById(id));
    }

    @Transactional
    public void createFaq(CreateFaqRequest request) {
        Faq faq = Faq.builder()
                .question(request.question())
                .answer(request.answer())
                .faqCategory(request.faqCategory())
                .build();
        faqRepository.save(faq);
    }

    @Transactional
    public void updateFaq(Long id, UpdateFaqRequest request) {
        Faq faq = getFaqById(id);
        faq.update(request.question(), request.answer(), request.faqCategory());
    }

    @Transactional
    public void deleteFaq(Long id) {
        faqRepository.delete(getFaqById(id));
    }

    private Faq getFaqById(Long id) {
        return faqRepository.findById(id)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext(ErrorContextKeys.ENTITY_TYPE, "faq"));
    }
}
