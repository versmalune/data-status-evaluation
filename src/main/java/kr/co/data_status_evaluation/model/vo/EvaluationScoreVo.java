package kr.co.data_status_evaluation.model.vo;

import kr.co.data_status_evaluation.model.InstitutionCategory;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

@Getter
@Setter
@ToString
public class EvaluationScoreVo {

    private InstitutionCategory category;

    private Integer level;

    @NotNull(message = "* 점수설정은 필수입니다.")
    private Float score;

    public Float getScore() {
        return Objects.isNull(this.score) ? null : BigDecimal.valueOf(this.score).setScale(2, RoundingMode.HALF_UP).floatValue();
    }
}
