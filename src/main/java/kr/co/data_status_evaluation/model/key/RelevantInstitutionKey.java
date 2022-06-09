package kr.co.data_status_evaluation.model.key;

import kr.co.data_status_evaluation.model.EvaluationIndex;
import kr.co.data_status_evaluation.model.InstitutionCategory;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class RelevantInstitutionKey implements Serializable {

    @Column(name = "evl_idx_id")
    private Long indexId;

    @Column(name = "instt_category_id")
    private Long categoryId;

    public RelevantInstitutionKey(EvaluationIndex index, InstitutionCategory category) {
        this.indexId = index.getId();
        this.categoryId = category.getId();
    }
}
