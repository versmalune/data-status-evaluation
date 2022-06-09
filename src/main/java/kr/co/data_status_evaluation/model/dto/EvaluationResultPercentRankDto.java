package kr.co.data_status_evaluation.model.dto;

import kr.co.data_status_evaluation.model.enums.EvaluationStatus;
import kr.co.data_status_evaluation.model.enums.EvaluationType;
import lombok.*;

import java.math.BigInteger;
import java.text.DecimalFormat;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class EvaluationResultPercentRankDto {

    private BigInteger indexId;

    private BigInteger institutionId;

    private String institutionCode;

    private String institutionName;

    private Float score;

    private Double percentRank;

    private Integer rank;

    private EvaluationType type;

    public String getPercentRankToString() {
        return String.format("%.2f", this.percentRank);
    }
}
