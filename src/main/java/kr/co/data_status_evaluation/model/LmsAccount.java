package kr.co.data_status_evaluation.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import kr.co.data_status_evaluation.model.vo.LmsAccountVo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "tb_rev_list_regist_mmg_sys_instt_user_info")
@NoArgsConstructor
@ToString
@Getter
@Setter
public class LmsAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Long id;

    private String mberId;

    @Column(columnDefinition = "CHAR")
    private String mberSttus;

    @Column(columnDefinition = "CHAR")
    private String mberAuthor;

    private String password;

    @Column(columnDefinition = "CHAR")
    private String insttCd;

    private String chargerNm;

    private String chargerEmail;

    private String chargerTelnoEnc;

    @CreationTimestamp
    @Column(name = "creat_dt")
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updt_dt")
    private Instant updatedAt;

    public LmsAccount(LmsAccountVo lmsAccountVo) {
        this.mberId = lmsAccountVo.getMberId();
        this.mberSttus = lmsAccountVo.getMberSttus();
        this.mberAuthor = lmsAccountVo.getMberAuthor();
        this.password = lmsAccountVo.getPassword();
        this.insttCd = lmsAccountVo.getInsttCd();
        this.chargerNm = lmsAccountVo.getChargerNm();
        this.chargerEmail = lmsAccountVo.getChargerEmail();
        this.chargerTelnoEnc = lmsAccountVo.getChargerTelnoEnc();

    }

    public void setFromVo(LmsAccountVo lmsAccountVo) {
        this.mberId = lmsAccountVo.getMberId();
        this.mberSttus = lmsAccountVo.getMberSttus();
        this.mberAuthor = lmsAccountVo.getMberAuthor();
        this.password = lmsAccountVo.getPassword();
        this.insttCd = lmsAccountVo.getInsttCd();
        this.chargerNm = lmsAccountVo.getChargerNm();
        this.chargerEmail = lmsAccountVo.getChargerEmail();
        this.chargerTelnoEnc = lmsAccountVo.getChargerTelnoEnc();
    }

}
