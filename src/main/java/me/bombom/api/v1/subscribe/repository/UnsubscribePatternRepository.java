package me.bombom.api.v1.subscribe.repository;

import java.util.List;
import me.bombom.api.v1.subscribe.domain.UnsubscribePattern;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UnsubscribePatternRepository extends JpaRepository<UnsubscribePattern, Long> {

    List<UnsubscribePattern> findByPatternKeyNotLike(String patternKeyPattern);

    List<UnsubscribePattern> findByPatternKeyStartingWith(String patternKeyPrefix);
}
