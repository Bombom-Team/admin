package me.bombom.api.v1.nativenewsletter.maeilmail.repository;

import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailContentAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MaeilMailContentAnswerRepository extends JpaRepository<MaeilMailContentAnswer, Long>,
        CustomMaeilMailContentAnswerRepository {
}
