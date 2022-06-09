package kr.co.data_status_evaluation.service;


import kr.co.data_status_evaluation.model.Institution;
import kr.co.data_status_evaluation.model.InstitutionCategory;
import kr.co.data_status_evaluation.model.dto.ScoreSummaryByCategoryDto;
import kr.co.data_status_evaluation.model.search.InstitutionSearchParam;
import kr.co.data_status_evaluation.model.search.SearchParam;
import kr.co.data_status_evaluation.model.search.StatisticSearchParam;
import kr.co.data_status_evaluation.model.vo.LmsInstitutionVo;
import kr.co.data_status_evaluation.repository.InstitutionCategoryRepository;
import kr.co.data_status_evaluation.repository.InstitutionRepository;
import kr.co.data_status_evaluation.specification.InstitutionSpecification;
import kr.co.data_status_evaluation.specification.StatisticSpecification;
import kr.co.data_status_evaluation.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.InvalidParameterException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class InstitutionService {

    private final InstitutionRepository institutionRepository;
    private final InstitutionCategoryRepository institutionCategoryRepository;
    private final EntityManager entityManager;
//    private final FactService factService;
//    private final EvaluationFieldService evaluationFieldService;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public Institution findByIdAndYear(Long id, String year) {
        return institutionRepository.findByIdAndYear(id, year).orElseThrow(InvalidParameterException::new);
    }

    public Institution findById(Long id) {
        return institutionRepository.findById(id).orElseThrow(InvalidParameterException::new);
    }

    public Optional<Institution> findByName(String name) {
        return institutionRepository.findByName(name);
    }

    public Institution findByCode(String insttCd) {
        return institutionRepository.findByCode(insttCd);
    }

    public Page<Institution> findAll(Pageable pageable, String code, String name) {
        return institutionRepository.findAllByCodeContainingAndNameContaining(pageable, code, name);
    }

    public Page<Institution> findAll(Pageable pageable) {
        return institutionRepository.findAll(pageable);
    }

    public List<Institution> findAll(Sort sort) {
        return institutionRepository.findAll(sort);
    }

    public List<Institution> findAll() {
        return this.institutionRepository.findAllByDeletedAndCategoryIsNotNull("N");
    }

    public List<Institution> findAllByTarget() {
        return this.institutionRepository.findAllByTarget();
    }

    public List<Institution> findAllByTarget(String year) {
        return this.institutionRepository.findAllByTarget(year);
    }

    public List<Institution> findAllByTargetAndCategoryIdAndYear(Long categoryId, String year) {
        return this.institutionRepository.findAllByTargetAndCategoryIdAndYear(categoryId, year);
    }

    public Page<Institution> findBySearchParam(Pageable pageable, SearchParam searchParam) {
        Specification specification;
        if (searchParam instanceof InstitutionSearchParam) {
            specification = Specification.where(InstitutionSpecification.search((InstitutionSearchParam) searchParam));
        } else {
            specification = Specification.where(StatisticSpecification.search((StatisticSearchParam) searchParam));
        }
        return institutionRepository.findAll(specification, pageable);
    }


    public Institution save(Institution institution) {
        return institutionRepository.save(institution);
    }

    public void saveAll(List<Institution> institutionList) {
        if (institutionList.size() > 0) {
            this.institutionRepository.saveAll(institutionList);
        }
    }

    public Institution deleteByCode(String insttCd) {
        Institution institution = institutionRepository.findFirstByCode(insttCd);
        if (institution != null)
            institutionRepository.delete(institution);
        return institution;
    }

    @Transactional
    public String upsert(LmsInstitutionVo vo) throws Exception {
        String processType = "Institution: ";
        Institution institution = institutionRepository.findByCodeRegardlessOfDelYn(vo.getInsttCd());
        if (institution != null) { // UPDATE
            processType += "UPDATE |";
            institution.setFromLmsVoExceptType(vo);
        } else { // INSERT
            processType += "INSERT |";
            institution = new Institution(vo);
            if (!Objects.isNull(vo.getStatusInsttType())) {
                String type = vo.getStatusInsttType();
                institution.setType(type);
                InstitutionCategory category = institutionCategoryRepository.findByCode(type);
                institution.setCategory(category);
            }
        }
        institutionRepository.save(institution);
        return processType;
    }

    public void deleteAllByCode(String code) {
        institutionRepository.deleteAllByCode(code);
    }

    @Transactional
    public void upsertAll(List<LmsInstitutionVo> voListForUpsert, List<String> insttCdListForUpsert) throws Exception {
        // 모든 InstitutionCategory
        List<InstitutionCategory> allInsttCategories = institutionCategoryRepository.findAll();
        // UPDATE 대상 Institution
        List<Institution> institutionListForUpdate = institutionRepository.findAllByInsttCdsRegardlessOfDelYn(insttCdListForUpsert);
        Map<String, Institution> institutionMapForUpdate = new HashMap<>();
        if (institutionListForUpdate != null && !institutionListForUpdate.isEmpty()) {
            for (Institution institution : institutionListForUpdate)
                institutionMapForUpdate.put(institution.getCode().trim(), institution);
        }
        // saveAll
        List<Institution> institutionListForSave = new ArrayList<>();
        for (LmsInstitutionVo vo : voListForUpsert) {
            Institution institution = institutionMapForUpdate.getOrDefault(vo.getInsttCd(), null);
            if (institution == null) { // INSERT
                institution = new Institution(vo);
            } else { // UPDATE
                institution.setFromLmsVo(vo);
            }
            // Institution Type, Category Id 설정
            if (vo.getStatusInsttType() != null) {
                String institutionType = vo.getStatusInsttType();
                institution.setType(institutionType);
                for (InstitutionCategory category : allInsttCategories) {
                    if (institutionType.trim().equals(category.getCode().trim())) {
                        institution.setCategory(category);
                        break;
                    }
                }
            }
            institutionListForSave.add(institution);
        }
        institutionRepository.saveAll(institutionListForSave);
    }

    public Map<String, ScoreSummaryByCategoryDto> getScoreSummaryByCategory(Long categoryId, String year, Long institutionId) {
        String sql = "SELECT t1.fld_nm,\n" +
                "       SUM(t1.total + t1.NA),\n" +
                "       MIN(t1.total + t1.NA) / t1.fld_scr * 100,\n" +
                "       MAX(t1.total + t1.NA) / t1.fld_scr * 100,\n" +
                "       AVG(t1.total + t1.NA) / t1.fld_scr * 100,\n" +
                "       COUNT(instt_id),\n" +
                "       t1.fld_scr\n" +
                "FROM (SELECT f.id                AS fld_id,\n" +
                "             f.fld_nm            AS fld_nm,\n" +
                "             f.fld_scr           AS fld_scr,\n" +
                "             SUM(idr.scr)        AS total,\n" +
                "             idr.instt_id        AS instt_id,\n" +
                "             trid.instt_category_id AS instt_category_id,\n" +
                "             NVL((\n" +
                "                             (SELECT SUM(r1.scr)\n" +
                "                              FROM tb_rev_evl_result r1\n" +
                "                                       LEFT OUTER JOIN tb_rev_evl_idx i1 ON r1.idx_id = i1.id\n" +
                "                                       LEFT OUTER JOIN tb_rev_evl_fld f1 ON i1.fld_id = f1.id\n" +
                "                              WHERE r1.instt_id = idr.instt_id\n" +
                "                                AND r1.evl_yr = :year\n" +
                "                                AND f1.fld_nm NOT LIKE '%기타%')\n" +
                "                             /\n" +
                "                             ((SELECT SUM(fld_scr)\n" +
                "                               FROM tb_rev_evl_fld\n" +
                "                               WHERE evl_yr = :year\n" +
                "                                 AND fld_nm NOT LIKE '%기타%')\n" +
                "                                 -\n" +
                "                              (SELECT SUM(s2.idx_scr)\n" +
                "                               FROM tb_rev_evl_result r2\n" +
                "                                        LEFT OUTER JOIN tb_rev_evl_idx i2 ON r2.idx_id = i2.id\n" +
                "                                        LEFT OUTER JOIN tb_rev_evl_scr s2 ON s2.idx_id = i2.id\n" +
                "                               WHERE r2.evl_yr = :year\n" +
                "                                 AND r2.scr IS NULL\n" +
                "                                 AND r2.instt_id = idr.instt_id\n" +
                "                                 AND s2.idx_lvl = 1\n" +
                "                                 AND s2.instt_category_id = :categoryId))\n" +
                "                         *\n" +
                "                             (SELECT SUM(s3.idx_scr)\n" +
                "                              FROM tb_rev_evl_result r3\n" +
                "                                       LEFT OUTER JOIN tb_rev_evl_idx i3 ON r3.idx_id = i3.id\n" +
                "                                       LEFT OUTER JOIN tb_rev_evl_fld f3 ON i3.fld_id = f3.id\n" +
                "                                       LEFT OUTER JOIN tb_rev_evl_scr s3 ON s3.idx_id = i3.id\n" +
                "                              WHERE r3.evl_yr = :year\n" +
                "                                AND r3.scr IS NULL\n" +
                "                                AND r3.instt_id = idr.instt_id\n" +
                "                                AND f3.id = idr.fld_id\n" +
                "                                AND s3.idx_lvl = 1\n" +
                "                                AND s3.instt_category_id = :categoryId)\n" +
                "                     ), 0)       AS \"NA\"\n" +
                "      FROM tb_rev_dw_fact_idx_result idr\n" +
                "               LEFT OUTER JOIN tb_rev_evl_fld f\n" +
                "                               ON idr.fld_id = f.id\n" +
                "               LEFT OUTER JOIN tb_rev_instt i ON idr.instt_id = i.id\n" +
                "               LEFT OUTER JOIN tb_rev_instt_detail trid ON i.id = trid.instt_id AND trid.evl_yr = :year\n" +
                "      WHERE idr.evl_yr = :year\n" +
                "      GROUP BY idr.instt_id, idr.fld_id) t1\n" +
                "WHERE t1.instt_category_id = :categoryId\n";
        if (!Objects.isNull(institutionId)) {
            sql += "AND t1.instt_id = :institutionId\n";
        }
        sql += "GROUP BY t1.fld_id;";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("categoryId", categoryId);
        query.setParameter("year", year);
        if (!Objects.isNull(institutionId)) {
            query.setParameter("institutionId", institutionId);
        }
        List<Object[]> objects = query.getResultList();
        Map<String, ScoreSummaryByCategoryDto> results = new HashMap<>();
        objects.forEach(result -> {
            String fieldName = (String) result[0];
            BigDecimal sum = (BigDecimal) result[1];
            Float fSum = Objects.isNull(sum) ? null : sum.floatValue();
            BigDecimal min = (BigDecimal) result[2];
            Float fMin = Objects.isNull(min) ? null : min.floatValue();
            BigDecimal max = (BigDecimal) result[3];
            Float fMax = Objects.isNull(max) ? null : max.floatValue();
            Double avg = (Double) result[4];
            Integer cnt = (Integer) result[5];
            BigDecimal fieldScore = (BigDecimal) result[6];

            results.put(fieldName, new ScoreSummaryByCategoryDto(fSum, fMin, fMax, avg, cnt, fieldScore));
        });

        return results;
    }

    public Map<String, Long> getInsttCdToInsttIdMap() {
        String sql = "SELECT  instt_cd, id\n" +
                "FROM    tb_rev_instt\n" +
                "WHERE   del_yn = 'N'\n";
        Query query = entityManager.createNativeQuery(sql);
        List<Object[]> objects = query.getResultList();
        Map<String, Long> results = new HashMap<>();
        objects.forEach(result -> {
            String insttCd = (String) result[0];
            BigInteger insttIdBigInteger = (BigInteger) result[1];
            insttCd = StringUtils.isNullOrEmpty(insttCd) ? null : insttCd.trim().toUpperCase();
            Long insttId = Objects.isNull(insttIdBigInteger) ? null : insttIdBigInteger.longValue();

            results.put(insttCd, insttId);
        });

        return results;
    }

    public Map<BigInteger, Map<String, Float>> getDistributionByField(Long categoryId, String year) {
        String sql = "SELECT f.fld_nm             AS field_name,\n" +
                "       NVL(SUM(r.scr), 0) AS total,\n" +
                "       r.instt_id\n" +
                "FROM tb_rev_evl_fld f\n" +
                "         LEFT OUTER JOIN tb_rev_evl_idx i ON i.fld_id = f.id\n" +
                "         LEFT OUTER JOIN tb_rev_evl_result r ON r.idx_id = i.id\n" +
                "WHERE r.instt_id in (SELECT distinct ins.id\n" +
                "                       FROM tb_rev_instt_category c\n" +
                "                              JOIN tb_rev_instt_detail id ON c.id = id.instt_category_id AND id.evl_yr = :year\n" +
                "                              JOIN tb_rev_instt ins ON ins.id = id.instt_id\n" +
//                "                              JOIN tb_rev_instt ins ON c.id = ins.instt_category_id\n" +
                "                              JOIN tb_rev_evl_trgt_instt ti ON ins.id = ti.instt_id\n" +
                "                     WHERE ins.del_yn = 'N'\n" +
                "                       AND ti.trgt_instt_yn = 'Y'\n" +
                "                       AND c.id = :categoryId" +
                "                       AND ti.evl_yr = :year)\n" +
                "AND f.evl_yr = :year\n" +
                "GROUP BY f.id, r.instt_id";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("categoryId", categoryId);
        query.setParameter("year", year);
        List<Object[]> objects = query.getResultList();
        Map<BigInteger, Map<String, Float>> results = new HashMap<>();

        objects.forEach(result -> {
            String fieldName = (String) result[0];
            BigDecimal sum = (BigDecimal) result[1];
            Float fSum = Objects.isNull(sum) ? null : sum.floatValue();
            BigInteger institutionId = (BigInteger) result[2];

            Map<String, Float> temp = new HashMap<>();
            if (results.getOrDefault(institutionId, null) != null) {
                temp = results.get(institutionId);
            }
            temp.put(fieldName, fSum);
            results.put(institutionId, temp);
        });

        return results;
    }

    public Map<BigInteger, Float> getDistributionByScore(Long categoryId, String year) {
        String sql = "SELECT NVL(SUM(r.scr), 0), r.instt_id\n" +
                "FROM tb_rev_instt_category c\n" +
//                "         JOIN tb_rev_instt ins ON c.id = ins.instt_category_id\n" +
                "         JOIN tb_rev_instt_detail id ON c.id = id.instt_category_id AND id.evl_yr = :year\n" +
                "         JOIN tb_rev_instt ins ON ins.id = id.instt_id\n" +
                "         JOIN tb_rev_evl_trgt_instt ti ON ins.id = ti.instt_id\n" +
                "         JOIN tb_rev_evl_result r ON ins.id = r.instt_id\n" +
                "         JOIN tb_rev_evl_idx i ON r.idx_id = i.id\n" +
                "         JOIN tb_rev_evl_fld f ON f.id = i.fld_id\n" +
                "WHERE ins.del_yn = 'N'\n" +
                "  AND ti.trgt_instt_yn = 'Y'\n" +
                "  AND c.id = :categoryId\n" +
                "  AND ti.evl_yr = :year\n" +
                "  AND r.evl_yr = :year\n" +
                "  AND i.evl_ty IS NOT NULL\n" +
                "GROUP BY r.instt_id";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("categoryId", categoryId);
        query.setParameter("year", year);
        List<Object[]> objects = query.getResultList();
        Map<BigInteger, Float> results = new HashMap<>();
        objects.forEach(result -> {
            BigDecimal sum = (BigDecimal) result[0];
            Float fSum = Objects.isNull(sum) ? null : sum.floatValue();
            BigInteger institutionId = (BigInteger) result[1];
            results.put(institutionId, fSum);
        });

        return results;
    }

    private Float convertToStandardScore(Float score, BigDecimal fieldScore) {
        return score * 100 / fieldScore.floatValue();
    }

    private Double convertToStandardScore(Double score, BigDecimal fieldScore) {
        return score * 100 / fieldScore.doubleValue();
    }

    public List<Institution> findAllBySearchParamNativeQuery(InstitutionSearchParam searchParam) {
        String sql = "SELECT  DISTINCT ind.instt_ty, i.instt_cd, i.instt_nm\n" +
                "FROM    tb_rev_evl_trgt_instt eti\n" +
                "        INNER JOIN  tb_rev_instt i\n" +
                "            ON      eti.evl_yr = :year\n" +
                "            AND     eti.trgt_instt_yn = 'Y'\n" +
                "            AND     eti.instt_id = i.id\n";
        if (!StringUtils.isNullOrEmpty(searchParam.getType()))
            sql += "            AND     i.instt_ty = :insttTy\n";
        if (!StringUtils.isNullOrEmpty(searchParam.getCode()))
            sql += "            AND     UPPER(i.instt_cd) LIKE '%'||UPPER(:code)||'%'\n";
        if (!StringUtils.isNullOrEmpty(searchParam.getName()))
            sql += "            AND     UPPER(i.instt_nm) LIKE '%'||UPPER(:name)||'%'\n";
        sql += "        INNER JOIN tb_rev_instt_detail ind ON i.id = ind.instt_id AND ind.evl_yr = :year\n";
        sql += "        INNER JOIN  tb_rev_instt_category ic\n" +
                "            ON      ind.instt_category_id = ic.id\n";
        if (!StringUtils.isNullOrEmpty(searchParam.getStatus())) {
            sql += "WHERE   NOT EXISTS  (\n" +
                    "                    SELECT  1\n" +
                    "                    FROM    (\n" +
                    "                            SELECT  DISTINCT instt_cd, evl_yr\n" +
                    "                            FROM    tb_rev_log\n" +
                    "                            WHERE   evl_yr = :year\n" +
                    "                            AND     actn_nm = :actionName\n" +
                    "                            ) log\n" +
                    "                    WHERE   eti.evl_yr = log.evl_yr\n" +
                    "                    AND     i.instt_cd = log.instt_cd\n" +
                    "                    )";
        }


        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("year", searchParam.getYear());
        if (!StringUtils.isNullOrEmpty(searchParam.getType())) query.setParameter("insttTy", searchParam.getType());
        if (!StringUtils.isNullOrEmpty(searchParam.getCode())) query.setParameter("code", searchParam.getCode());
        if (!StringUtils.isNullOrEmpty(searchParam.getName())) query.setParameter("name", searchParam.getName());
        if (!StringUtils.isNullOrEmpty(searchParam.getStatus())) {
            if (searchParam.getStatus().equals("noLogin"))
                query.setParameter("actionName", "로그인");
            else
                query.setParameter("actionName", "실태평가 파일 등록");
        }

        List<Object[]> objects = query.getResultList();
        List<Institution> results = new ArrayList<>();

        objects.forEach(result -> {
            String insttTy = (String) result[0];
            String insttCd = (String) result[1];
            String insttNm = (String) result[2];

            Institution institution = new Institution();
            institution.setType(insttTy);
            institution.setCode(insttCd);
            institution.setName(insttNm);

            results.add(institution);
        });

        return results;
    }

    public ByteArrayInputStream statusNoneDatabaseToExcel(InstitutionSearchParam searchParam) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        String sheetNm = searchParam.getYear() + "년_";
        sheetNm += "noLogin".equals(searchParam.getStatus()) ? "미접속_기관" : "미등록_기관";

        Sheet sheet1 = workbook.createSheet(sheetNm);
        Row headerRow1 = sheet1.createRow(0);

        String[] headers1 = {"번호", "평가년도", "기관유형", "기관코드", "기관명"};
        for (int col = 0; col < headers1.length; col++) {
            Cell cell = headerRow1.createCell(col);
            cell.setCellValue(headers1[col]);
        }

        List<Institution> institutions = this.findAllBySearchParamNativeQuery(searchParam);

        int rowIdx1 = 1;
        List<InstitutionCategory> categoryList = institutionCategoryRepository.findAll();
        Map<String, String> categoryMap = new HashMap<>();
        for (InstitutionCategory category : categoryList) {
            categoryMap.put(category.getCode(), category.getName());
        }
        for (Institution institution : institutions) {
            Row row = sheet1.createRow(rowIdx1);

            row.createCell(0).setCellValue(rowIdx1);
            row.createCell(1).setCellValue(searchParam.getYear());
            row.createCell(2).setCellValue(categoryMap.get(institution.getCode()));
            row.createCell(3).setCellValue(institution.getCode());
            row.createCell(4).setCellValue(institution.getName());

            rowIdx1++;
        }

        workbook.write(out);

        return new ByteArrayInputStream(out.toByteArray());
    }

//    public ByteArrayInputStream evaluationRankDatabaseToExcel(StatisticSearchParam searchParam) throws IOException {
//        Workbook workbook = new XSSFWorkbook();
//        ByteArrayOutputStream out = new ByteArrayOutputStream();
//
//        Sheet sheet1 = workbook.createSheet("평가순위");
//        Row headerRow1 = sheet1.createRow(0);
//
//        String[] headers1 = {"번호","기관유형", "기관명", "평가영역", "평가지표명", "평가점수", "평가순위"};
//        for (int col = 0; col < headers1.length; col++) {
//            Cell cell = headerRow1.createCell(col);
//            cell.setCellValue(headers1[col]);
//        }
//
//        // 기관유형 검색조건
//        String categoryCode = searchParam.getType() == null ? null : searchParam.getType().name();
//        String currentYear = searchParam.getYear();
//        List<IndexScoreResultVo> indexScores = this.factService.findAllRankingByCategoryAndIndexAndYear(categoryCode, searchParam.getIndex(), currentYear);
//
//        int rowIdx1 = 1;
//        for (IndexScoreResultVo indexScore : indexScores) {
//            Row row = sheet1.createRow(rowIdx1);
//
//            row.createCell(0).setCellValue(rowIdx1);
//            row.createCell(1).setCellValue(indexScore.getCategory());
//            row.createCell(2).setCellValue(indexScore.getInstitution());
//            row.createCell(3).setCellValue(indexScore.getField());
//            row.createCell(4).setCellValue(indexScore.getIndex());
//            row.createCell(5).setCellValue(indexScore.getScore());
//            row.createCell(6).setCellValue(indexScore.getRanking());
//
//            rowIdx1++;
//        }
//
//        workbook.write(out);
//
//        return new ByteArrayInputStream(out.toByteArray());
//    }
//
//    public ByteArrayInputStream insttTypeEvaluationRankDatabaseToExcel(StatisticSearchParam searchParam) throws IOException {
//        Workbook workbook = new XSSFWorkbook();
//        ByteArrayOutputStream out = new ByteArrayOutputStream();
//
//        Sheet sheet1 = workbook.createSheet("평가순위");
//        Row headerRow1 = sheet1.createRow(0);
//
//        String[] headers1 = {"번호", "기관유형", "기관명", "평가진행상태", "환산전 점수", "환산후 점수", "관리체계", "개방", "활용", "품질", "기타(가감점)", "평가순위"};
//        for (int col = 0; col < headers1.length; col++) {
//            Cell cell = headerRow1.createCell(col);
//            cell.setCellValue(headers1[col]);
//        }
//
//        // 기관유형 검색조건
//        String categoryCode = searchParam.getType() == null ? null : searchParam.getType().name();
//        String currentYear = searchParam.getYear();
//        // 기관평가결과(fact) 리스트
//        List<InstitutionResultVo> institutions = this.factService.findAllInstitutionResultByCategoryAndYear(categoryCode, currentYear);
//
//        // 평가영역별 환산점수 리스트
//        List<FieldScoreResultVo> fieldScores = this.factService.findAllOfFieldScoreByYear(currentYear);
//        // 평가영역을 key로 기관별 평가영역 환산점수 매핑
//        Map<String, Map<Long, Float>> fieldScoreMap = new HashMap<>();
//        fieldScores.forEach(fieldScore -> {
//            String fieldName = fieldScore.getFieldName();
//            Long institutionId = fieldScore.getInstitutionId();
//            Float score = fieldScore.getStandardScore();
//            Map<Long, Float> map = fieldScoreMap.getOrDefault(fieldName, new HashMap<>());
//            map.put(institutionId, score);
//            fieldScoreMap.put(fieldName, map);
//        });
//
//        List<EvaluationField> fields = this.evaluationFieldService.findAllByYear(currentYear);
//        // 평가영역별 지표 리스트 매핑
//        Map<String, List<EvaluationIndexVo>> indexMap = new HashMap<>();
//        fields.forEach(field -> {
//            indexMap.put(field.getName(), field.getEvaluationIndices().stream().map(index -> {
//                EvaluationIndexVo indexVo = new EvaluationIndexVo();
//                indexVo.setId(index.getId());
//                indexVo.setName(index.getName());
//                return indexVo;
//            }).collect(Collectors.toList()));
//        });
//
//        int rowIdx1 = 1;
//        int num = 6;
//        for (InstitutionResultVo institution : institutions) {
//            Row row = sheet1.createRow(rowIdx1);
//
//            row.createCell(0).setCellValue(rowIdx1);
//            row.createCell(1).setCellValue(institution.getCategory());
//            row.createCell(2).setCellValue(institution.getName());
//            row.createCell(3).setCellValue(institution.getProcessStatus().getTitle());
//            row.createCell(4).setCellValue(institution.getScore());
//            row.createCell(5).setCellValue(institution.getStandardScore());
//            for (EvaluationField field : fields) {
//                if(field.getName().equals("관리체계"))
//                    num = 6;
//                if(fieldScoreMap.get(field.getName()) != null && fieldScoreMap.get(field.getName()).get(institution.getId()) != null) {
//                    row.createCell(num++).setCellValue(fieldScoreMap.get(field.getName()) != null ? fieldScoreMap.get(field.getName()).get(institution.getId()).toString() : "0.0");
//                }
//            }
//            row.createCell(11).setCellValue(institution.getRanking());
//            rowIdx1++;
//        }
//
//        workbook.write(out);
//
//        return new ByteArrayInputStream(out.toByteArray());
//    }
}
