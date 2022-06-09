package kr.co.data_status_evaluation.repository;

import kr.co.data_status_evaluation.model.EvaluationResultTotal;
import kr.co.data_status_evaluation.model.key.EvalutationResultTotalKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface EvaluationResultTotalRepository extends JpaRepository<EvaluationResultTotal, EvalutationResultTotalKey> {

    @Modifying
    @Query(value = "" +
            "UPDATE tb_rev_evl_result_total\n" +
            "SET instt_id = (:newInsttId)\n" +
            "WHERE instt_id = (:beforeInsttId)", nativeQuery = true)
    void updateByBeforeInstt(Long newInsttId, Long beforeInsttId);
}
