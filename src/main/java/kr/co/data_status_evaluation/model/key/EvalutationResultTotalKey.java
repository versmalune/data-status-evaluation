package kr.co.data_status_evaluation.model.key;

import kr.co.data_status_evaluation.model.EvaluationField;
import kr.co.data_status_evaluation.model.Institution;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class EvalutationResultTotalKey implements Serializable {

    @Column(name = "instt_id")
    private Long institutionId;

    @Column(name = "fld_id")
    private Long fieldId;

    public EvalutationResultTotalKey(Institution institution, EvaluationField field) {
        this.institutionId = institution.getId();
        this.fieldId = field.getId();
    }
}
