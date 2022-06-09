package kr.co.data_status_evaluation.repository.dw;

import kr.co.data_status_evaluation.model.Institution;
import kr.co.data_status_evaluation.model.dw.InstitutionResultFact;
import kr.co.data_status_evaluation.model.enums.EvaluationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InstitutionResultFactRepository extends JpaRepository<InstitutionResultFact, Long> {

    Optional<InstitutionResultFact> findByInstitutionIdAndYear(Long institutionId, String year);

    @Query(value = "SELECT ir.*\n" +
            "FROM tb_rev_dw_fact_instt_result ir\n" +
            "         LEFT OUTER JOIN tb_rev_evl_trgt_instt ti ON ir.instt_id = ti.instt_id\n" +
            "WHERE ti.trgt_instt_yn = 'Y'\n" +
            "  AND ti.evl_yr = :year\n" +
            "  AND ir.evl_yr = :year", nativeQuery = true)
    List<InstitutionResultFact> findAllByYear(String year);

    List<InstitutionResultFact> findAllByProcessStatusAndYear(EvaluationStatus status, String year);

    @Modifying
    @Query(value = "UPDATE  tb_rev_dw_fact_instt_result\n" +
            "SET     process_sttus_cd = :status\n" +
            "WHERE   evl_yr = :year\n" +
            "AND     instt_id IN (:idList)", nativeQuery = true)
    void bulkUpdateOfInstitutionResultProcessStatusByYearAndInsttIdList(String year, List<Long> idList, String status);

    void deleteAllByInstitutionIdAndYear(Long institutionId, String year);

    @Modifying
    @Query(value = "" +
            "UPDATE tb_rev_dw_fact_instt_result\n" +
            "SET instt_id = (:newInsttId)\n" +
            "WHERE instt_id = (:beforeInsttId)", nativeQuery = true)
    void updateByBeforeInstt(Long newInsttId, Long beforeInsttId);
}
