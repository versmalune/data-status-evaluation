package kr.co.data_status_evaluation.model.vo;

import kr.co.data_status_evaluation.util.StringUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AttachFileCategoryVo {

    private Long id; // category Id
    private String code;
    private String name;
    private boolean hasAttachFile; // 증빙자료 존재여부

    public String getCode() {
        return StringUtils.isNullOrEmpty(this.code) ? "" : this.code.trim();
    }
}
