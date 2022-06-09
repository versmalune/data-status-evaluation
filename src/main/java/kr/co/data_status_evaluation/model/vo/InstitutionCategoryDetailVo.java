package kr.co.data_status_evaluation.model.vo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class InstitutionCategoryDetailVo {

    private Long id;

    private Long institutionCategoryId;

    private String code;

    private String name;

    private String description;
}
