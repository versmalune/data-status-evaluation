package kr.co.data_status_evaluation.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import kr.co.data_status_evaluation.model.enums.EvaluationType;
import kr.co.data_status_evaluation.model.vo.EvaluationScoreVo;
import kr.co.data_status_evaluation.util.BooleanToYNConverter;
import kr.co.data_status_evaluation.util.StringUtils;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.crypto.Mac;
import javax.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

@Entity
@Table(name = "tb_rev_evl_scr")
@Getter
@Setter
public class EvaluationScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "idx_lvl")
    private Integer level;

    @Column(name = "idx_scr")
    private Float score;

    @CreationTimestamp
    @Column(name = "creat_dt")
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updt_dt")
    private Instant updatedAt;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idx_id")
    private EvaluationIndex evaluationIndex;

    @ManyToOne
    @JoinColumn(name = "instt_category_id")
    private InstitutionCategory institutionCategory;

    public EvaluationScore() {
    }

    public EvaluationScore(EvaluationScoreVo vo) {
        setLevel(vo.getLevel());
        setScore(vo.getScore());
    }

    public void setAll(EvaluationScoreVo vo) {
        setLevel(vo.getLevel());
        setScore(vo.getScore());
    }

    public String getScoreToString() {
        return this.score == null ? null : this.score.toString();
    }

    @Override
    public String toString() {
        String score = StringUtils.isNullOrEmpty(String.valueOf(this.score)) ? "0"
                : String.valueOf(BigDecimal.valueOf(this.score).setScale(2, RoundingMode.HALF_UP).floatValue());
        return this.level + "등급(" + score + "점)";
    }
}
