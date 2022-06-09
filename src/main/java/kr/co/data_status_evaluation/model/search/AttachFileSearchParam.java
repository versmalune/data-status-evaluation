package kr.co.data_status_evaluation.model.search;

import kr.co.data_status_evaluation.util.StringUtils;
import lombok.Getter;
import lombok.Setter;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.StringJoiner;

@Getter
@Setter
public class AttachFileSearchParam implements SearchParam {
    private String tab;
    private String year;

    private String field; // 지표 탭
    private String indexNm; // 지표 탭
//    private String attachFile; // 지표 탭

    public String getQueryParams() {
        StringJoiner sj = new StringJoiner("&");

        try {
            if (!StringUtils.isNullOrEmpty(this.tab)) {
                sj.add(String.format("tab=%s", URLEncoder.encode(this.tab, "UTF-8")));
            }

            if (!StringUtils.isNullOrEmpty(this.year)) {
                sj.add(String.format("year=%s", URLEncoder.encode(this.year, "UTF-8")));
            }

            if (!StringUtils.isNullOrEmpty(this.field)) {
                sj.add(String.format("field=%s", URLEncoder.encode(this.field, "UTF-8")));
            }

            if (!StringUtils.isNullOrEmpty(this.indexNm)) {
                sj.add(String.format("indexNm=%s", URLEncoder.encode(this.indexNm, "UTF-8")));
            }

//            if (!StringUtils.isNullOrEmpty(this.attachFile)) {
//                sj.add(String.format("attachFile=%s", URLEncoder.encode(this.attachFile, "UTF-8")));
//            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return sj.toString();
    }
}
