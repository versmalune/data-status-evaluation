package kr.co.data_status_evaluation.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "tb_rev_evl_trgt_instt")
@Getter
@Setter
public class EvaluationTargetInstitution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "instt_id")
    private Institution institution;

    @Column(name = "evl_yr")
    private String year;

    @Column(columnDefinition = "CHAR", name = "trgt_instt_yn")
    private String trgtInsttYn = "N";
}