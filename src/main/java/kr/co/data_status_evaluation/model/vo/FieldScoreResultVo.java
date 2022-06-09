package kr.co.data_status_evaluation.model.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FieldScoreResultVo {

    private String fieldName;

    private Float score;

    private Float standardScore;

    private Long institutionId;
}
