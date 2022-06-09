package kr.co.data_status_evaluation.repository;

import kr.co.data_status_evaluation.model.EvaluationScore;
import kr.co.data_status_evaluation.model.InstitutionCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EvaluationScoreRepository extends JpaRepository<EvaluationScore, Long> {
    void deleteAllByInstitutionCategory(InstitutionCategory categoryId);

    Optional<EvaluationScore> findByEvaluationIndexIdAndInstitutionCategoryIdAndLevel(Long indexId, Long categoryId, Integer level);
}
