package kr.co.data_status_evaluation.model;

import kr.co.data_status_evaluation.model.enums.InstitutionType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "tb_rev_instt_info_dev")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class InstitutionInfo {

    @Id
    @Column(name = "instt_cd")
    private String code;

    @Column(name="instt_nm")
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name="instt_ty")
    private InstitutionType type;

    @CreationTimestamp
    @Column(name = "creat_dt")
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updt_dt")
    private Instant updatedAt;
}
