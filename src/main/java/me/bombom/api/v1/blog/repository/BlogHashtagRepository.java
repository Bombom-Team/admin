package me.bombom.api.v1.blog.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import me.bombom.api.v1.blog.domain.BlogHashtag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlogHashtagRepository extends JpaRepository<BlogHashtag, Long> {

    List<BlogHashtag> findAllByNameIn(Collection<String> names);

    Optional<BlogHashtag> findByName(String name);
}
