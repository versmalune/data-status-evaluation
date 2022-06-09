package kr.co.data_status_evaluation.model;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "tb_rev_log")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Log {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId;

    private String insttCd;

    @Column(name = "actn_nm")
    private String actionName;

    @CreationTimestamp
    @Column(name = "actn_dt")
    private Instant actionDt;

    @Column(name = "evl_yr")
    private String year;
}
