package kr.co.data_status_evaluation.repository;

import kr.co.data_status_evaluation.model.EvaluationIndex;
import kr.co.data_status_evaluation.model.EvaluationResult;
import kr.co.data_status_evaluation.model.Institution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EvaluationResultRepository extends JpaRepository<EvaluationResult, Long>, JpaSpecificationExecutor<EvaluationResult> {
    @Query(value = "SELECT DISTINCT er.year FROM EvaluationResult er " +
            "WHERE er.institution = :institution ORDER BY er.year DESC")
    List<String> findYearsByInstt(Institution institution);

    List<EvaluationResult> findAllByInstitutionAndYear(Institution institution, String year);

    @Query("SELECT r FROM EvaluationResult r LEFT JOIN FETCH r.evaluationIndex LEFT JOIN FETCH r.institution WHERE r.year = :year")
    List<EvaluationResult> findAllByYear(String year);

    @Query(value = "SELECT * FROM tb_rev_evl_result r\n" +
            "         LEFT OUTER JOIN tb_rev_evl_trgt_instt ti ON r.instt_id = ti.instt_id\n" +
            "LEFT OUTER JOIN tb_rev_evl_idx i ON r.idx_id = i.id\n"+
            "WHERE ti.trgt_instt_yn = 'Y'\n" +
            "AND i.rao_posbl_yn = 'Y'\n"+
            "  AND ti.evl_yr = :year\n" +
            "  AND r.evl_yr = :year", nativeQuery = true)
    List<EvaluationResult> findAllByYearAndRaoPossible(String year);

    EvaluationResult findByInstitutionAndYearAndEvaluationIndex(Institution institution, String year, EvaluationIndex evaluationIndex);

    @Modifying
    @Query(value = "UPDATE  tb_rev_evl_result\n" +
            "SET     process_sttus_cd = :status\n" +
            "WHERE   evl_yr = :year\n" +
            "AND     instt_id IN (:idList)", nativeQuery = true)
    void bulkUpdateOfInstitutionResultProcessStatusByYearAndInsttIdList(String year, List<Long> idList, String status);

    @Modifying
    @Query(value = "DELETE\n" +
                "FROM    tb_rev_evl_result\n" +
                "WHERE   instt_id = :institutionId\n" +
                "AND     evl_yr = :year", nativeQuery = true)
    void deleteAllByInstitutionIdAndYear(Long institutionId, String year);

    @Modifying
    @Query(value = "" +
            "UPDATE tb_rev_evl_result\n" +
            "SET instt_id = (:newInsttId)\n" +
            "WHERE instt_id = (:beforeInsttId)", nativeQuery = true)
    void updateByBeforeInstt(Long newInsttId, Long beforeInsttId);

}
