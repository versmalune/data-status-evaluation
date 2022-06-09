package kr.co.data_status_evaluation.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import kr.co.data_status_evaluation.model.key.EvalutationResultTotalKey;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "tb_rev_evl_result_total")
@Getter
@Setter
public class EvaluationResultTotal {

    @EmbeddedId
    private EvalutationResultTotalKey id;

    @ManyToOne
    @MapsId("institutionId")
    @JoinColumn(name = "instt_id")
    @JsonIgnore
    private Institution institution;

    @ManyToOne
    @MapsId("fieldId")
    @JoinColumn(name = "fld_id")
    @JsonIgnore
    private EvaluationField evaluationField;

    @Column(name = "tot_rvw")
    private String review;

    @Column(name = "evl_yr", columnDefinition = "CHAR")
    private String year;

}
