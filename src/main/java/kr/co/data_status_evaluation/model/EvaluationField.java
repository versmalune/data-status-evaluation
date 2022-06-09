package kr.co.data_status_evaluation.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "tb_rev_evl_fld")
@Getter
@Setter
@NoArgsConstructor
public class EvaluationField {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fld_no")
    private int no;

    @Column(name = "fld_nm")
    private String name;

    @Column(name = "evl_yr", columnDefinition = "CHAR")
    private String year;

    @Column(name = "fld_scr")
    private int score;

    @CreationTimestamp
    @Column(name = "creat_dt")
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updt_dt")
    private Instant updatedAt;

    @JsonIgnore
    @OneToMany(mappedBy = "evaluationField", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("no ASC")
    List<EvaluationIndex> evaluationIndices;

    public static EvaluationField getNewInstance() {
        return new EvaluationField(1);
    }

    private EvaluationField(int no) {
        this.no = no;
        this.year = Integer.toString(LocalDate.now().getYear());
    }
}
