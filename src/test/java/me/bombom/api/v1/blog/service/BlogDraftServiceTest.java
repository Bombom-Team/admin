package me.bombom.api.v1.blog.service;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import me.bombom.api.v1.blog.domain.BlogPost;
import me.bombom.api.v1.blog.domain.BlogPostStatus;
import me.bombom.api.v1.blog.domain.BlogVisibility;
import me.bombom.api.v1.blog.dto.CreateBlogDraftResponse;
import me.bombom.api.v1.blog.repository.BlogPostRepository;
import me.bombom.api.v1.common.config.QuerydslConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import({BlogDraftService.class, QuerydslConfig.class})
class BlogDraftServiceTest {

    @Autowired
    private BlogDraftService blogDraftService;

    @Autowired
    private BlogPostRepository blogPostRepository;

    @Test
    void 초안_생성_성공() {
        // when
        CreateBlogDraftResponse response = blogDraftService.createDraft(1L);

        // then
        BlogPost blogPost = blogPostRepository.findById(response.postId()).orElseThrow();
        assertSoftly(softly -> {
            softly.assertThat(blogPost.getMemberId()).isEqualTo(1L);
            softly.assertThat(blogPost.getTitle()).isNull();
            softly.assertThat(blogPost.getContent()).isNull();
            softly.assertThat(blogPost.getDescription()).isNull();
            softly.assertThat(blogPost.getThumbnailImageId()).isNull();
            softly.assertThat(blogPost.getCategoryId()).isNull();
            softly.assertThat(blogPost.getStatus()).isEqualTo(BlogPostStatus.DRAFT);
            softly.assertThat(blogPost.getVisibility()).isEqualTo(BlogVisibility.PRIVATE);
            softly.assertThat(blogPost.getPublishedAt()).isNull();
        });
    }
}
