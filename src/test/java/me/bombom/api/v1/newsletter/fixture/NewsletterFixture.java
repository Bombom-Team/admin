package me.bombom.api.v1.newsletter.fixture;

import static org.instancio.Select.field;

import me.bombom.api.v1.newsletter.domain.Category;
import me.bombom.api.v1.newsletter.domain.Newsletter;
import me.bombom.api.v1.newsletter.domain.NewsletterDetail;
import me.bombom.api.v1.newsletter.domain.NewsletterPreviousPolicy;
import me.bombom.api.v1.newsletter.domain.NewsletterPreviousStrategy;
import me.bombom.api.v1.subscribe.domain.NewsletterSubscriptionCount;
import org.instancio.Instancio;

public class NewsletterFixture {

    public static Category createCategory(String name) {
        return Instancio.of(Category.class)
                .set(field(Category::getId), null)
                .set(field(Category::getName), name)
                .create();
    }

    public static NewsletterDetail createDetail(String issueCycle) {
        return Instancio.of(NewsletterDetail.class)
                .set(field(NewsletterDetail::getId), null)
                .set(field(NewsletterDetail::getMainPageUrl), "https://example.com")
                .set(field(NewsletterDetail::getSubscribeUrl), "https://subscribe.com")
                .set(field(NewsletterDetail::getIssueCycle), issueCycle)
                .set(field(NewsletterDetail::getSender), "sender")
                .set(field(NewsletterDetail::isPreviousAllowed), true)
                .create();
    }

    public static Newsletter createNewsletter(Long categoryId, Long detailId, String name, String email) {
        return Instancio.of(Newsletter.class)
                .set(field(Newsletter::getId), null)
                .set(field(Newsletter::getCategoryId), categoryId)
                .set(field(Newsletter::getDetailId), detailId)
                .set(field(Newsletter::getName), name)
                .set(field(Newsletter::getEmail), email)
                .set(field(Newsletter::getDescription), "description")
                .set(field(Newsletter::getImageUrl), "image.png")
                .create();
    }

    public static NewsletterPreviousPolicy createPreviousPolicy(Long newsletterId,
            NewsletterPreviousStrategy strategy) {
        return Instancio.of(NewsletterPreviousPolicy.class)
                .set(field(NewsletterPreviousPolicy::getId), null)
                .set(field(NewsletterPreviousPolicy::getNewsletterId), newsletterId)
                .set(field(NewsletterPreviousPolicy::getStrategy), strategy)
                .set(field(NewsletterPreviousPolicy::getFixedCount), 0)
                .set(field(NewsletterPreviousPolicy::getRecentCount), 0)
                .set(field(NewsletterPreviousPolicy::getExposureRatio), 0)
                .create();
    }

    public static NewsletterSubscriptionCount createSubscriptionCount(Long newsletterId, int total) {
        return Instancio.of(NewsletterSubscriptionCount.class)
                .set(field(NewsletterSubscriptionCount::getId), null)
                .set(field(NewsletterSubscriptionCount::getNewsletterId), newsletterId)
                .set(field(NewsletterSubscriptionCount::getTotal), total)
                .create();
    }
}
