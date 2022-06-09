package kr.co.data_status_evaluation.model;

import kr.co.data_status_evaluation.model.enums.EvaluationStatus;
import kr.co.data_status_evaluation.model.vo.LmsInstitutionVo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Entity
@Table(name = "tb_rev_instt")
@Getter
@Setter
@SQLDelete(sql = "UPDATE tb_rev_instt SET del_yn = 'Y' WHERE id=?")
@Where(clause = "del_yn='N'")
@NoArgsConstructor
public class Institution implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "instt_cd", columnDefinition = "CHAR")
    private String code;

    @Column(name = "instt_nm")
    private String name;

//    @Enumerated(EnumType.STRING)
    @Column(name = "instt_ty")
    private String type;

    @Column(columnDefinition = "CHAR", name = "del_yn")
    private String deleted = "N";

    private String beforeInsttCd;

    @OneToMany(mappedBy = "institution")
    private List<Account> accounts;

    @OneToMany(mappedBy = "institution")
    private List<EvaluationResult> evaluationResults;

    @OneToMany(mappedBy = "institution", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EvaluationResultTotal> evaluationResultTotals;

    @OneToMany(mappedBy = "institution")
    private List<EvaluationTargetInstitution> targetInstitution;

    @OneToMany(mappedBy = "institution", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<InstitutionDetail> institutionDetails;

    @ManyToOne
    @JoinColumn(name = "instt_category_id")
    private InstitutionCategory category;

    @CreationTimestamp
    @Column(name = "creat_dt")
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updt_dt")
    private Instant updatedAt;

    @Column(name = "last_access_dt")
    private Instant lastAccessDt;

    public Institution(LmsInstitutionVo lmsInstitutionVo) {
        this.code = lmsInstitutionVo.getInsttCd();
        this.name = lmsInstitutionVo.getInsttNm();
        this.type = lmsInstitutionVo.getStatusInsttType();
        if (lmsInstitutionVo.getMntnabYn())
            this.deleted = "Y";
        this.beforeInsttCd = lmsInstitutionVo.getBeforeInsttCd();
    }

    public Institution(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public void setFromLmsVo(LmsInstitutionVo lmsInstitutionVo) {
        this.code = lmsInstitutionVo.getInsttCd();
        this.name = lmsInstitutionVo.getInsttNm();
        this.type = lmsInstitutionVo.getStatusInsttType();
        if (lmsInstitutionVo.getMntnabYn())
            this.deleted = "Y";
        else
            this.deleted = "N";
        this.beforeInsttCd = lmsInstitutionVo.getBeforeInsttCd();
    }

    public void setFromLmsVoExceptType(LmsInstitutionVo vo) {
        this.code = vo.getInsttCd();
        this.name = vo.getInsttNm();
        if (vo.getMntnabYn())
            this.deleted = "Y";
        else
            this.deleted = "N";
        this.beforeInsttCd = vo.getBeforeInsttCd();
    }

    public String getFullName() {
        StringBuffer sb = new StringBuffer();
        sb.append("(")
                .append(this.code)
                .append(")")
                .append(" ")
                .append(this.name);

        return sb.toString();
    }

    public EvaluationResult getEvaluationResult(EvaluationIndex index) {
        for (EvaluationResult evaluationResult : this.evaluationResults) {
            if (Objects.equals(evaluationResult.getEvaluationIndex(), index)) {
                return evaluationResult;
            }
        }
        EvaluationResult result = new EvaluationResult();
        result.setProcessStatus(EvaluationStatus.NONE);
        return result;
    }

    public EvaluationResultTotal getResultTotal(EvaluationField field) {
        if (this.evaluationResultTotals != null && this.evaluationResultTotals.size() > 0) {
            return this.evaluationResultTotals.stream()
                    .filter(el -> Objects.equals(el.getEvaluationField(), field)).findFirst().orElse(null);
        }
        return null;
    }

    public String getStatus() {
        int index = 0;
        for (EvaluationResult result : this.evaluationResults) {
            EvaluationStatus status = result.getProcessStatus();
            if (index < EvaluationStatus.getIndex(status)) {
                index = EvaluationStatus.getIndex(status);
            }
        }

        return EvaluationStatus.getStatus(index).getTitle();
    }

    public String getStatus(String year) {
        int index = 0;
        List<EvaluationResult> results = this.evaluationResults.stream().filter(result-> result.getYear().equals(year)).collect(Collectors.toList());
        for (EvaluationResult result : results) {
            EvaluationStatus status = result.getProcessStatus();
            if (index < EvaluationStatus.getIndex(status)) {
                index = EvaluationStatus.getIndex(status);
            }
        }

        return EvaluationStatus.getStatus(index).getTitle();
    }

    public String getTypeByYear(String year) {
        for (InstitutionDetail institutionDetail : this.institutionDetails) {
            if (institutionDetail.getYear().equals(year)) {
                return institutionDetail.getCategory().getName();
            }
        }
        return this.category.getName();
    }

    public InstitutionCategory getCategoryByYear(String year) {
        for (InstitutionDetail institutionDetail : this.institutionDetails) {
            if (institutionDetail.getYear().equals(year)) {
                return institutionDetail.getCategory();
            }
        }
        return this.category;
    }
}
