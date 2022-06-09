package kr.co.data_status_evaluation.service.dw;

import kr.co.data_status_evaluation.model.EvaluationField;
import kr.co.data_status_evaluation.model.EvaluationScore;
import kr.co.data_status_evaluation.model.Institution;
import kr.co.data_status_evaluation.model.dw.IndexResultFact;
import kr.co.data_status_evaluation.model.dw.InstitutionResultFact;
import kr.co.data_status_evaluation.model.enums.EvaluationStatus;
import kr.co.data_status_evaluation.model.vo.FieldScoreResultVo;
import kr.co.data_status_evaluation.model.vo.IndexScoreResultVo;
import kr.co.data_status_evaluation.model.vo.InstitutionResultVo;
import kr.co.data_status_evaluation.repository.dw.IndexResultFactRepository;
import kr.co.data_status_evaluation.repository.dw.InstitutionResultFactRepository;
import kr.co.data_status_evaluation.service.EvaluationFieldService;
import kr.co.data_status_evaluation.service.EvaluationScoreService;
import kr.co.data_status_evaluation.service.InstitutionCategoryService;
import kr.co.data_status_evaluation.service.InstitutionService;
import kr.co.data_status_evaluation.util.StringUtils;
import lombok.RequiredArgsConstructor;
import ognl.Evaluation;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FactService {

    private final InstitutionResultFactRepository institutionResultFactRepository;
    private final IndexResultFactRepository indexResultFactRepository;
    private final EvaluationFieldService evaluationFieldService;
    private final InstitutionService institutionService;
    private final EvaluationScoreService evaluationScoreService;
    private final EntityManager entityManager;

    public Optional<InstitutionResultFact> findInstitutionResultByInstitutionIdAndYear(Long institutionId, String year) {
        return this.institutionResultFactRepository.findByInstitutionIdAndYear(institutionId, year);
    }

    public Optional<IndexResultFact> findIndexResultByInstitutionIdAndIndexIdAndYear(Long institutionId, Long indexId, String year) {
        return this.indexResultFactRepository.findByInstitutionIdAndIndexIdAndYear(institutionId, indexId, year);
    }

    public List<InstitutionResultFact> findAllInstitutionResultByYear(String year) {
        return this.institutionResultFactRepository.findAllByYear(year);
    }

    public Map<Long, InstitutionResultFact> findInstitutionResultFactByYearAndInsttIds(String year, List<Long> insttIds) {
        String sql = "SELECT  dfir.id, dfir.instt_id, dfir.process_sttus_cd\n" +
                "FROM    tb_rev_dw_fact_instt_result dfir\n" +
                "        INNER JOIN  tb_rev_instt i\n" +
                "            ON      dfir.instt_id = i.id\n" +
                "            AND     dfir.evl_yr = :year\n" +
                "            AND     i.id in (:insttIds)";

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("year", year);
        query.setParameter("insttIds", insttIds);

        List<Object[]> objects = query.getResultList();
        Map<Long, InstitutionResultFact> results = new HashMap<>();

        objects.forEach(result -> {
            BigInteger idBigInteger = (BigInteger) result[0];
            Long id = idBigInteger.longValue();

            BigInteger insttIdBigInteger = (BigInteger) result[1];
            Long insttId = insttIdBigInteger.longValue();

            String processStatusCd = (String) result[2];
            EvaluationStatus evaluationStatus = EvaluationStatus.valueOf(processStatusCd);

            InstitutionResultFact institutionResultFact = new InstitutionResultFact(insttId, year);
            institutionResultFact.setId(id);
            institutionResultFact.setProcessStatus(evaluationStatus);

            results.put(insttId, institutionResultFact);
        });

        return results;
    }

    public List<InstitutionResultVo> findAllInstitutionResultByStatusAndYear(String status, String year) {
        Float fieldTotalScore = 0.0F;
        List<EvaluationField> fields = this.evaluationFieldService.findAllByYear(year);
        for (EvaluationField field : fields) {
            if (!field.getName().contains("기타")) {
                fieldTotalScore += field.getScore();
            }
        }
        Float finalFieldTotalScore = fieldTotalScore;

        String sql = "SELECT c.category_nm, i.instt_nm, ir.scr, ir.ext_scr, ir.process_sttus_cd, ir.updt_dt, " +
                "RANK() OVER (ORDER BY ir.scr * 100 / (SELECT SUM(f.fld_scr) FROM tb_rev_evl_fld f WHERE f.evl_yr = :year AND f.fld_nm NOT LIKE '%기타%') + ir.ext_scr DESC) AS ranking, " +
                " ir.instt_id\n" +
                "FROM tb_rev_dw_fact_instt_result ir\n" +
                "         LEFT OUTER JOIN tb_rev_instt i ON ir.instt_id = i.id\n" +
                "         LEFT OUTER JOIN tb_rev_instt_detail ind ON i.id = ind.instt_id AND ind.evl_yr = :year\n" +
                "         LEFT OUTER JOIN tb_rev_instt_category c ON ind.instt_category_id = c.id" +
                "         LEFT OUTER JOIN tb_rev_evl_trgt_instt ti ON i.id = ti.instt_id\n" +
                "WHERE i.del_yn = 'N'\n" +
                "  AND ti.trgt_instt_yn = 'Y'\n" +
                "  AND ir.evl_yr = :year";
        if (!StringUtils.isNullOrEmpty(status)) {
            sql += "\n  AND ir.process_sttus_cd = :status";
        }
        sql += "\n GROUP BY ir.instt_id";
        Query query = entityManager.createNativeQuery(sql);
        if (!StringUtils.isNullOrEmpty(status)) {
            query.setParameter("status", status);
        }
        query.setParameter("year", year);
        List<Object[]> objects = query.getResultList();
        List<InstitutionResultVo> results = objects.stream()
                .map(result -> {
                    String category = (String) result[0];
                    String name = (String) result[1];
                    BigDecimal score = (BigDecimal) result[2];
                    Float fScore = Objects.isNull(score) ? null : score.floatValue();

                    BigDecimal extraScore = (BigDecimal) result[3];
                    Float fExtraScore = Objects.isNull(extraScore) ? null : extraScore.floatValue();

                    String processStatus = (String) result[4];
                    Timestamp updatedAt = (Timestamp) result[5];
                    Integer ranking = (Integer) result[6];
                    Long institutionId = ((BigInteger) result[7]).longValue();
                    Float standardScore = fScore * 100 / finalFieldTotalScore + fExtraScore;
                    return new InstitutionResultVo(category, name, fScore, fExtraScore, standardScore, processStatus, updatedAt, ranking, institutionId);
                }).collect(Collectors.toList());

        return results;
    }

    public List<InstitutionResultVo> findInstitutionResultByInstitutionIdAndCategoryAndYear(Long institutionId, String categoryCd, String year) {
        Float fieldTotalScore = 0.0F;
        List<EvaluationField> fields = this.evaluationFieldService.findAllByYear(year);
        for (EvaluationField field : fields) {
            if (!field.getName().contains("기타")) {
                fieldTotalScore += field.getScore();
            }
        }
        Float finalFieldTotalScore = fieldTotalScore;

        String sql = "SELECT c.category_nm, i.instt_nm, ir.scr, ir.ext_scr, ir.process_sttus_cd, ir.updt_dt, " +
                "RANK() OVER (ORDER BY ir.scr * 100 / (SELECT SUM(f.fld_scr) FROM tb_rev_evl_fld f WHERE f.evl_yr = :year AND f.fld_nm NOT LIKE '%기타%') + ir.ext_scr DESC) AS ranking, " +
                " ir.instt_id\n" +
                "FROM tb_rev_dw_fact_instt_result ir\n" +
                "         LEFT OUTER JOIN tb_rev_instt i ON ir.instt_id = i.id\n" +
                "         LEFT OUTER JOIN tb_rev_instt_category c ON i.instt_category_id = c.id\n" +
                "         LEFT OUTER JOIN tb_rev_evl_trgt_instt ti ON i.id = ti.instt_id\n" +
                "WHERE i.del_yn = 'N'\n" +
                "  AND ti.trgt_instt_yn = 'Y'\n" +
                "  AND ir.evl_yr = :year\n" +
                "  AND ir.instt_id = :institutionId";
        if (!StringUtils.isNullOrEmpty(categoryCd)) {
            sql += "\n  AND c.category_cd = :categoryCd";
        }
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("year", year);
        query.setParameter("institutionId", institutionId);
        if (!StringUtils.isNullOrEmpty(categoryCd)) {
            query.setParameter("categoryCd", categoryCd);
        }
        List<Object[]> objects = query.getResultList();
        List<InstitutionResultVo> results = objects.stream()
                .map(result -> {
                    String category = (String) result[0];
                    String name = (String) result[1];
                    BigDecimal score = (BigDecimal) result[2];
                    Float fScore = Objects.isNull(score) ? null : score.floatValue();

                    BigDecimal extraScore = (BigDecimal) result[3];
                    Float fExtraScore = Objects.isNull(extraScore) ? null : extraScore.floatValue();

                    String processStatus = (String) result[4];
                    Timestamp updatedAt = (Timestamp) result[5];
                    Integer ranking = (Integer) result[6];
                    Float standardScore = fScore * 100 / finalFieldTotalScore + fExtraScore;
                    return new InstitutionResultVo(category, name, fScore, fExtraScore, standardScore, processStatus, updatedAt, ranking, institutionId);
                }).collect(Collectors.toList());

        return results;
    }

    public List<InstitutionResultVo> findAllInstitutionResultByCategoryAndYear(String categoryCd, String year) {
        String sql = "SELECT c.category_nm, i.instt_nm, ir.scr, ir.ext_scr, ir.process_sttus_cd, ir.updt_dt, " +
                "RANK() OVER (ORDER BY ir.scr + ir.ext_scr DESC) AS ranking, " +
                " ir.instt_id, i.instt_cd\n" +
                "FROM tb_rev_dw_fact_instt_result ir\n" +
                "         LEFT OUTER JOIN tb_rev_instt i ON ir.instt_id = i.id\n" +
                "         LEFT OUTER JOIN tb_rev_instt_detail id ON i.id = id.instt_id AND id.evl_yr = :year\n" +
                "         LEFT OUTER JOIN tb_rev_instt_category c ON id.instt_category_id = c.id\n" +
                "         LEFT OUTER JOIN tb_rev_evl_trgt_instt ti ON i.id = ti.instt_id\n" +
                "WHERE i.del_yn = 'N'\n" +
                "  AND ti.trgt_instt_yn = 'Y'\n" +
                "  AND ti.evl_yr = :year\n" +
                "  AND ir.evl_yr = :year";
        if (!StringUtils.isNullOrEmpty(categoryCd)) {
            sql += "\n  AND c.category_cd = :categoryCd";
        }
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("year", year);
        if (!StringUtils.isNullOrEmpty(categoryCd)) {
            query.setParameter("categoryCd", categoryCd);
        }

        List<Object[]> objects = query.getResultList();
        List<InstitutionResultVo> results = objects.stream()
                .map(result -> {
                    String category = (String) result[0];
                    String name = (String) result[1];
                    BigDecimal score = (BigDecimal) result[2];
                    Float fScore = Objects.isNull(score) ? null : score.floatValue();
                    BigDecimal extraScore = (BigDecimal) result[3];
                    Float fExtraScore = Objects.isNull(extraScore) ? null : extraScore.floatValue();
                    String processStatus = (String) result[4];
                    Timestamp updatedAt = (Timestamp) result[5];
                    Integer ranking = (Integer) result[6];
                    Long institutionId = ((BigInteger) result[7]).longValue();
                    Float standardScore = fScore + fExtraScore;
                    String institutionCode = (String) result[8];
                    return new InstitutionResultVo(category, name, fScore, fExtraScore, standardScore, processStatus, updatedAt, ranking, institutionId, institutionCode);
                }).collect(Collectors.toList());

        return results;
    }

    public List<FieldScoreResultVo> findFieldScoreByInstitutionIdAndYear(Long institutionId, String year) {
        Float fieldTotalScore = 0.0F;
        List<EvaluationField> fields = this.evaluationFieldService.findAllByYear(year);

        String sql = "SELECT f.fld_nm, SUM(idr.scr), idr.instt_id\n" +
                "FROM tb_rev_dw_fact_idx_result idr\n" +
                "         LEFT OUTER JOIN tb_rev_evl_fld f ON idr.fld_id = f.id\n" +
                "WHERE idr.evl_yr = :year\n" +
                "AND idr.instt_id = :institutionId\n" +
                "GROUP BY idr.fld_id, idr.instt_id";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("year", year);
        query.setParameter("institutionId", institutionId);
        List<Object[]> objects = query.getResultList();
        List<FieldScoreResultVo> results = objects.stream()
                .map(result -> {
                    String name = (String) result[0];
                    BigDecimal score = (BigDecimal) result[1];
                    Float fScore = Objects.isNull(score) ? null : score.floatValue();
                    Float standardScore = fScore;
                    if (!name.contains("기타")) {
                        standardScore = fScore * 100 / fields.stream().filter(field -> field.getName().contains(name)).findFirst().get().getScore();
                    }
                    return new FieldScoreResultVo(name, fScore, standardScore, institutionId);
                }).collect(Collectors.toList());

        return results;
    }

    public List<FieldScoreResultVo> findAllOfFieldScoreByYear(String year) {
        String sql = "SELECT f.fld_nm      AS fld_nm,\n" +
                "       SUM(idr.scr)  AS total,\n" +
                "       idr.instt_id  AS instt_id,\n" +
                "       NVL((\n" +
                "                       (SELECT SUM(r1.scr)\n" +
                "                        FROM tb_rev_evl_result r1\n" +
                "                                 LEFT OUTER JOIN tb_rev_evl_idx i1 ON r1.idx_id = i1.id\n" +
                "                                 LEFT OUTER JOIN tb_rev_evl_fld f1 ON i1.fld_id = f1.id\n" +
                "                        WHERE r1.instt_id = idr.instt_id\n" +
                "                          AND r1.evl_yr = :year\n" +
                "                          AND f1.fld_nm NOT LIKE '%기타%')\n" +
                "                       /\n" +
                "                       ((SELECT SUM(fld_scr)\n" +
                "                         FROM tb_rev_evl_fld\n" +
                "                         WHERE evl_yr = :year\n" +
                "                           AND fld_nm NOT LIKE '%기타%')\n" +
                "                           -\n" +
                "                        (SELECT SUM(s2.idx_scr)\n" +
                "                         FROM tb_rev_evl_result r2\n" +
                "                                  LEFT OUTER JOIN tb_rev_evl_idx i2 ON r2.idx_id = i2.id\n" +
                "                                  LEFT OUTER JOIN tb_rev_evl_scr s2 ON s2.idx_id = i2.id\n" +
                "                         WHERE r2.evl_yr = :year\n" +
                "                           AND r2.scr IS NULL\n" +
                "                           AND r2.instt_id = idr.instt_id\n" +
                "                           AND s2.idx_lvl = 1\n" +
                "                           AND s2.instt_category_id = ind.instt_category_id))\n" +
                "                   *\n" +
                "                       (SELECT SUM(s3.idx_scr)\n" +
                "                        FROM tb_rev_evl_result r3\n" +
                "                                 LEFT OUTER JOIN tb_rev_evl_idx i3 ON r3.idx_id = i3.id\n" +
                "                                 LEFT OUTER JOIN tb_rev_evl_fld f3 ON i3.fld_id = f3.id\n" +
                "                                 LEFT OUTER JOIN tb_rev_evl_scr s3 ON s3.idx_id = i3.id\n" +
                "                        WHERE r3.evl_yr = :year\n" +
                "                          AND r3.scr IS NULL\n" +
                "                          AND r3.instt_id = idr.instt_id\n" +
                "                          AND f3.id = idr.fld_id\n" +
                "                          AND s3.idx_lvl = 1\n" +
                "                          AND s3.instt_category_id = ind.instt_category_id)\n" +
                "               ), 0) AS \"NA\"\n" +
                "FROM tb_rev_dw_fact_idx_result idr\n" +
                "         LEFT OUTER JOIN tb_rev_evl_fld f\n" +
                "                         ON idr.fld_id = f.id\n" +
                "         LEFT OUTER JOIN tb_rev_instt i ON idr.instt_id = i.id\n" +
                "         LEFT OUTER JOIN tb_rev_instt_detail ind ON i.id = ind.instt_id AND ind.evl_yr = :year\n" +
                "WHERE idr.evl_yr = :year\n" +
                "GROUP BY idr.instt_id, idr.fld_id;";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("year", year);
        List<Object[]> objects = query.getResultList();
        List<FieldScoreResultVo> results = objects.stream()
                .map(result -> {
                    String name = (String) result[0];
                    BigDecimal score = (BigDecimal) result[1];
                    BigDecimal naScore = (BigDecimal) result[3];
                    Float fScore = Objects.isNull(score) ? null : score.floatValue();
                    Float standardScore = fScore;
                    if (!name.contains("기타")) {
                        standardScore = fScore + naScore.floatValue();
                    }
                    Long institutionId = ((BigInteger) result[2]).longValue();
                    return new FieldScoreResultVo(name, fScore, standardScore, institutionId);
                }).collect(Collectors.toList());

        return results;
    }

    public List<IndexScoreResultVo> findAllRankingByCategoryAndIndexAndYear(String categoryCode, String indexId, String year) {
        String sql = "SELECT category_nm,\n" +
                "       instt_nm,\n" +
                "       fld_nm,\n" +
                "       idx_nm,\n" +
                "       NVL(scr, 0) + \"NA\",\n" +
                "       RANK() OVER(ORDER BY NVL(scr,0) + \"NA\" DESC) AS rank\n" +
                "FROM (SELECT c.category_nm,\n" +
                "             i.instt_nm,\n" +
                "             f.fld_nm,\n" +
                "             id.idx_nm,\n" +
                "             idr.scr,\n" +
                "             NVL(((SELECT SUM(r.scr)\n" +
                "                   FROM tb_rev_evl_result r\n" +
                "                            LEFT OUTER JOIN tb_rev_evl_idx i ON r.idx_id = i.id\n" +
                "                            LEFT OUTER JOIN tb_rev_evl_fld fld ON i.fld_id = fld.id\n" +
                "                   WHERE r.evl_yr = :year\n" +
                "                     AND fld.evl_yr = :year\n" +
                "                     AND fld.fld_nm NOT LIKE '%기타%'\n" +
                "                     AND r.instt_id = idr.instt_id)\n" +
                "                      /\n" +
                "                  ((SELECT SUM(fld_scr)\n" +
                "                    FROM tb_rev_evl_fld fld\n" +
                "                    WHERE fld.evl_yr = :year\n" +
                "                      AND fld.fld_nm NOT LIKE '%기타%')\n" +
                "                      -\n" +
                "                   (SELECT SUM(s.idx_scr)\n" +
                "                    FROM tb_rev_evl_idx i\n" +
                "                             LEFT OUTER JOIN tb_rev_evl_fld fld\n" +
                "                                             ON i.fld_id = fld.id\n" +
                "                             LEFT OUTER JOIN tb_rev_evl_scr s ON s.idx_id = i.id\n" +
                "                    WHERE i.na_yn = 'Y'\n" +
                "                      AND s.idx_lvl = 1\n" +
                "                      AND fld.evl_yr = :year\n" +
                "                      AND s.instt_category_id = c.id))\n" +
                "                 *\n" +
                "                  (SELECT SUM(s.idx_scr)\n" +
                "                   FROM tb_rev_evl_idx i\n" +
                "                            LEFT OUTER JOIN tb_rev_evl_fld fld ON i.fld_id = fld.id\n" +
                "                            LEFT OUTER JOIN tb_rev_evl_scr s ON s.idx_id = i.id\n" +
                "                   WHERE i.na_yn = 'Y'\n" +
                "                     AND s.idx_lvl = 1\n" +
                "                     AND fld.evl_yr = :year\n" +
                "                     AND instt_category_id = c.id\n" +
                "                   GROUP BY fld.id\n" +
                "                   HAVING fld.id = idr.fld_id)\n" +
                "                     ), 0) AS \"NA\"\n" +
                "      FROM tb_rev_dw_fact_idx_result idr\n" +
                "               LEFT OUTER JOIN tb_rev_evl_fld f ON idr.fld_id = f.id\n" +
                "               LEFT OUTER JOIN tb_rev_dw_fact_instt_result ir ON ir.instt_id = idr.instt_id\n" +
                "               LEFT OUTER JOIN tb_rev_instt i ON ir.instt_id = i.id\n" +
                "               LEFT OUTER JOIN tb_rev_instt_detail ind ON i.id = ind.instt_id AND ind.evl_yr = :year\n" +
                "               LEFT OUTER JOIN tb_rev_instt_category c ON ind.instt_category_id = c.id\n" +
                "               LEFT OUTER JOIN tb_rev_evl_idx id ON idr.idx_id = id.id\n" +
                "      WHERE idr.evl_yr = :year\n" +
                "        AND idr.idx_id = :indexId";
        if (!StringUtils.isNullOrEmpty(categoryCode)) {
            sql += "\n  AND c.category_cd = :categoryCode";
        }
        sql += "\n GROUP BY ir.instt_id)";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("year", year);
        query.setParameter("indexId", indexId);
        if (!StringUtils.isNullOrEmpty(categoryCode)) {
            query.setParameter("categoryCode", categoryCode);
        }
        List<Object[]> objects = query.getResultList();
        List<IndexScoreResultVo> results = objects.stream()
                .map(result -> {
                    String category = (String) result[0];
                    String institution = (String) result[1];
                    String field = (String) result[2];
                    String index = (String) result[3];
                    BigDecimal score = (BigDecimal) result[4];
                    Float fScore = Objects.isNull(score) ? null : score.floatValue();
                    Integer ranking = (Integer) result[5];
                    return new IndexScoreResultVo(
                            category,
                            institution,
                            field,
                            index,
                            fScore,
                            ranking);
                }).collect(Collectors.toList());

        return results;
    }

    public List<IndexResultFact> findAllIndexResultByInstitutionIds(List<Long> ids, String year) {
        return this.indexResultFactRepository.findAllByInstitutionIdAndYear(ids, year);
    }

    public List<IndexResultFact> findAllIndexResultByYear(String year) {
        return this.indexResultFactRepository.findAllByYear(year);
    }

    @Transactional
    public void deleteInstitutionResult(InstitutionResultFact fact) {
        this.institutionResultFactRepository.delete(fact);
    }

    @Transactional
    public void deleteOfInstitutionResultByInstitutionIdAndYear(Long institutionId, String year) {
        this.institutionResultFactRepository.deleteAllByInstitutionIdAndYear(institutionId, year);
    }

    @Transactional
    public void saveOfInstitutionResult(InstitutionResultFact fact) {
        this.institutionResultFactRepository.save(fact);
    }

    @Transactional
    public void saveAllOfInstitutionResult(List<InstitutionResultFact> facts) {
        this.institutionResultFactRepository.saveAll(facts);
    }

    @Transactional
    public void saveAllOfIndexResult(List<IndexResultFact> facts) {
        this.indexResultFactRepository.saveAll(facts);
    }

    @Transactional
    public void saveAllOfIndexResult(List<IndexResultFact> facts, EvaluationStatus status, String year) {
        this.saveAllOfIndexResult(facts);
        List<Long> institutionIds = facts.stream().map(IndexResultFact::getInstitutionId).distinct().collect(Collectors.toList());
        this.institutionResultProcess(institutionIds, status, year);
    }

    @Transactional
    public void bulkUpdateOfInstitutionResultProcessStatusByYearAndInsttIdList(String year, List<Long> idList, String status) {
        institutionResultFactRepository.bulkUpdateOfInstitutionResultProcessStatusByYearAndInsttIdList(year, idList, status);
    }

    @Transactional
    public void deleteAllOfIndexResultByIndexId(Long indexId) {
        this.indexResultFactRepository.deleteAllByIndexId(indexId);
    }

    @Transactional
    public void deleteAllOfIndexResultByInstitutionIdAndYear(Long institutionId, String year) {
        this.indexResultFactRepository.deleteAllByInstitutionIdAndYear(institutionId, year);
    }

    private void institutionResultProcess(List<Long> institutionIds, EvaluationStatus status, String year) {
        List<IndexResultFact> indexFacts = this.findAllIndexResultByInstitutionIds(institutionIds, year);
        if (indexFacts.isEmpty()) {
            return;
        }

        List<InstitutionResultFact> institutionResults = new ArrayList<>();
        Map<Long, Float> fieldNaScores = new LinkedHashMap<>();
        Float fieldTotalScore = 0.0F;

        List<EvaluationField> fields = this.evaluationFieldService.findByYear(year, Sort.by(Sort.Direction.ASC, "no"));
        EvaluationField etcField = fields.stream().filter(field -> field.getName().contains("기타")).findFirst().orElse(null);
        for (EvaluationField field : fields) {
            Long fieldId = field.getId();
            if (!field.getName().contains("기타")) {
                fieldTotalScore += field.getScore();
                fieldNaScores.put(fieldId, fieldNaScores.getOrDefault(fieldId, 0.0F));
            }
        }

        for (Long institutionId : institutionIds) {
            Float totalScore = 0.0F;
            Float extraScore = 0.0F;
            Optional<InstitutionResultFact> institutionOptional = this.findInstitutionResultByInstitutionIdAndYear(institutionId, year);
            if (institutionOptional.isPresent()) {
                InstitutionResultFact institutionResult = institutionOptional.get();
                List<IndexResultFact> filteredFacts = indexFacts.stream().filter(fact -> fact.getInstitutionId().equals(institutionId)).collect(Collectors.toList());
                for (IndexResultFact fact : filteredFacts) {
                    if (Objects.isNull(fact.getScore())) {
                        Institution institution = this.institutionService.findById(institutionId);
                        Long categoryId = institution.getCategoryByYear(year).getId();
                        Long indexId = fact.getIndexId();
                        Long fieldId = fact.getFieldId();
                        Optional<EvaluationScore> evaluationScore = this.evaluationScoreService.findByIndexIdAndCategoryIdAndLevel(indexId, categoryId, 1);
                        if (evaluationScore.isPresent()) {
                            Float naScore = evaluationScore.get().getScore();
                            fieldNaScores.put(fieldId, fieldNaScores.getOrDefault(fieldId, 0.0F) + naScore);
                        }
                    } else {
                        if (!Objects.isNull(etcField) && fact.getFieldId().equals(etcField.getId())) {
                            extraScore += fact.getScore();
                        } else {
                            totalScore += fact.getScore();
                        }
                    }
                }
                Float standardTotalScore = totalScore;
                Float fieldTotalNaScore = fieldNaScores.values().stream().reduce(0.0F, Float::sum);
                for (Long fieldId : fieldNaScores.keySet()) {
                    Float naScore = totalScore / (fieldTotalScore - fieldTotalNaScore) * fieldNaScores.get(fieldId);
                    standardTotalScore += naScore;
                }

                institutionResult.setProcessStatus(status);
                institutionResult.setScore(standardTotalScore);
                institutionResult.setExtraScore(extraScore);
                institutionResults.add(institutionResult);
            }
        }

        this.saveAllOfInstitutionResult(institutionResults);
    }

    public Map<String, Integer> getProgressStatistic(String year) {
        List<InstitutionResultFact> institutions = this.findAllInstitutionResultByYear(year);
        Map<String, Integer> statusMap = new LinkedHashMap<>();
        for (EvaluationStatus value : EvaluationStatus.values()) {
            statusMap.put(value.getTitle(), 0);
        }
        for (InstitutionResultFact institution : institutions) {
            String status = institution.getProcessStatus().getTitle();
            statusMap.put(status, statusMap.getOrDefault(status, 0) + 1);
        }
        return statusMap;
    }

    public void remapInstitutionResultFact(Institution newInstt, Institution beforeInstt) {
        institutionResultFactRepository.updateByBeforeInstt(newInstt.getId(), beforeInstt.getId());
    }

    public void remapIndexResultFact(Institution newInstt, Institution beforeInstt) {
        indexResultFactRepository.updateByBeforeInstt(newInstt.getId(), beforeInstt.getId());
    }
}
