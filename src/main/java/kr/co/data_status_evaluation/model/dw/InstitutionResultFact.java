package kr.co.data_status_evaluation.model.dw;

import kr.co.data_status_evaluation.model.enums.EvaluationStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "tb_rev_dw_fact_instt_result")
@Getter
@Setter
@NoArgsConstructor
public class InstitutionResultFact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "instt_id")
    private Long institutionId;

    @Column(name = "evl_yr")
    private String year;

    @Column(name = "scr")
    private BigDecimal score;

    @Column(name = "ext_scr")
    private BigDecimal extraScore;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "process_sttus_cd")
    private EvaluationStatus processStatus;

    @CreationTimestamp
    @Column(updatable = false, name = "creat_dt")
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updt_dt")
    private Instant updatedAt;

    public InstitutionResultFact(Long institutionId, String year) {
        this.institutionId = institutionId;
        this.year = year;
        this.score = BigDecimal.ZERO;
        this.extraScore = BigDecimal.ZERO;
        this.processStatus = EvaluationStatus.NONE;
    }

    public Float getScore() {
        return Objects.isNull(this.score) ? null : this.score.floatValue();
    }

    public void setScore(Float score) {
        this.score = Objects.isNull(score) ? null : BigDecimal.valueOf(score);
    }

    public Float getExtraScore() {
        return Objects.isNull(this.extraScore) ? null : this.extraScore.floatValue();
    }

    public void setExtraScore(Float extraScore) {
        this.extraScore = Objects.isNull(extraScore) ? null : BigDecimal.valueOf(extraScore);
    }
}
