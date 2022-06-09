package kr.co.data_status_evaluation.model.vo;

import kr.co.data_status_evaluation.model.enums.EvaluationStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
public class InstitutionResultVo {

    private String name;

    private String category;

    private String year;

    private EvaluationStatus processStatus;

    private Float score;

    private Float extraScore;

    private Float standardScore;

    private Float improvementRate;

    private Timestamp updatedAt;

    private Integer ranking;

    private Long id;

    private String code;

    public Instant getUpdatedAt() {
        return this.updatedAt.toInstant();
    }

    public InstitutionResultVo(String category,
                               String name,
                               Float score,
                               Float extraScore,
                               Float standardScore,
                               String status,
                               Timestamp updatedAt,
                               Integer ranking,
                               Long id) {
        this.category = category;
        this.name = name;
        this.score = score;
        this.extraScore = extraScore;
        this.standardScore = standardScore;
        this.processStatus = EvaluationStatus.valueOf(status);
        this.updatedAt = updatedAt;
        this.ranking = ranking;
        this.id = id;
    }

    public InstitutionResultVo(String category,
                               String name,
                               Float score,
                               Float extraScore,
                               Float standardScore,
                               String status,
                               Timestamp updatedAt,
                               Integer ranking,
                               Long id,
                               String code) {
        this.category = category;
        this.name = name;
        this.score = score;
        this.extraScore = extraScore;
        this.standardScore = standardScore;
        this.processStatus = EvaluationStatus.valueOf(status);
        this.updatedAt = updatedAt;
        this.ranking = ranking;
        this.id = id;
        this.code = code;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InstitutionResultVo that = (InstitutionResultVo) o;
        return Objects.equals(name, that.name) && Objects.equals(category, that.category) && Objects.equals(year, that.year) && processStatus == that.processStatus && Objects.equals(score, that.score) && Objects.equals(extraScore, that.extraScore) && Objects.equals(standardScore, that.standardScore) && Objects.equals(updatedAt, that.updatedAt) && Objects.equals(ranking, that.ranking) && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, category, year, processStatus, score, extraScore, standardScore, updatedAt, ranking, id);
    }

    public BigDecimal getImprovementRate() {
        if (this.improvementRate.isInfinite()) {
            return null;
        }
        BigDecimal finalRate = new BigDecimal(this.improvementRate);
        return finalRate.setScale(2, RoundingMode.HALF_UP);
    }
}
