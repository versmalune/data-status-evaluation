package kr.co.data_status_evaluation.model.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminEvaluationDto {
    Long resultId;
    String opinion;
    String objectionReview;
    Float score;
}
