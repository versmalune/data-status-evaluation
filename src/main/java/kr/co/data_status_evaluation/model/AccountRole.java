package kr.co.data_status_evaluation.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import kr.co.data_status_evaluation.model.enums.Author;
import kr.co.data_status_evaluation.model.vo.LmsAccountVo;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(name = "tb_rev_account_role")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AccountRole implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private Author author;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "accnt_id")
    private Account account;

    @JsonIgnore
    @CreationTimestamp
    @Column(name = "creat_dt")
    private Instant createdAt;

    @JsonIgnore
    @UpdateTimestamp
    @Column(name = "updt_dt")
    private Instant updatedAt;

    public AccountRole(LmsAccountVo vo) {
        this.author = vo.getStatusAuthor();
    }

    public void setLmsAccountVo(LmsAccountVo vo) {
        this.author = vo.getStatusAuthor();
    }
}
