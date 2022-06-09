package kr.co.data_status_evaluation.model.search;

import kr.co.data_status_evaluation.util.StringUtils;
import lombok.Getter;
import lombok.Setter;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.Instant;
import java.util.Locale;
import java.util.StringJoiner;

@Getter
@Setter
public class InstitutionSearchParam implements SearchParam {
    private String year;
    private String type;
    private String code;
    private String name;

    // 자체실적 미등록 기관 조회용
    private String trgtInsttYn;
    private Instant stdDt;
    private String status;

    public String getQueryParams() {
        StringJoiner sj = new StringJoiner("&");

        try {
            if (!StringUtils.isNullOrEmpty(this.year)) {
                sj.add(String.format("year=%s", URLEncoder.encode(this.year, "UTF-8")));
            }

            if (!StringUtils.isNullOrEmpty(this.type)) {
                sj.add(String.format("type=%s", URLEncoder.encode(this.type, "UTF-8")));
            }

            if (!StringUtils.isNullOrEmpty(this.code)) {
                sj.add(String.format("code=%s", URLEncoder.encode(this.code, "UTF-8")));
            }

            if (!StringUtils.isNullOrEmpty(this.name)) {
                sj.add(String.format("name=%s", URLEncoder.encode(this.name, "UTF-8")));
            }

            // 자체실적 미등록 기관 조회용
            if (!StringUtils.isNullOrEmpty(this.trgtInsttYn)) {
                sj.add(String.format("trgtInsttYn=%s", URLEncoder.encode(this.trgtInsttYn, "UTF-8")));
            }
            if (!StringUtils.isNullOrEmpty(this.status)) {
                sj.add(String.format("status=%s", URLEncoder.encode(this.status, "UTF-8")));
            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return sj.toString();
    }

    public String getYear() {
        return StringUtils.isNullOrEmpty(this.year) ? "" : this.year;
    }

    public String getCode() {
        if (!StringUtils.isNullOrEmpty(this.code)) {
            return this.code.toUpperCase(Locale.ROOT);
        }
        return this.code;
    }
}
