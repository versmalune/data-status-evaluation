package kr.co.data_status_evaluation.model;

import kr.co.data_status_evaluation.model.enums.EvaluationStatus;
import kr.co.data_status_evaluation.util.StringUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "tb_rev_evl_result")
@Getter
@Setter
@RequiredArgsConstructor
@DynamicInsert
@DynamicUpdate
public class EvaluationResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "instt_id")
    private Institution institution;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idx_id")
    private EvaluationIndex evaluationIndex;

    @Column(name = "evl_yr")
    private String year;

    @Column(name = "scr")
    private BigDecimal score;

    @Column(name = "rvw_opinion")
    private String opinion;

    @Column(name = "objc")
    private String objection;

    @Column(name = "objc_rvw_opinion")
    private String objectionReview;

    @Enumerated(EnumType.STRING)
    @Column(name = "process_sttus_cd")
    private EvaluationStatus processStatus;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "tb_rev_file_evl_result", joinColumns = @JoinColumn(name = "evl_result_id"),
            inverseJoinColumns = @JoinColumn(name = "file_id"))
    @OrderBy("createdAt ASC")
    Set<File> files = new LinkedHashSet<>();

    @CreationTimestamp
    @Column(updatable = false, name = "creat_dt")
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updt_dt")
    private Instant updatedAt;

    public String getScore() {
        return Objects.isNull(this.score) ? null : String.valueOf(this.score.setScale(2, RoundingMode.HALF_UP).doubleValue());
    }

    public Float getScoreToFloat() {
        return Objects.isNull(this.score) ? null : this.score.floatValue();
    }

    public void setScore(String score) {
        this.score = StringUtils.isNullOrEmpty(score) ? null : BigDecimal.valueOf(Double.parseDouble(score));
    }

    public void addFile(File file) {
        this.files.add(file);
        file.getEvaluationResults().add(this);
    }

    public void removeFile(File file) {
        this.files.remove(file);
        file.getEvaluationResults().remove(this);
    }

    public String getUpdatedAtToString() {
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                .withLocale(Locale.KOREA)
                .withZone(ZoneId.systemDefault());
        return df.format(this.updatedAt);
    }

    public String getObjection() {
        return StringUtils.isNullOrEmpty(this.objection) ? "없음" : this.objection;
    }

    public String getOpinion() {
        return StringUtils.isNullOrEmpty(this.opinion) ? null : this.opinion;
    }

    public Instant getCurrentFileCreatedDt() {
        Set<File> files = this.getFiles();

        if (files.isEmpty()) {
            return null;
        }
        File lastFile = (File) files.toArray()[files.size()-1];

        return lastFile.getCreatedAt();
    }
}

