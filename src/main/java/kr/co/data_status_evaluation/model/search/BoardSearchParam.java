package kr.co.data_status_evaluation.model.search;

import kr.co.data_status_evaluation.model.enums.*;
import kr.co.data_status_evaluation.util.StringUtils;
import lombok.Getter;
import lombok.Setter;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.StringJoiner;

@Getter
@Setter
public class BoardSearchParam {
    private BoardType boardType;
    private QnaStatus qnaStatus;
    private QnaSearchType qnaSearchType;
    private SearchType searchType;
    private String searchWord;
    private Long institutionId;
    private boolean isHidden = true;

    public String getQuery() {
        StringJoiner stringJoiner = new StringJoiner("&");
        try {
            if (this.qnaStatus != null) {
                stringJoiner.add(String.format("qnaStatus=%s", this.qnaStatus));
            }
            if (this.qnaSearchType != null) {
                stringJoiner.add(String.format("qnaSearchType=%s", this.qnaSearchType));
            }
            if (this.searchType != null) {
                stringJoiner.add(String.format("searchType=%s", this.searchType));
            }
            if (!StringUtils.isNullOrEmpty(this.searchWord)) {
                stringJoiner.add(String.format("searchWord=%s", URLEncoder.encode(this.searchWord, "UTF-8")));
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return stringJoiner.toString();
    }
}
