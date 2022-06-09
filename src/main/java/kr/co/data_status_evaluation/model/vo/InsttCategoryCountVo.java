package kr.co.data_status_evaluation.model.vo;

import kr.co.data_status_evaluation.util.StringUtils;
import lombok.*;

import java.math.BigInteger;

@Getter
@Setter
@NoArgsConstructor
@ToString
@AllArgsConstructor
public class InsttCategoryCountVo {

    private BigInteger id;

    private String code;
    private String name;

    private Integer count;

    public String getCode() {
        return StringUtils.isNullOrEmpty(this.code) ? "" : this.code.trim();
    }
}
