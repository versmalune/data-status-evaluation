package kr.co.data_status_evaluation.model;

import kr.co.data_status_evaluation.model.enums.StatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class RestMessage {
    private StatusEnum statusEnum;
    private String message;
    private Object data;
}
