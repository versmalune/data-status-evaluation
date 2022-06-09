package kr.co.data_status_evaluation.model.search;

import kr.co.data_status_evaluation.model.enums.EvaluationType;
import kr.co.data_status_evaluation.util.StringUtils;
import lombok.Getter;
import lombok.Setter;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.StringJoiner;

@Getter
@Setter
public class EvaluationIndexSearchParam {

    private String year;

    private EvaluationType type;

    private String institutionName;

    private String institutionType;

    public String getQueryParams() {
        StringJoiner sj = new StringJoiner("&");

        try {
            if (!StringUtils.isNullOrEmpty(this.year)) {
                sj.add(String.format("year=%s", URLEncoder.encode(this.year, "UTF-8")));
            }

            if (type != null) {
                sj.add(String.format("type=%s", URLEncoder.encode(this.type.getTitle(), "UTF-8")));
            }

            if (!StringUtils.isNullOrEmpty(this.institutionName)) {
                sj.add(String.format("institutionName=%s", URLEncoder.encode(this.institutionName, "UTF-8")));
            }

            if (!StringUtils.isNullOrEmpty(this.institutionType)) {
                sj.add(String.format("institutionType=%s", URLEncoder.encode(this.institutionType, "UTF-8")));
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return sj.toString();
    }
}