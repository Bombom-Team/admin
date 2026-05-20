package me.bombom.api.v1.nativenewsletter.maeilmail.repository;

import java.util.Optional;
import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailTopic;
import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailTrack;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MaeilMailTopicRepository extends JpaRepository<MaeilMailTopic, Long> {

    Optional<MaeilMailTopic> findByTrack(MaeilMailTrack track);
}
