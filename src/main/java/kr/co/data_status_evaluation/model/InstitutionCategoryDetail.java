package kr.co.data_status_evaluation.model;

import kr.co.data_status_evaluation.model.dto.InstitutionCategoryDetailDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "tb_rev_instt_category_detail")
@Getter
@Setter
@NoArgsConstructor
public class InstitutionCategoryDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private InstitutionCategory institutionCategory;

    @Column(name = "detail_category_cd")
    private String code;

    @Column(name = "detail_category_nm")
    private String name;

    @Column(name = "detail_category_dc")
    private String description;

    @CreationTimestamp
    @Column(name = "creat_dt")
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updt_dt")
    private Instant updatedAt;

    public InstitutionCategoryDetail(InstitutionCategoryDetailDto detailDto) {
        this.code = detailDto.getCode();
        this.name = detailDto.getName();
        this.description = detailDto.getDescription();
    }

    public void setFromDto(InstitutionCategoryDetailDto detailDto) {
        this.code = detailDto.getCode();
        this.name = detailDto.getName();
        this.description = detailDto.getDescription();
    }
}
