package kr.co.data_status_evaluation.model.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
public class LmsNoticeDto {
    private Long id;
    private String sj;
    private Long viewCount;
    private String createdAt;
    private String updatedAt;
    private boolean noticeYn;
}
