package kr.co.data_status_evaluation.model.key;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class InstitutionDetailKey implements Serializable {

    private Long institution;

    private String year;

    public InstitutionDetailKey() {
    }

    public InstitutionDetailKey(Long institution, String year) {
        this.institution = institution;
        this.year = year;
    }
}
