package kr.co.data_status_evaluation.model.dw;

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
@Table(name = "tb_rev_dw_fact_idx_result")
@Getter
@Setter
@NoArgsConstructor
public class IndexResultFact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "instt_id")
    private Long institutionId;

    @Column(name = "idx_id")
    private Long indexId;

    @Column(name = "fld_id")
    private Long fieldId;

    @Column(name = "evl_yr")
    private String year;

    @Column(name = "scr")
    private BigDecimal score = BigDecimal.ZERO;

    @CreationTimestamp
    @Column(updatable = false, name = "creat_dt")
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updt_dt")
    private Instant updatedAt;

    public IndexResultFact(Long institutionId, Long indexId, Long fieldId, String year) {
        this.institutionId = institutionId;
        this.indexId = indexId;
        this.fieldId = fieldId;
        this.year = year;
        this.score = BigDecimal.ZERO;
    }

    public Float getScore() {
        return Objects.isNull(this.score) ? null : this.score.floatValue();
    }

    public void setScore(Float score) {
        this.score = Objects.isNull(score) ? null : BigDecimal.valueOf(score);
    }
}
