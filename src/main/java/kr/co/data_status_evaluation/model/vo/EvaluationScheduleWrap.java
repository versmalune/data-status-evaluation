package kr.co.data_status_evaluation.model.vo;

import lombok.Getter;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class EvaluationScheduleWrap {

    @NotEmpty(message = "* 평가년도는 필수입니다.")
    private String year;

    @NotEmpty(message = "* 평가기간은 필수입니다.")
    private List<@Valid EvaluationScheduleVo> schedules = new ArrayList<>();
}
