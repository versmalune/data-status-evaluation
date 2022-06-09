package kr.co.data_status_evaluation.model.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import kr.co.data_status_evaluation.model.enums.Author;
import kr.co.data_status_evaluation.util.StringUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@ToString
public class LmsAccountVo {

    private String mberId;
    private String mberSttus;
    private String mberAuthor;
    private String password;
    private String insttCd;
    private String chargerNm;
    private String chargerEmail;
    private String chargerTelnoEnc;
    private String chargerDeptNm;

    @JsonIgnore
    public Author getStatusAuthor() {
        if (this.mberAuthor.equals("MBAU01") || this.mberAuthor.equals("MBAU99"))
            return Author.ADMIN;
        else if (this.mberAuthor.equals("MBAU06") || this.mberAuthor.equals("MBAU07"))
            return Author.INSTITUTION;
        else
            return null;
    }

    @JsonIgnore
    public boolean isValueEmpty() {
        return (StringUtils.isNullOrEmpty(this.mberId) || StringUtils.isNullOrEmpty(this.mberSttus) ||
                StringUtils.isNullOrEmpty(this.mberAuthor) || StringUtils.isNullOrEmpty(this.password) ||
                StringUtils.isNullOrEmpty(this.insttCd) || StringUtils.isNullOrEmpty(this.chargerNm) ||
                StringUtils.isNullOrEmpty(this.chargerEmail));
    }
}
