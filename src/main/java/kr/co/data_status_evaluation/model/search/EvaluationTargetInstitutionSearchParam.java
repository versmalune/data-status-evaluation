package kr.co.data_status_evaluation.model.search;

import kr.co.data_status_evaluation.util.StringUtils;
import lombok.Getter;
import lombok.Setter;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.StringJoiner;

@Getter
@Setter
public class EvaluationTargetInstitutionSearchParam {
    private String categoryId;
    private String year;
    private String institutionCode;
    private String institutionNm;
    private String trgtYn;

    public String getQueryParams() {
        StringJoiner sj = new StringJoiner("&");

        try {
            if (!StringUtils.isNullOrEmpty(this.year)) {
                sj.add(String.format("year=%s", URLEncoder.encode(this.year, "UTF-8")));
            }
            if (!StringUtils.isNullOrEmpty(this.institutionCode)) {
                sj.add(String.format("institutionCode=%s", URLEncoder.encode(this.institutionCode, "UTF-8")));
            }
            if (!StringUtils.isNullOrEmpty(this.institutionNm)) {
                sj.add(String.format("institutionNm=%s", URLEncoder.encode(this.institutionNm, "UTF-8")));
            }
            if (!StringUtils.isNullOrEmpty(this.trgtYn)) {
                sj.add(String.format("trgtYn=%s", URLEncoder.encode(this.trgtYn, "UTF-8")));
            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return sj.toString();
    }
}
