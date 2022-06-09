package kr.co.data_status_evaluation.model.vo;

import kr.co.data_status_evaluation.model.EvaluationIndex;
import kr.co.data_status_evaluation.model.EvaluationScore;
import kr.co.data_status_evaluation.model.RelevantInstitution;
import kr.co.data_status_evaluation.model.enums.EvaluationType;
import kr.co.data_status_evaluation.util.StringUtils;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
public class EvaluationIndexVo {
    private DateFormat df = new SimpleDateFormat("yyyy년 MM월 dd일");

    private Long id;

    private String year;

    @NotNull(message = "* 평가영역은 필수입니다.")
    private int fieldNo;

//    @NotNull(message = "* 평가유형은 필수입니다.")
    private EvaluationType type = EvaluationType.QUALITATIVE;

    @NotBlank(message = "* 지표번호는 공백일 수 없습니다.")
    private String no;

    @NotBlank(message = "* 지표명은 필수입니다.")
    private String name;

    @NotBlank(message = "* 질문항목은 필수입니다.")
    private String question;

    private String description;

    private String fileDescription;

    private boolean scorePublic;

    private boolean performanceInput;

    private boolean objectionPossible;

    private boolean attachFile = true;

    private boolean naLevel = false;

    @NotEmpty(message = "* 기간설정은 필수입니다.")
    private String dateRange;

    private List<@Valid EvaluationScoreVo> scores;

    private List<RelevantInstitution> relevantInstitutions;

    private MultipartFile formFile;

    private String[] deleteMaterialIds;

    public Date getBeginDt() throws ParseException {
        if (!StringUtils.isNullOrEmpty(this.dateRange)) {
            String[] splitDates = this.dateRange.split(" - ");
            Date beginDt = df.parse(splitDates[0]);
            return beginDt;
        }
        return null;
    }

    public Date getEndDt() throws ParseException {
        if (!StringUtils.isNullOrEmpty(this.dateRange)) {
            String[] splitDates = this.dateRange.split(" - ");
            Date endDt = df.parse(splitDates[1]);
            return endDt;
        }
        return null;
    }

    public EvaluationScoreVo getScore(int level) {
        return this.scores.stream().filter(el -> Objects.equals(el.getLevel(), level)).findAny().orElse(null);
    }

    public void setDateRange(Date beginDate, Date endDate) {
        if (beginDate == null || endDate == null) {
            this.setDateRange("");
            return;
        }
        StringBuffer sb = new StringBuffer();
        sb.append(df.format(beginDate));
        sb.append(" - ");
        sb.append(df.format(endDate));

        this.setDateRange(sb.toString());
    }

    public boolean hasDeletedMaterialIds() {
        return (this.deleteMaterialIds != null && this.deleteMaterialIds.length > 0);
    }

}
