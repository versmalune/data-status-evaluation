package kr.co.data_status_evaluation.repository;

import kr.co.data_status_evaluation.model.Answer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnswerRepository extends JpaRepository<Answer, Long> {
}
