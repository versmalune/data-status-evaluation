package kr.co.data_status_evaluation.model.dto;

import kr.co.data_status_evaluation.model.EvaluationIndex;
import kr.co.data_status_evaluation.model.EvaluationIndexRate;
import kr.co.data_status_evaluation.model.EvaluationIndexRateDetail;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class AdminEvaluationIndexRateDto {

    String year;

    List<EvaluationIndexRateDetail> rateDetails = new ArrayList<>();

    List<EvaluationIndex> indices = new ArrayList<>();

    public AdminEvaluationIndexRateDto() {
        EvaluationIndexRateDetail detail1 = new EvaluationIndexRateDetail();
        detail1.setLevel(1);
        this.rateDetails.add(detail1);

        EvaluationIndexRateDetail detail2 = new EvaluationIndexRateDetail();
        detail2.setLevel(2);
        this.rateDetails.add(detail2);
    }

    public AdminEvaluationIndexRateDto(EvaluationIndexRate rate) {
        this.year = rate.getYear();
        this.rateDetails = rate.getDetails();
        this.indices = new ArrayList<>(rate.getIndices());
    }
}
