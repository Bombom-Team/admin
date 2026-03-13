package me.bombom.api.v1.newsletter.repository;

import java.util.List;
import java.util.Optional;
import me.bombom.api.v1.newsletter.domain.Category;
import me.bombom.api.v1.newsletter.dto.GetCategoryResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByName(String name);

    @Query("""
        SELECT new me.bombom.api.v1.newsletter.dto.GetCategoryResponse(c.id, c.name)
        FROM Category c
        ORDER BY c.id ASC
    """)
    List<GetCategoryResponse> findAllAsResponse();
}
