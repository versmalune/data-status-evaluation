package kr.co.data_status_evaluation.model.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class InstitutionCategoryDto {

    private Long id;

    private String code;

    private String name;

    private String description;

    private List<InstitutionCategoryDetailDto> detailCategories = new ArrayList<>();

}
