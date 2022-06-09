package kr.co.data_status_evaluation.model.vo;

import kr.co.data_status_evaluation.model.*;
import kr.co.data_status_evaluation.model.dto.EvaluationResultPercentRankDto;
import kr.co.data_status_evaluation.model.enums.EvaluationType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Value;

import java.math.BigInteger;
import java.time.Instant;
import java.util.*;


@Getter
@Setter
@NoArgsConstructor
public class InsttIndexVo {
    private Long id;
    private EvaluationField evaluationField;
    private EvaluationType type;
    private String no;
    private String name;
    private String question;
    private String description;
    private String fileDescription;
    private boolean scorePublic;
    private boolean performanceInput;
    private boolean objectionPossible;
    private boolean attachFileYn;
    private boolean naLevel;
    private Date beginDt;
    private Date endDt;
    private List<RelevantInstitution> relevantInstitutions;
    private List<EvaluationScore> scores;
    private EvaluationResult result;
    private Integer qualitativeRank;

    public InsttIndexVo (EvaluationIndex index, EvaluationResult result) {
        this.id = index.getId();
        this.evaluationField = index.getEvaluationField();
        this.type = index.getType();
        this.no = index.getNo();
        this.name = index.getName();
        this.question = index.getQuestion();
        this.description = index.getDescription();
        this.fileDescription = index.getFileDescription();
        this.scorePublic = index.isScorePublic();
        this.performanceInput = index.isPerformanceInput();
        this.objectionPossible = index.isObjectionPossible();
        this.attachFileYn = index.isAttachFile();
        this.naLevel = index.isNaLevel();
        this.beginDt = index.getBeginDt();
        this.endDt = index.getEndDt();
//        this.scores = index.getScores();
        this.relevantInstitutions = index.getRelevantInstitutions();
        this.evaluationField = index.getEvaluationField();
        this.result = result;
    }

    public String getPerfectScore(InstitutionCategory institutionCategory) {
        Float perfectScore = new Float(0.0);
        for (EvaluationScore score : this.scores) {
            if (score.getLevel() != null) {
                if (score.getInstitutionCategory() == institutionCategory && score.getLevel() == 1) {
                    perfectScore = score.getScore();
                    break;
                }
            } else {
                if (score.getInstitutionCategory() == institutionCategory) {
                    perfectScore = score.getScore();
                    break;
                }
            }
        }

        return perfectScore.toString();
    }

    public String getPerfectScore(String categoryName) {
        Float perfectScore = new Float(0.0);
        for (EvaluationScore score : this.scores) {
            if (score.getLevel() != null) {
                if (score.getInstitutionCategory().getName().equals(categoryName) && score.getLevel() == 1) {
                    perfectScore = score.getScore();
                    break;
                }
            } else {
                if (score.getInstitutionCategory().getName().equals(categoryName)) {
                    perfectScore = score.getScore();
                    break;
                }
            }
        }

        return perfectScore.toString();
    }

    public Instant getCurrentFileCreatedDt() {
        Set<File> files = this.result.getFiles();

        if (files.isEmpty())
            return null;
        File lastFile = (File) files.toArray()[files.size()-1];

        return lastFile.getCreatedAt();
    }

    public Integer getQuantitationRank() {
        Integer rank = null;
        if (Objects.isNull(this.result.getScore()) || this.scores.isEmpty()) {
            return rank;
        }
        for (EvaluationScore score : this.scores) {
            EvaluationType type = score.getEvaluationIndex().getType();
            if (!Objects.isNull(score.getScore()) && !Objects.isNull(type) && type.equals(EvaluationType.QUANTITATION)) {
                if (score.getScore() == Float.parseFloat(this.result.getScore())) {
                    rank = score.getLevel();
                    break;
                }
            }
        }

        return rank;
    }

    public void setQualitativeRankByRate(List<EvaluationResultPercentRankDto> rankList) {
        Integer qualitativeRank = null;

        if (!Objects.isNull(this.result)) {
            for (EvaluationResultPercentRankDto rank : rankList) {
                if (!Objects.isNull(this.result.getInstitution()) && !Objects.isNull(this.result.getEvaluationIndex())
                        && rank.getInstitutionId().equals(BigInteger.valueOf(this.result.getInstitution().getId()))
                        && rank.getIndexId().equals(BigInteger.valueOf(this.result.getEvaluationIndex().getId()))) {

                    Double qualitativeRate = rank.getPercentRank();
                    if (qualitativeRate == 0) qualitativeRate = 100.0;

                    List<EvaluationIndexRateDetail> rateDetails =  this.result.getEvaluationIndex().getRate().getDetails();
                    rateDetails.sort(Comparator.comparingDouble(EvaluationIndexRateDetail::getRate));
                    for (EvaluationIndexRateDetail rate : rateDetails) {
                        if (qualitativeRate <= rate.getRate()) {
                            qualitativeRank = rate.getLevel();
                            break;
                        }
                    }
                    if (rateDetails.size() > 0 && Objects.isNull(qualitativeRank)) {
                        qualitativeRank = rateDetails.get(rateDetails.size()-1).getLevel();
                    }

                    break;
                }
            }
        }
        this.setQualitativeRank(qualitativeRank);
    }

    public boolean isContainMaterial(List<Material> materials) {
        Optional<Material> optional = materials.stream().filter(material -> material.getAtchTrgtId().equals(this.id)).findFirst();
        return optional.isPresent();
    }

    public String getDescription() {
        String description = StringEscapeUtils.unescapeJava(this.description);
        if (description.contains("/resources/assets/images/")) {
            description = description.replaceAll("/resources/assets/images/", "/dse/imgs/");
        }
        return description;
    }
}