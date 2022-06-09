package kr.co.data_status_evaluation.model.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class IndexScoreResultVo {

    private String category;

    private String institution;

    private String field;

    private String index;

    private Float score;

    private Integer ranking;
}
