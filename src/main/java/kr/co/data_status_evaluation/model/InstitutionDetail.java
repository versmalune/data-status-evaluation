package kr.co.data_status_evaluation.model;

import kr.co.data_status_evaluation.model.key.InstitutionDetailKey;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "tb_rev_instt_detail")
@IdClass(InstitutionDetailKey.class)
@Getter
@Setter
public class InstitutionDetail {

    @Id
    @Column(name = "evl_yr")
    private String year;

    @Column(name = "instt_ty")
    private String type;

    @Id
    @ManyToOne
    @JoinColumn(name = "instt_id")
    private Institution institution;

    @ManyToOne
    @JoinColumn(name = "instt_category_id")
    private InstitutionCategory category;

    public InstitutionDetail() {}

    public InstitutionDetail(Institution institution, String year) {
        this.institution = institution;
        this.year = year;
    }

    @Builder
    public InstitutionDetail(Institution institution, String year, InstitutionCategory category, String type) {
        this.institution = institution;
        this.year = year;
        this.category = category;
        this.type = type;
    }
}
