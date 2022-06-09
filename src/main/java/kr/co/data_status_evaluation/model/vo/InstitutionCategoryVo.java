package kr.co.data_status_evaluation.model.vo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class InstitutionCategoryVo {

    private Long id;

    private String code;

    private String description;

    private List<InstitutionCategoryDetailVo> detailCategories = new ArrayList<>();

}
