package kr.co.data_status_evaluation.model.vo;

import kr.co.data_status_evaluation.model.EvaluationSchedule;
import kr.co.data_status_evaluation.model.File;
import kr.co.data_status_evaluation.model.Institution;
import kr.co.data_status_evaluation.model.enums.EvaluationStatus;
import kr.co.data_status_evaluation.util.StringUtils;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
public class EvaluationResultVo {
    private Long id;

    private Long insttId;

    private Long idxId;

    private String year;

    private String score;

    private EvaluationStatus processStatus;

    Set<File> Files;
}
