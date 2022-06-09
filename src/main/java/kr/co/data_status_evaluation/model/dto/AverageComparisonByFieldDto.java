package kr.co.data_status_evaluation.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AverageComparisonByFieldDto {
    Map<String, ScoreSummaryByCategoryDto> scoreByField;
    Map<String, ScoreSummaryByCategoryDto> scoreSummaryByCategory;
}
