package kr.co.data_status_evaluation.model.search;

import kr.co.data_status_evaluation.util.StringUtils;
import lombok.Getter;
import lombok.Setter;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.StringJoiner;

@Getter
@Setter
public class StatisticSearchParam implements SearchParam {
    private String type;
    private String index;
    private String year;
    private String field;
    private String tab;

    public String getQueryParams() {
        StringJoiner sj = new StringJoiner("&");

        try {
            if (!StringUtils.isNullOrEmpty(this.type)) {
                sj.add(String.format("type=%s", URLEncoder.encode(this.type, "UTF-8")));
            }

            if (!StringUtils.isNullOrEmpty(this.index)) {
                sj.add(String.format("index=%s", URLEncoder.encode(this.index, "UTF-8")));
            }

            if (!StringUtils.isNullOrEmpty(this.year)) {
                sj.add(String.format("year=%s", URLEncoder.encode(this.year, "UTF-8")));
            }

            if (!StringUtils.isNullOrEmpty(this.field)) {
                sj.add(String.format("field=%s", URLEncoder.encode(this.field, "UTF-8")));
            }

            if (!StringUtils.isNullOrEmpty(this.tab)) {
                sj.add(String.format("tab=%s", URLEncoder.encode(this.tab, "UTF-8")));
            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return sj.toString();
    }

    public String getYear() {
        return StringUtils.isNullOrEmpty(this.year) ? "" : this.year;
    }
}
