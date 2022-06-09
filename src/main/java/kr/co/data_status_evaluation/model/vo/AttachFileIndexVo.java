package kr.co.data_status_evaluation.model.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AttachFileIndexVo {

    private Long id; // index Id
    private String fieldNm;
    private String evlType;
    private String indexNo;
    private String indexNm;
    private String attachFile;
    private boolean hasAttachFile;
}
