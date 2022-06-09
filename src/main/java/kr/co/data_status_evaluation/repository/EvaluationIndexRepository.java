package kr.co.data_status_evaluation.repository;

import kr.co.data_status_evaluation.model.EvaluationIndex;
import kr.co.data_status_evaluation.model.enums.EvaluationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EvaluationIndexRepository extends JpaRepository<EvaluationIndex, Long>, JpaSpecificationExecutor<EvaluationIndex> {
    List<EvaluationIndex> findAllByType(EvaluationType type);
}
