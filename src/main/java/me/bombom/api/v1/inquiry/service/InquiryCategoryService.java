package me.bombom.api.v1.inquiry.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorContextKeys;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.inquiry.domain.InquiryCategory;
import me.bombom.api.v1.inquiry.dto.request.CreateInquiryCategoryRequest;
import me.bombom.api.v1.inquiry.dto.request.UpdateInquiryCategoryRequest;
import me.bombom.api.v1.inquiry.dto.response.InquiryCategoryResponse;
import me.bombom.api.v1.inquiry.repository.InquiryCategoryRepository;
import me.bombom.api.v1.inquiry.repository.InquiryRoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InquiryCategoryService {

    private final InquiryCategoryRepository inquiryCategoryRepository;
    private final InquiryRoomRepository inquiryRoomRepository;

    public List<InquiryCategoryResponse> getCategories() {
        return inquiryCategoryRepository.findAllByOrderByIdAsc().stream()
                .map(InquiryCategoryResponse::from)
                .toList();
    }

    @Transactional
    public void createCategory(CreateInquiryCategoryRequest request) {
        validateNameNotDuplicated(request.name());
        inquiryCategoryRepository.save(InquiryCategory.create(request.name()));
    }

    @Transactional
    public void updateCategory(Long id, UpdateInquiryCategoryRequest request) {
        InquiryCategory category = getCategoryById(id);
        validateNameNotDuplicatedForUpdate(request.name(), id);
        category.update(request.name());
    }

    @Transactional
    public void deleteCategory(Long id) {
        InquiryCategory category = getCategoryById(id);
        if (inquiryRoomRepository.existsByCategoryId(id)) {
            throw new CIllegalArgumentException(ErrorDetail.INQUIRY_CATEGORY_IN_USE)
                    .addContext(ErrorContextKeys.ENTITY_TYPE, "inquiryCategory");
        }
        inquiryCategoryRepository.delete(category);
    }

    private void validateNameNotDuplicated(String name) {
        if (inquiryCategoryRepository.existsByName(name)) {
            throw new CIllegalArgumentException(ErrorDetail.DUPLICATED_DATA)
                    .addContext(ErrorContextKeys.ENTITY_TYPE, "inquiryCategory");
        }
    }

    private void validateNameNotDuplicatedForUpdate(String name, Long id) {
        if (inquiryCategoryRepository.existsByNameAndIdNot(name, id)) {
            throw new CIllegalArgumentException(ErrorDetail.DUPLICATED_DATA)
                    .addContext(ErrorContextKeys.ENTITY_TYPE, "inquiryCategory");
        }
    }

    private InquiryCategory getCategoryById(Long id) {
        return inquiryCategoryRepository.findById(id)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext(ErrorContextKeys.ENTITY_TYPE, "inquiryCategory"));
    }
}
