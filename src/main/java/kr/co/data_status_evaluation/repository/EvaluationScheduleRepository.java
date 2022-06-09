package kr.co.data_status_evaluation.repository;

import kr.co.data_status_evaluation.model.EvaluationSchedule;
import kr.co.data_status_evaluation.model.enums.EvaluationStatus;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface EvaluationScheduleRepository extends JpaRepository<EvaluationSchedule, Long> {
    EvaluationSchedule findByYearAndName(String year, EvaluationStatus name);
    List<EvaluationSchedule> findAllByYear(String year);
    List<EvaluationSchedule> findAllByYear(String year, Sort sort);

    @Query(value = "SELECT *\n" +
            "FROM tb_rev_evl_schd\n" +
            "WHERE evl_yr = (SELECT evl_yr FROM tb_rev_evl_schd GROUP BY evl_yr ORDER BY evl_yr DESC LIMIT 1)", nativeQuery = true)
    List<EvaluationSchedule> findAllByLastYear();

    @Query(value = "SELECT  * \n" +
                    "FROM   tb_rev_evl_schd \n" +
                    "WHERE  begin_dt <= :now1 \n" +
                    "AND    :now2 < ADDDATE(end_dt, 1) \n" +
                    "ORDER BY evl_yr desc \n" +
                    "LIMIT  1", nativeQuery = true)
    EvaluationSchedule findOneScheduleByNow(Instant now1, Instant now2);
}
