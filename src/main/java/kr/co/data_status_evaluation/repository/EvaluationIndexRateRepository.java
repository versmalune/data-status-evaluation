package kr.co.data_status_evaluation.repository;

import kr.co.data_status_evaluation.model.EvaluationIndexRate;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EvaluationIndexRateRepository extends JpaRepository<EvaluationIndexRate, Long> {

    List<EvaluationIndexRate> findAllByYear(String year, Sort sort);

    List<EvaluationIndexRate> findAllByYearOrderById(String year);
}
