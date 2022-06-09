package kr.co.data_status_evaluation.model.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import kr.co.data_status_evaluation.util.StringUtils;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Getter
@Setter
public class LmsInstitutionVo {

    private String insttCd;
    private String insttNm;
    private String tyClLarge;
    private String tyClMedium;
    private String tyClSmall;
    private Boolean mntnabYn;
    private String beforeInsttCd;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date registDt;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date updtDt;

    @JsonIgnore
    public boolean isValueEmpty() {
        return (StringUtils.isNullOrEmpty(this.insttCd) || StringUtils.isNullOrEmpty(this.insttNm) || this.mntnabYn == null);
    }

    @JsonIgnore
    public boolean isListInsttTyEmpty() {
        return (this.tyClLarge == null || this.tyClMedium == null
                || this.tyClLarge.equals("") || this.tyClMedium.equals(""));
    }

    @JsonIgnore
    public String getStatusInsttType() {
        if (!isListInsttTyEmpty() && this.tyClLarge.equals("1") && this.tyClMedium.equals("1"))
            return "CA";
        else if (this.tyClLarge != null && this.tyClLarge.equals("2")) {
            if (this.tyClMedium != null && this.tyClMedium.equals("1"))
                return "MC";
            else if (this.tyClMedium != null && this.tyClMedium.equals("2"))
                return "LG";
            else
                return null;
        }
        else if (this.tyClLarge != null && Integer.valueOf(this.tyClLarge) >= 7 && Integer.valueOf(this.tyClLarge) <= 9)
            return "GO";
        else
            return null;
    }
}
