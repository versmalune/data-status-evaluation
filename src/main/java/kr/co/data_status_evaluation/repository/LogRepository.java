package kr.co.data_status_evaluation.repository;

import kr.co.data_status_evaluation.model.Log;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface LogRepository extends JpaRepository<Log, Long> {

    @Modifying
    @Query(value = "" +
            "UPDATE tb_rev_log\n" +
            "SET instt_cd = (:newInsttCd)\n" +
            "WHERE instt_cd = (:beforeInsttCd)", nativeQuery = true)
    void updateByBeforeInstt(String newInsttCd, String beforeInsttCd);
}
