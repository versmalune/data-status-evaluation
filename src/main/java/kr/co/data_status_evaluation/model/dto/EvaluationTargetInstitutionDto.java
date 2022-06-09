package kr.co.data_status_evaluation.model.dto;

import kr.co.data_status_evaluation.util.StringUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationTargetInstitutionDto {

    private Long id;

    private Long insttId;

    private String year;

    private String trgtInsttYn;

    // Excel Download용
    private String insttCtgyCd;
    private String insttCtgyNm;
    private String insttCd;
    private String insttNm;

    public EvaluationTargetInstitutionDto(Long id, Long insttId, String year, String trgtInsttYn) {
        this.id = id;
        this.insttId = insttId;
        this.year = year;
        this.trgtInsttYn = trgtInsttYn;
    }

    public EvaluationTargetInstitutionDto(Long insttId, String year, String trgtInsttYn) {
        this.insttId = insttId;
        this.year = year;
        this.trgtInsttYn = trgtInsttYn;
    }

    public EvaluationTargetInstitutionDto(String year, String categoryCd, String categoryNm, String insttCd, String insttNm, Character trgtInsttYn) {
        this.year = year;
        this.insttCtgyCd = categoryCd;
        this.insttCtgyNm = categoryNm;
        this.insttCd = insttCd;
        this.insttNm = insttNm;
        this.trgtInsttYn = !Objects.isNull(trgtInsttYn) ? trgtInsttYn.toString() : null;
    }

    public String getInsttCtgyCd() {
        return StringUtils.isNullOrEmpty(this.insttCtgyCd) ? "" : this.insttCtgyCd.trim();
    }
}
