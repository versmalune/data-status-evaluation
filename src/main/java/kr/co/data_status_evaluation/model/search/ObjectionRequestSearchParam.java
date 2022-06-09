package kr.co.data_status_evaluation.model.search;

import lombok.Getter;
import lombok.Setter;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.StringJoiner;

@Getter
@Setter
public class ObjectionRequestSearchParam {
    private String year;
    private String type;
    private String insttNm;
    private String indexName;
    private String objection;
    private String status;

    public String getQueryParams() {
        StringJoiner sj = new StringJoiner("&");

        try {
            if (this.year != null && !this.year.equals("")) {
                sj.add(String.format("year=%s", URLEncoder.encode(this.year, "UTF-8")));
            }

            if (this.type != null && !this.type.equals("")) {
                sj.add(String.format("type=%s", URLEncoder.encode(this.type, "UTF-8")));
            }

            if (this.insttNm != null && !this.insttNm.equals("")) {
                sj.add(String.format("insttNm=%s", URLEncoder.encode(this.insttNm, "UTF-8")));
            }

            if (this.indexName != null && !this.indexName.equals("")) {
                sj.add(String.format("indexName=%s", URLEncoder.encode(this.indexName, "UTF-8")));
            }

            if (this.objection != null && !this.objection.equals("")) {
                sj.add(String.format("objection=%s", URLEncoder.encode(this.objection, "UTF-8")));
            }

            if (this.status != null && !this.status.equals("")) {
                sj.add(String.format("status=%s", URLEncoder.encode(this.status, "UTF-8")));
            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return sj.toString();
    }
}
