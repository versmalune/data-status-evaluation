package kr.co.data_status_evaluation.repository;

import kr.co.data_status_evaluation.model.EvaluationTargetInstitution;
import kr.co.data_status_evaluation.model.Institution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface EvaluationTargetInstitutionRepository extends JpaRepository<EvaluationTargetInstitution, Long>, JpaSpecificationExecutor<EvaluationTargetInstitution> {
    @Query(value = "SELECT  eti.*\n" +
                    "FROM   tb_rev_evl_trgt_instt eti\n" +
                    "       INNER JOIN  tb_rev_instt i\n" +
                    "           ON      eti.instt_id = i.id\n" +
                    "           AND     eti.evl_yr = :year\n" +
                    "           AND     i.instt_category_id = :insttCategoryId", nativeQuery = true)
    List<EvaluationTargetInstitution> findAllByYearCategoryId(String year, String insttCategoryId);

    List<EvaluationTargetInstitution> findAllByYear(String year);

    Optional<EvaluationTargetInstitution> findAllByYearAndInstitution(String year, Institution institution);
    
    @Modifying
    @Query(value = "" +
            "UPDATE tb_rev_evl_trgt_instt\n" +
            "SET instt_id = (:newInsttId)\n" +
            "WHERE instt_id = (:beforeInsttId)", nativeQuery = true)
    void updateByBeforeInstt(Long newInsttId, Long beforeInsttId);
}
