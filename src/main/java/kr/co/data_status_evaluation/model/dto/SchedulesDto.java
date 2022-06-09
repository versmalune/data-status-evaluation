package kr.co.data_status_evaluation.model.dto;

import kr.co.data_status_evaluation.model.EvaluationSchedule;
import kr.co.data_status_evaluation.model.enums.EvaluationStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Optional;

@Getter
@NoArgsConstructor
public class SchedulesDto {

    private List<EvaluationSchedule> schedules;

    public SchedulesDto(List<EvaluationSchedule> schedules) {
        this.schedules = schedules;
    }

    public EvaluationSchedule getSchedule(EvaluationStatus status) {
        return this.schedules.stream()
                .filter(schedule -> schedule.getName().equals(status))
                .findFirst().orElse(null);
    }
}
