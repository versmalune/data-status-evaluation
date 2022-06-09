package kr.co.data_status_evaluation.model.vo;

import kr.co.data_status_evaluation.model.EvaluationSchedule;
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

@Getter
@Setter
@NoArgsConstructor
public class EvaluationScheduleVo {
    private DateFormat df = new SimpleDateFormat("yyyy년 MM월 dd일");

    private EvaluationStatus name;

    @NotEmpty(message = "* 기간설정은 필수입니다.")
    private String dateRange;

    public EvaluationScheduleVo(EvaluationSchedule schedule) {
        this.name = schedule.getName();
        this.dateRange = schedule.getDateRange();
    }

    public Date getBeginDt() throws ParseException {
        if (!StringUtils.isNullOrEmpty(this.dateRange)) {
            String[] splitDates = this.dateRange.split(" - ");
            Date beginDt =  df.parse(splitDates[0]);
            return beginDt;
        }
        return null;
    }

    public Date getEndDt() throws ParseException {
        if (!StringUtils.isNullOrEmpty(this.dateRange)) {
            String[] splitDates = this.dateRange.split(" - ");
            Date endDt =  df.parse(splitDates[1]);
            return endDt;
        }
        return null;
    }
}
