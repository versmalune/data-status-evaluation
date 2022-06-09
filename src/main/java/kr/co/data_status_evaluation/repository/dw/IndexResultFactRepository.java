package kr.co.data_status_evaluation.repository.dw;

import kr.co.data_status_evaluation.model.dw.IndexResultFact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IndexResultFactRepository extends JpaRepository<IndexResultFact, Long> {

    Optional<IndexResultFact> findByInstitutionIdAndIndexIdAndYear(Long institutionId, Long indexId, String year);

    List<IndexResultFact> findAllByYear(String year);

    @Query(value = "SELECT * FROM tb_rev_dw_fact_idx_result WHERE instt_id IN :ids AND evl_yr = :year", nativeQuery = true)
    List<IndexResultFact> findAllByInstitutionIdAndYear(List<Long> ids, String year);

    void deleteAllByIndexId(Long indexId);

    void deleteAllByInstitutionIdAndYear(Long institutionId, String year);

    @Modifying
    @Query(value = "" +
            "UPDATE tb_rev_dw_fact_idx_result\n" +
            "SET instt_id = (:newInsttId)\n" +
            "WHERE instt_id = (:beforeInsttId)", nativeQuery = true)
    void updateByBeforeInstt(Long newInsttId, Long beforeInsttId);
}
