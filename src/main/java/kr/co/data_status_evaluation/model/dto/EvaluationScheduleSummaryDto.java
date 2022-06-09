package kr.co.data_status_evaluation.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationScheduleSummaryDto {

    private String year;

    private Long count; // 지표(index) Count

    private Long totalScore;

    private Long scheduleCount; // 일정(schedule) Count
}
