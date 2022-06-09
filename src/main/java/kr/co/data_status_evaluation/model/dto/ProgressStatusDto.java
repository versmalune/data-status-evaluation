package kr.co.data_status_evaluation.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProgressStatusDto {
    String status;
    Integer count;
}
