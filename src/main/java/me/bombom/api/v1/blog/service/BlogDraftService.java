package me.bombom.api.v1.blog.service;

import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.blog.domain.BlogPost;
import me.bombom.api.v1.blog.domain.BlogPostStatus;
import me.bombom.api.v1.blog.domain.BlogVisibility;
import me.bombom.api.v1.blog.dto.CreateBlogDraftResponse;
import me.bombom.api.v1.blog.repository.BlogPostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BlogDraftService {

    private final BlogPostRepository blogPostRepository;

    @Transactional
    public CreateBlogDraftResponse createDraft(Long memberId) {
        BlogPost blogPost = blogPostRepository.save(BlogPost.builder()
                .memberId(memberId)
                .status(BlogPostStatus.DRAFT)
                .visibility(BlogVisibility.PRIVATE)
                .build());

        return CreateBlogDraftResponse.from(blogPost);
    }
}
