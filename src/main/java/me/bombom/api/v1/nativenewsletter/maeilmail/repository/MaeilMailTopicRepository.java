package me.bombom.api.v1.nativenewsletter.maeilmail.repository;

import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailTopic;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MaeilMailTopicRepository extends JpaRepository<MaeilMailTopic, Long> {
}
