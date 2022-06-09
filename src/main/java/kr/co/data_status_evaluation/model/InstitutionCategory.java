package kr.co.data_status_evaluation.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import kr.co.data_status_evaluation.model.dto.InstitutionCategoryDto;
import kr.co.data_status_evaluation.util.StringUtils;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tb_rev_instt_category")
@Getter
@Setter
@ToString
@NoArgsConstructor
public class InstitutionCategory implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "category_cd", columnDefinition = "CHAR")
    private String code;

    @Column(name = "category_nm")
    private String name;

    @Column(name = "category_dc")
    private String description;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "institutionCategory")
    @JsonIgnore
    private List<InstitutionCategoryDetail> detailCategories = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "creat_dt")
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updt_dt")
    private Instant updatedAt;

    @JsonIgnore
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "category")
    private List<Institution> institutions;

    public InstitutionCategory(InstitutionCategoryDto dto) {
        this.code = dto.getCode();
        this.name = dto.getName();
        this.description = dto.getDescription();
    }

    public void setFromDto(InstitutionCategoryDto dto) {
        this.code = dto.getCode();
        this.name = dto.getName();
        this.description = dto.getDescription();
    }

    public String getCode() {
        return StringUtils.isNullOrEmpty(this.code) ? "" : this.code.trim();
    }
}
