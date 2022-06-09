package kr.co.data_status_evaluation.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.StringJoiner;

@Data
@AllArgsConstructor
public class EvaluationIndexDto {

    private Long id;
    private String no;
    private String name;

    public String getFullName() {
        StringJoiner sj = new StringJoiner(". ");
        sj.add(this.getNo());
        sj.add(this.getName());
        return sj.toString();
    }
}
