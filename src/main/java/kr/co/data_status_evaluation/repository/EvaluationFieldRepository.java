package kr.co.data_status_evaluation.repository;

import kr.co.data_status_evaluation.model.EvaluationField;
import kr.co.data_status_evaluation.model.dto.EvaluationScheduleSummaryDto;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EvaluationFieldRepository extends JpaRepository<EvaluationField, Long> {
    Optional<EvaluationField> findByNoAndYear(int no, String year);

    @Query(value = "SELECT new kr.co.data_status_evaluation.model.dto.EvaluationScheduleSummaryDto(ef.year, count(ei), " +
            "(SELECT SUM(fld.score) FROM EvaluationField fld WHERE fld.year = ef.year), " +
            "(SELECT COUNT(es) FROM EvaluationSchedule es WHERE es.year = ef.year)) " +
            "FROM EvaluationField ef " +
            "   JOIN EvaluationIndex ei " +
            "       ON ef = ei.evaluationField " +
            "GROUP BY ef.year " +
            "ORDER BY ef.year DESC")
    List<EvaluationScheduleSummaryDto> findAllSummary();

    List<EvaluationField> findAllByYear(String year);

    List<EvaluationField> findAllByYear(String year, Sort sort);

    @Query(value = "SELECT DISTINCT ef.year " +
            "FROM EvaluationField ef " +
            "ORDER BY ef.year DESC")
    List<String> findYears();


    @Query(value = "SELECT COUNT(ef) FROM EvaluationField ef " +
            "WHERE (ef.year = :year AND ef.no = :no) OR (ef.year = :year AND ef.name = :name)")
    int countAllByYearAndNoAndName(String year, int no, String name);
}
