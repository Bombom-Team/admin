package me.bombom.api.v1.blog.service;

import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.blog.domain.BlogImageAsset;
import me.bombom.api.v1.blog.domain.BlogImageAssetStatus;
import me.bombom.api.v1.blog.domain.BlogPost;
import me.bombom.api.v1.blog.domain.BlogPostStatus;
import me.bombom.api.v1.blog.dto.UploadBlogDraftImageResponse;
import me.bombom.api.v1.blog.repository.BlogImageAssetRepository;
import me.bombom.api.v1.blog.repository.BlogPostRepository;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorContextKeys;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.file.dto.StoredFile;
import me.bombom.api.v1.file.service.S3FileService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BlogImageService {

    private static final String BLOG_IMAGE_PREFIX = "blog/posts";

    private final BlogPostRepository blogPostRepository;
    private final BlogImageAssetRepository blogImageAssetRepository;
    private final S3FileService s3FileService;

    @Value("${spring.cloud.aws.s3.blog-bucket}")
    private String blogBucketName;

    @Transactional
    public UploadBlogDraftImageResponse uploadPostImage(
            Long memberId,
            Long postId,
            MultipartFile imageFile
    ) {
        BlogPost blogPost = getPost(postId);
        validateOwner(blogPost, memberId);
        validateEditableStatus(blogPost);
        validateImageFile(imageFile);

        StoredFile storedFile = s3FileService.uploadToBucketWithMetadata(imageFile, blogBucketName, BLOG_IMAGE_PREFIX);

        try {
            BlogImageAssetStatus imageStatus = resolveImageStatus(blogPost);
            BlogImageAsset blogImageAsset = blogImageAssetRepository.save(BlogImageAsset.builder()
                    .blogPostId(postId)
                    .objectKey(storedFile.objectKey())
                    .imageUrl(storedFile.fileUrl())
                    .status(imageStatus)
                    .build());

            return UploadBlogDraftImageResponse.from(blogImageAsset);
        } catch (RuntimeException e) {
            // TODO: S3 delete 지원이 추가되면 업로드된 objectKey 보상 삭제 수행
            throw e;
        }
    }

    private BlogPost getPost(Long postId) {
        return blogPostRepository.findById(postId)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext(ErrorContextKeys.ENTITY_TYPE, "blogPost")
                        .addContext(ErrorContextKeys.OPERATION, "uploadPostImage"));
    }

    private void validateOwner(BlogPost blogPost, Long memberId) {
        if (blogPost.getMemberId().equals(memberId)) {
            return;
        }

        throw new CIllegalArgumentException(ErrorDetail.FORBIDDEN_RESOURCE)
                .addContext(ErrorContextKeys.ENTITY_TYPE, "blogPost")
                .addContext(ErrorContextKeys.OPERATION, "uploadPostImage");
    }

    private void validateEditableStatus(BlogPost blogPost) {
        BlogPostStatus status = blogPost.getStatus();
        if (status == BlogPostStatus.DRAFT || status == BlogPostStatus.PUBLISHED) {
            return;
        }

        throw new CIllegalArgumentException(ErrorDetail.RESOURCE_CONFLICT)
                .addContext(ErrorContextKeys.ENTITY_TYPE, "blogPost")
                .addContext(ErrorContextKeys.OPERATION, "uploadPostImage");
    }

    private void validateImageFile(MultipartFile imageFile) {
        if (imageFile == null || imageFile.isEmpty()) {
            throw new CIllegalArgumentException(ErrorDetail.INVALID_INPUT_VALUE)
                    .addContext("field", "imageFile");
        }
    }

    private BlogImageAssetStatus resolveImageStatus(BlogPost blogPost) {
        if (blogPost.getStatus() == BlogPostStatus.PUBLISHED) {
            return BlogImageAssetStatus.ATTACHED;
        }

        return BlogImageAssetStatus.UPLOADED;
    }
}
