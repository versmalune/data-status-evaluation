package kr.co.data_status_evaluation.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tb_rev_evl_idx_rate")
@Getter
@Setter
@NoArgsConstructor
public class EvaluationIndexRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "evl_yr", columnDefinition = "CHAR(4)")
    private String year;

    @OneToMany(mappedBy = "indexRate", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("level ASC")
    List<EvaluationIndexRateDetail> details = new ArrayList<>();

    @OneToMany
    @JoinTable(name = "tb_rev_evl_idx_idx_rate",
            joinColumns = @JoinColumn(name = "idx_rate_id"),
            inverseJoinColumns = @JoinColumn(name = "idx_id"))
    List<EvaluationIndex> indices = new ArrayList<>();
}