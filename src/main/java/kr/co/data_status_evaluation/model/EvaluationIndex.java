package kr.co.data_status_evaluation.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import kr.co.data_status_evaluation.model.enums.EvaluationType;
import kr.co.data_status_evaluation.model.key.RelevantInstitutionKey;
import kr.co.data_status_evaluation.model.vo.EvaluationIndexVo;
import kr.co.data_status_evaluation.util.BooleanToYNConverter;
import kr.co.data_status_evaluation.util.ModelConverter;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Entity
@Table(name = "tb_rev_evl_idx")
@Getter
@Setter
@ToString
public class EvaluationIndex {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "evl_ty")
    private EvaluationType type;

    @Column(name = "idx_no")
    private String no;

    @Column(name = "idx_nm")
    private String name;

    @Column(name = "idx_qestn")
    private String question;

    @Column(name = "idx_dc")
    private String description;

    @Column(name = "file_dc")
    private String fileDescription;

    @Column(name = "evl_scr_yn")
    @Convert(converter = BooleanToYNConverter.class)
    private boolean scorePublic;

    @Column(name = "prfrmnc_input_yn")
    @Convert(converter = BooleanToYNConverter.class)
    private boolean performanceInput;

    @Column(name = "rao_posbl_yn")
    @Convert(converter = BooleanToYNConverter.class)
    private boolean objectionPossible;

    @Column(name = "atch_file_yn")
    @Convert(converter = BooleanToYNConverter.class)
    private boolean attachFile;

    @Column(name = "na_yn")
    @Convert(converter = BooleanToYNConverter.class)
    private boolean naLevel;

    @Column(name = "begin_dt")
    private Instant beginDt;

    @Column(name = "end_dt")
    private Instant endDt;

    @CreationTimestamp
    @Column(name = "creat_dt")
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updt_dt")
    private Instant updatedAt;

    @ManyToOne(optional = false)
    @JoinColumn(name = "fld_id")
    private EvaluationField evaluationField;

    @OneToMany(mappedBy = "evaluationIndex", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("level, institutionCategory ASC")
    private List<EvaluationScore> scores;

    @OneToMany(mappedBy = "evaluationIndex", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RelevantInstitution> relevantInstitutions;

    @OneToMany(mappedBy = "evaluationIndex", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EvaluationResult> results;

    @JsonIgnore
    @ManyToMany(mappedBy = "indices")
    List<Account> accounts;

    @ManyToOne
    @JoinTable(name = "tb_rev_evl_idx_idx_rate",
            joinColumns = @JoinColumn(name = "idx_id"),
            inverseJoinColumns = @JoinColumn(name = "idx_rate_id"))
    EvaluationIndexRate rate;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    public Date getBeginDt() {
        return Objects.isNull(this.beginDt) ? null : Date.from(this.beginDt);
    }

    public void setBeginDt(Date beginDt) {
        this.beginDt = Objects.isNull(beginDt) ? null : beginDt.toInstant();
    }

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    public Date getEndDt() {
        return Objects.isNull(this.endDt) ? null : Date.from(this.endDt);
    }

    public void setEndDt(Date endDt) {
        this.endDt = Objects.isNull(endDt) ? null : endDt.toInstant();
    }

    public EvaluationIndex() {

    }

    public EvaluationIndex(EvaluationIndexVo vo) throws ParseException {
        this.no = vo.getNo();
        this.type = vo.getType();
        this.name = vo.getName();
        this.question = vo.getQuestion();
        this.description = vo.getDescription();
        this.fileDescription = vo.getFileDescription();
        this.scorePublic = vo.isScorePublic();
        this.performanceInput = vo.isPerformanceInput();
        this.objectionPossible = vo.isObjectionPossible();
        this.attachFile = vo.isAttachFile();
        this.naLevel = vo.isNaLevel();
        this.beginDt = Objects.isNull(vo.getBeginDt()) ? null : vo.getBeginDt().toInstant();
        this.endDt = Objects.isNull(vo.getEndDt()) ? null : vo.getEndDt().toInstant();
        if (vo.getScores().size() > 0) {
            List<EvaluationScore> evaluationScores = vo.getScores()
                    .stream()
                    .map(el -> (EvaluationScore) ModelConverter.convertObject(el, EvaluationScore.class))
                    .collect(Collectors.toList());
            evaluationScores.forEach(el -> el.setEvaluationIndex(this));
            this.scores = evaluationScores;
        }

        if (vo.getRelevantInstitutions().size() > 0) {
            List<RelevantInstitution> relevantInstitutions = vo.getRelevantInstitutions();
            relevantInstitutions.forEach(el -> {
                el.setEvaluationIndex(this);
                el.setId(new RelevantInstitutionKey(this, el.getInstitutionCategory()));
            });
            this.relevantInstitutions = relevantInstitutions;
        }
    }

    public void setAll(EvaluationIndexVo vo) throws ParseException {
        this.no = vo.getNo();
        this.type = vo.getType();
        this.name = vo.getName();
        this.question = vo.getQuestion();
        this.description = vo.getDescription();
        this.fileDescription = vo.getFileDescription();
        this.scorePublic = vo.isScorePublic();
        this.performanceInput = vo.isPerformanceInput();
        this.objectionPossible = vo.isObjectionPossible();
        this.attachFile = vo.isAttachFile();
        this.naLevel = vo.getType().equals(EvaluationType.QUANTITATION) && vo.isNaLevel();
        this.beginDt = Objects.isNull(vo.getBeginDt()) ? null : vo.getBeginDt().toInstant();
        this.endDt = Objects.isNull(vo.getEndDt()) ? null : vo.getEndDt().toInstant();
        this.scores.clear();
        if (vo.getScores().size() > 0) {
            List<EvaluationScore> evaluationScores = vo.getScores()
                    .stream()
                    .map(el -> (EvaluationScore) ModelConverter.convertObject(el, EvaluationScore.class))
                    .collect(Collectors.toList());
            evaluationScores.forEach(el -> el.setEvaluationIndex(this));
            this.scores.addAll(evaluationScores);
        }

        this.relevantInstitutions.clear();
        if (vo.getRelevantInstitutions().size() > 0) {
            List<RelevantInstitution> relevantInstitutions = vo.getRelevantInstitutions();
            relevantInstitutions.forEach(el -> {
                el.setEvaluationIndex(this);
                el.setId(new RelevantInstitutionKey(this, el.getInstitutionCategory()));
            });
            this.relevantInstitutions.addAll(relevantInstitutions);
        }
    }


    public String getFullName() {
        StringJoiner sj = new StringJoiner(". ");
        sj.add(this.getNo());
        sj.add(this.getName());
        return sj.toString();
    }

    public List<EvaluationScore> getScoresOrderByLevel() {
        scores.sort(Comparator.comparing(EvaluationScore::getLevel));
        return scores;

    }

    public List<Integer> getScoresLevel() {
        return this.scores.stream().map(EvaluationScore::getLevel).distinct().collect(Collectors.toList());
    }

    public String getTotalScore() {
        Float totalScore = new Float(0.0);
        for (EvaluationScore score : this.scores) {
            for (RelevantInstitution relevantInstitution : this.relevantInstitutions) {
                if (score.getInstitutionCategory().equals(relevantInstitution.getInstitutionCategory())) {
                    totalScore += score.getScore();
                }
            }
        }

        return totalScore.toString();
    }

    public Float getTotalScore(InstitutionCategory category) {
        for (EvaluationScore score : this.scores) {
            if (Objects.equals(score.getInstitutionCategory(), category)) {
                if (score.getLevel() == null || score.getLevel() == 1) {
                    return score.getScore();
                }
            }
        }
        return null;
    }

    public List<EvaluationScore> getScores(InstitutionCategory category) {
        List<EvaluationScore> scores = new ArrayList<>();
        for (EvaluationScore score : this.scores) {
            if (Objects.equals(score.getInstitutionCategory(), category)) {
                scores.add(score);
            }
        }
        return scores;
    }

    public boolean isAssigned(InstitutionCategory category) throws Exception {
        return this.relevantInstitutions.stream()
                .filter(el -> Objects.equals(el.getInstitutionCategory(), category)).findFirst()
                .map(RelevantInstitution::isYn).orElse(false);
    }

    public List<InstitutionCategory> getAssignedCategories() {
        List<InstitutionCategory> categories = new ArrayList<>();
        for (RelevantInstitution relevantInstitution : this.relevantInstitutions) {
            if (relevantInstitution.isYn()) {
                categories.add(relevantInstitution.getInstitutionCategory());
            }
        }
        return categories;
    }

    public List<Institution> getAssignedInstitutions() {
        List<Institution> institutions = new ArrayList<>();
        List<InstitutionCategory> categories = this.getAssignedCategories();
        for (InstitutionCategory category : categories) {
            institutions.addAll(category.getInstitutions());
        }

        return institutions;
    }

    public boolean isContainByYear(String year) {
        int prevYear = Integer.parseInt(year) - 1;
        int nextYear = Integer.parseInt(year) + 1;
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        Date start = null;
        Date end = null;
        try {
            start = df.parse(prevYear + "-12-31");
            end = df.parse(nextYear + "-01-01");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Date beginDt = this.getBeginDt();
        Date endDt = this.getEndDt();

        return beginDt.after(start) && endDt.before(end);
    }

    public boolean isContainMaterial(List<Material> materials) {
        Optional<Material> optional = materials.stream().filter(material -> material.getAtchTrgtId().equals(this.id)).findFirst();
        return optional.isPresent();
    }
}