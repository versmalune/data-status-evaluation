package kr.co.data_status_evaluation.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import kr.co.data_status_evaluation.model.key.RelevantInstitutionKey;
import kr.co.data_status_evaluation.util.BooleanToYNConverter;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "tb_rev_evl_idx_rlvt_instt")
@Getter
@Setter
public class RelevantInstitution {

    @EmbeddedId
    private RelevantInstitutionKey id;

    @ManyToOne
    @MapsId("indexId")
    @JoinColumn(name = "evl_idx_id")
    @JsonIgnore
    private EvaluationIndex evaluationIndex;

    @ManyToOne(optional = false)
    @MapsId("categoryId")
    @JoinColumn(name = "instt_category_id")
    @JsonIgnore
    private InstitutionCategory institutionCategory;

    @Convert(converter = BooleanToYNConverter.class)
    @Column(name = "rlvt_instt_yn")
    private boolean yn;
}