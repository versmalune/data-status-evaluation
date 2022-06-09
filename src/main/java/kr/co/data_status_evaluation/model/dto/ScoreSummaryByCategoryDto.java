package kr.co.data_status_evaluation.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Objects;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ScoreSummaryByCategoryDto {

    private Float sum;

    private Float min;

    private Float max;

    private Double avg;

    private Integer count;

    private BigDecimal fieldScore;

    public Double getRate() {
        return Objects.isNull(this.fieldScore) ? null : this.fieldScore.doubleValue();
    }

    public void setRate(Double fieldScore) {
        this.fieldScore = Objects.isNull(fieldScore) ? null : BigDecimal.valueOf(fieldScore);
    }
}
