package kr.co.data_status_evaluation.model;

import kr.co.data_status_evaluation.model.vo.LmsInstitutionVo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tb_rev_list_regist_mmg_sys_instt_info")
@NoArgsConstructor
@Getter
@Setter
public class LmsInstitution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "CHAR")
    private String insttCd;

    private String insttNm;

    @Column(columnDefinition = "CHAR")
    private String tyClLarge;

    @Column(columnDefinition = "CHAR")
    private String tyClMedium;

    @Column(columnDefinition = "CHAR")
    private String tyClSmall;

    @Column(columnDefinition = "CHAR")
    private Boolean mntnabYn;

    private String beforeInsttCd;

    @Column(name = "creat_dt")
    private Instant createdAt;

    @Column(name = "updt_dt")
    private Instant updatedAt;

    public LmsInstitution(LmsInstitutionVo lmsInstitutionVo) {
        this.insttCd = lmsInstitutionVo.getInsttCd();
        this.insttNm = lmsInstitutionVo.getInsttNm();
        this.tyClLarge = lmsInstitutionVo.getTyClLarge();
        this.tyClMedium = lmsInstitutionVo.getTyClMedium();
        this.tyClSmall = lmsInstitutionVo.getTyClSmall();
        this.mntnabYn = lmsInstitutionVo.getMntnabYn();
        this.beforeInsttCd = lmsInstitutionVo.getBeforeInsttCd();
        this.createdAt = lmsInstitutionVo.getRegistDt().toInstant();
        if (lmsInstitutionVo.getUpdtDt() == null)
            this.updatedAt = lmsInstitutionVo.getRegistDt().toInstant();
        else
            this.updatedAt = lmsInstitutionVo.getUpdtDt().toInstant();
    }

    public void setFromLmsVo(LmsInstitutionVo lmsInstitutionVo) {
        this.insttCd = lmsInstitutionVo.getInsttCd();
        this.insttNm = lmsInstitutionVo.getInsttNm();
        this.tyClLarge = lmsInstitutionVo.getTyClLarge();
        this.tyClMedium = lmsInstitutionVo.getTyClMedium();
        this.tyClSmall = lmsInstitutionVo.getTyClSmall();
        this.mntnabYn = lmsInstitutionVo.getMntnabYn();
        this.beforeInsttCd = lmsInstitutionVo.getBeforeInsttCd();
        this.createdAt = lmsInstitutionVo.getRegistDt().toInstant();
        if (lmsInstitutionVo.getUpdtDt() == null)
            this.updatedAt = lmsInstitutionVo.getRegistDt().toInstant();
        else
            this.updatedAt = lmsInstitutionVo.getUpdtDt().toInstant();
    }
}
