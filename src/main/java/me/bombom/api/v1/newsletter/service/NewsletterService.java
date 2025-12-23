package me.bombom.api.v1.newsletter.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorContextKeys;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.newsletter.domain.Category;
import me.bombom.api.v1.newsletter.domain.Newsletter;
import me.bombom.api.v1.newsletter.domain.NewsletterDetail;
import me.bombom.api.v1.newsletter.domain.NewsletterPreviousPolicy;
import me.bombom.api.v1.newsletter.domain.NewsletterPreviousStrategy;
import me.bombom.api.v1.newsletter.dto.CreateNewsletterRequest;
import me.bombom.api.v1.newsletter.dto.GetNewsletterResponse;
import me.bombom.api.v1.newsletter.dto.GetNewsletterSummaryResponse;
import me.bombom.api.v1.newsletter.dto.GetNewslettersRequest;
import me.bombom.api.v1.newsletter.dto.UpdateNewsletterRequest;
import me.bombom.api.v1.newsletter.repository.CategoryRepository;
import me.bombom.api.v1.newsletter.repository.NewsletterDetailRepository;
import me.bombom.api.v1.newsletter.repository.NewsletterPreviousPolicyRepository;
import me.bombom.api.v1.newsletter.repository.NewsletterRepository;
import me.bombom.api.v1.newsletter.repository.NewsletterSubscriptionCountRepository;
import me.bombom.api.v1.subscribe.domain.NewsletterSubscriptionCount;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NewsletterService {

        private final NewsletterRepository newsletterRepository;
        private final NewsletterDetailRepository newsletterDetailRepository;
        private final NewsletterPreviousPolicyRepository newsletterPreviousPolicyRepository;
        private final NewsletterSubscriptionCountRepository newsletterSubscriptionCountRepository;
        private final CategoryRepository categoryRepository;

        @Transactional
        public void create(CreateNewsletterRequest request) {
                Category category = categoryRepository.findByName(request.category())
                                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                                                .addContext("category", request.category()));
                NewsletterDetail newsletterDetail = newsletterDetailRepository.save(request.toDetailEntity());

                Newsletter newsletter = newsletterRepository
                                .save(request.toNewsletterEntity(newsletterDetail.getId(), category.getId()));
                newsletterPreviousPolicyRepository.save(NewsletterPreviousPolicy.builder()
                                .newsletterId(newsletter.getId())
                                .strategy(NewsletterPreviousStrategy.INACTIVE)
                                .fixedCount(0)
                                .recentCount(0)
                                .exposureRatio(0)
                                .build());
                newsletterSubscriptionCountRepository.save(NewsletterSubscriptionCount.from(newsletter.getId()));
        }

        public List<GetNewsletterSummaryResponse> getNewsletters(GetNewslettersRequest request) {
                return newsletterRepository.findNewsletters(request);
        }

        public GetNewsletterResponse getNewsletterDetail(Long id) {
                return newsletterRepository.findNewsletter(id)
                                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                                                .addContext(ErrorContextKeys.ENTITY_TYPE, "newsletter")
                                                .addContext(ErrorContextKeys.OPERATION, "getNewsletter"));
        }

        @Transactional
        public void update(Long id, UpdateNewsletterRequest request) {
                Newsletter newsletter = findNewsletter(id);
                NewsletterDetail newsletterDetail = findNewsletterDetail(newsletter.getDetailId());

                Long categoryId = resolveCategoryId(request.category());

                newsletter.update(
                                request.name(),
                                request.description(),
                                request.imageUrl(),
                                request.email(),
                                categoryId
                );

                newsletterDetail.update(
                                request.mainPageUrl(),
                                request.subscribeUrl(),
                                request.issueCycle(),
                                request.sender(),
                                request.previousNewsletterUrl(),
                                request.previousAllowed(),
                                request.subscribeMethod()
                );
        }

        // TODO: 추후에 휴재, 폐간 처리로 해야할 듯
        @Transactional
        public void delete(Long id) {
                Newsletter newsletter = findNewsletter(id);

                newsletterSubscriptionCountRepository.deleteByNewsletterId(id);
                newsletterPreviousPolicyRepository.deleteByNewsletterId(id);
                newsletterRepository.delete(newsletter);
                newsletterDetailRepository.deleteById(newsletter.getDetailId());
        }

        private Newsletter findNewsletter(Long id) {
                return newsletterRepository.findById(id)
                                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                                                .addContext("newsletterId", id));
        }

        private NewsletterDetail findNewsletterDetail(Long id) {
                return newsletterDetailRepository.findById(id)
                                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                                                .addContext("detailId", id));
        }

        private Long resolveCategoryId(String categoryName) {
                if (categoryName == null) {
                        return null;
                }
                return categoryRepository.findByName(categoryName)
                                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                                                .addContext("category", categoryName))
                                .getId();
        }
}
