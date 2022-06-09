package kr.co.data_status_evaluation.model.search;

import kr.co.data_status_evaluation.model.enums.Author;
import lombok.Getter;
import lombok.Setter;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.StringJoiner;

@Getter
@Setter
public class AccountSearchParam {
    private Author type;
    private String userId;
    private String user;
    private String institution;

    public String getQueryParams() {
        StringJoiner sj = new StringJoiner("&");

        try {
            if (this.type != null) {
                sj.add(String.format("type=%s", URLEncoder.encode(this.type.name(), "UTF-8")));
            }
            if (this.userId != null && !this.userId.equals("")) {
                sj.add(String.format("userId=%s", URLEncoder.encode(this.userId, "UTF-8")));
            }
            if (this.user != null && !this.user.equals("")) {
                sj.add(String.format("user=%s", URLEncoder.encode(this.user, "UTF-8")));
            }
            if (this.institution != null && !this.institution.equals("")) {
                sj.add(String.format("institution=%s", URLEncoder.encode(this.institution, "UTF-8")));
            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return sj.toString();
    }
}
