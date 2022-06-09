package kr.co.data_status_evaluation.model.vo;

import kr.co.data_status_evaluation.model.EvaluationResult;
import kr.co.data_status_evaluation.model.EvaluationResultTotal;
import lombok.Getter;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class EvaluationResultWrap {

    private List<EvaluationResult> evaluationResultList = new ArrayList<>();

    private List<EvaluationResultTotal> evaluationResultTotals = new ArrayList<>();
}
