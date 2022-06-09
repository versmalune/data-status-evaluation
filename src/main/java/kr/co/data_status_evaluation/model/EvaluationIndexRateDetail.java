package kr.co.data_status_evaluation.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "tb_rev_evl_idx_rate_details")
@Getter
@Setter
@NoArgsConstructor
public class EvaluationIndexRateDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "lvl")
    private Integer level;

    @Column(name = "rate")
    private BigDecimal rate;

    @ManyToOne
    @JoinColumn(name = "idx_rate_id")
    private EvaluationIndexRate indexRate;

    public Double getRate() {
        return Objects.isNull(this.rate) ? null : this.rate.doubleValue();
    }

    public void setRate(Double rate) {
        this.rate = Objects.isNull(rate) ? null : BigDecimal.valueOf(rate);
    }
}
