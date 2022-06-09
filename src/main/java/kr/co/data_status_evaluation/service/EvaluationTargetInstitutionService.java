package kr.co.data_status_evaluation.service;

import kr.co.data_status_evaluation.model.*;
import kr.co.data_status_evaluation.model.dto.EvaluationTargetInstitutionDto;
import kr.co.data_status_evaluation.model.dw.IndexResultFact;
import kr.co.data_status_evaluation.model.dw.InstitutionResultFact;
import kr.co.data_status_evaluation.model.enums.EvaluationStatus;
import kr.co.data_status_evaluation.model.search.EvaluationTargetInstitutionSearchParam;
import kr.co.data_status_evaluation.model.vo.InsttCategoryCountVo;
import kr.co.data_status_evaluation.repository.EvaluationTargetInstitutionRepository;
import kr.co.data_status_evaluation.repository.InstitutionRepository;
import kr.co.data_status_evaluation.service.dw.FactService;
import kr.co.data_status_evaluation.specification.EvaluationTargetInstitutionSpecification;
import kr.co.data_status_evaluation.util.ExcelUtils;
import kr.co.data_status_evaluation.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EvaluationTargetInstitutionService {

    private final EvaluationTargetInstitutionRepository evaluationTargetInstitutionRepository;
    private final InstitutionCategoryService institutionCategoryService;
    private final InstitutionService institutionService;
    private final InstitutionRepository institutionRepository;
    private final EvaluationResultService evaluationResultService;
    private final EvaluationIndexService evaluationIndexService;
    private final FactService factService;
    private final EntityManager em;
    private final ExcelUtils excelUtils;

    public List<EvaluationTargetInstitution> findAll() {
        return this.evaluationTargetInstitutionRepository.findAll();
    }

    public Optional<EvaluationTargetInstitution> findById(Long id) {
        return this.evaluationTargetInstitutionRepository.findById(id);
    }

    public boolean isTargetInstitutionByYearAndInsttId(String year, Institution institution) {
        boolean result = false;
        Optional<EvaluationTargetInstitution> evaluationTargetInstitution = this.evaluationTargetInstitutionRepository.findAllByYearAndInstitution(year, institution);
        if (evaluationTargetInstitution.isPresent()) {
            result = true;
        }
        return result;
    }

    public void save(EvaluationTargetInstitution target) {
        this.evaluationTargetInstitutionRepository.save(target);
    }

    @Transactional
    public EvaluationTargetInstitution updateTrgtInsttYn(EvaluationTargetInstitutionDto target, List<EvaluationIndex> indices) {
        Institution institution = institutionService.findById(target.getInsttId());
        EvaluationTargetInstitution evaluationTargetInstitution = null;
        if (!Objects.isNull(target) && !Objects.isNull(target.getId())) {
            evaluationTargetInstitution = this.findById(target.getId()).orElse(null);
        }
        if (Objects.isNull(evaluationTargetInstitution)) {
            evaluationTargetInstitution = new EvaluationTargetInstitution();
            evaluationTargetInstitution.setInstitution(institution);
            evaluationTargetInstitution.setYear(target.getYear());
        } else {
            if (target.getTrgtInsttYn().equals(evaluationTargetInstitution.getTrgtInsttYn())) {
                return null;
            }
        }
        evaluationTargetInstitution.setTrgtInsttYn(target.getTrgtInsttYn());

        if ("Y".equals(target.getTrgtInsttYn())) {
            List<EvaluationResult> results = new ArrayList<>();
            List<IndexResultFact> facts = new ArrayList<>();
            for (EvaluationIndex index : indices) {
                EvaluationResult result = new EvaluationResult();
                result.setEvaluationIndex(index);
                result.setInstitution(institution);
                if (index.isAttachFile()) {
                    result.setProcessStatus(EvaluationStatus.NONE);
                } else {
                    result.setProcessStatus(EvaluationStatus.START);
                }
                result.setYear(target.getYear());
                result.setScore("0.0");

                results.add(result);

                Long institutionId = institution.getId();
                Long indexId = index.getId();
                Long fieldId = index.getEvaluationField().getId();
                String year = target.getYear();
                Optional<IndexResultFact> factOptional = this.factService.findIndexResultByInstitutionIdAndIndexIdAndYear(institutionId, indexId, year);
                IndexResultFact fact = factOptional.orElseGet(() -> new IndexResultFact(institutionId, indexId, fieldId, year));
                fact.setScore(Float.parseFloat(result.getScore()));
                facts.add(fact);
            }
            this.evaluationResultService.saveAll(results);
            this.factService.saveAllOfIndexResult(facts, EvaluationStatus.NONE, target.getYear());
        } else {
            List<EvaluationResult> results = evaluationResultService.findAllByInstitutionAndYear(institution, target.getYear());
            evaluationResultService.deleteAll(results);
            this.factService.deleteAllOfIndexResultByInstitutionIdAndYear(target.getInsttId(), target.getYear());
        }

        return evaluationTargetInstitution;
    }

    public List<InsttCategoryCountVo> findAllInsttCategoryCountVoByYear(String year) {
        String sql = "SELECT ic.id, ic.category_cd, ic.category_nm, NVL(res.cnt, 0)\n" +
                "FROM  tb_rev_instt_category ic\n" +
                "      LEFT JOIN (\n" +
                "                SELECT ind.instt_category_id, COUNT(eti.instt_id) as cnt\n" +
                "                FROM tb_rev_evl_trgt_instt eti\n" +
                "                    INNER JOIN tb_rev_instt i\n" +
                "                        ON eti.evl_yr = :year\n" +
                "                        AND i.del_yn = 'N'\n" +
                "                        AND i.instt_category_id IS NOT NULL\n" +
                "                        AND eti.instt_id = i.id\n" +
                "                    INNER JOIN tb_rev_instt_detail ind ON i.id = ind.instt_id AND ind.evl_yr = :year\n" +
                "                GROUP BY ind.instt_category_id\n" +
                "                ) res\n" +
                "          ON ic.id = res.instt_category_id";
        Query query = em.createNativeQuery(sql);
        query.setParameter("year", year);
        List<Object[]> objects = query.getResultList();
        List<InsttCategoryCountVo> results = objects.stream()
                .map(result -> new InsttCategoryCountVo(
                        (BigInteger) result[0],
                        (String) result[1],
                        (String) result[2],
                        (Integer) result[3]
                ))
                .collect(Collectors.toList());

        return results;
    }

    public List<InsttCategoryCountVo> findAllInsttCategoryCountVoByYearAndTrgtYnEqualY(String year) {
        String sql = "SELECT ic.id, ic.category_cd, ic.category_nm, NVL(res.cnt, 0)\n" +
                "FROM  tb_rev_instt_category ic\n" +
                "      LEFT JOIN (\n" +
                "                SELECT ind.instt_category_id, COUNT(eti.instt_id) as cnt\n" +
                "                FROM tb_rev_evl_trgt_instt eti\n" +
                "                    INNER JOIN tb_rev_instt i\n" +
                "                        ON eti.evl_yr = :year\n" +
                "                        AND i.del_yn = 'N'\n" +
                "                        AND i.instt_category_id IS NOT NULL\n" +
                "                        AND eti.trgt_instt_yn = 'Y'\n" + // 추가
                "                        AND eti.instt_id = i.id\n" +
                "                    INNER JOIN tb_rev_instt_detail ind ON i.id = ind.instt_id AND ind.evl_yr = :year\n" +
                "                GROUP BY ind.instt_category_id\n" +
                "                ) res\n" +
                "          ON ic.id = res.instt_category_id";
        Query query = em.createNativeQuery(sql);
        query.setParameter("year", year);
        List<Object[]> objects = query.getResultList();
        List<InsttCategoryCountVo> results = objects.stream()
                .map(result -> new InsttCategoryCountVo(
                        (BigInteger) result[0],
                        (String) result[1],
                        (String) result[2],
                        (Integer) result[3]
                ))
                .collect(Collectors.toList());

        return results;
    }

    public List<EvaluationTargetInstitutionDto> findAllEvaluationTargetInstitutionDtoByYear(String year) {
        String sql = "SELECT eti.evl_yr, ic.category_cd, ic.category_nm, i.instt_cd, i.instt_nm, eti.trgt_instt_yn\n" +
                "FROM tb_rev_evl_trgt_instt eti\n" +
                "         INNER JOIN tb_rev_instt i\n" +
                "                    ON eti.evl_yr = :year\n" +
                "                        AND i.del_yn = 'N'\n" +
                "                        AND i.instt_category_id IS NOT NULL\n" +
                "                        AND eti.instt_id = i.id\n" +
                "         INNER JOIN tb_rev_instt_detail ind ON i.id = ind.instt_id AND ind.evl_yr = :year\n" +
                "         INNER JOIN tb_rev_instt_category ic\n" +
                "                    ON ind.instt_category_id = ic.id";
        Query query = em.createNativeQuery(sql);
        query.setParameter("year", year);
        List<Object[]> objects = query.getResultList();
        List<EvaluationTargetInstitutionDto> results = objects.stream()
                .map(result -> new EvaluationTargetInstitutionDto(
                        (String) result[0],
                        (String) result[1],
                        (String) result[2],
                        (String) result[3],
                        (String) result[4],
                        (Character) result[5]
                ))
                .collect(Collectors.toList());

        return results;
    }

    public List<EvaluationTargetInstitutionDto> getEvaluationTargetInstitutionDtoListByEvlYr(String year) {
        String sql = "SELECT    eti.id, eti.instt_id, eti.evl_yr, eti.trgt_instt_yn\n" +
                "FROM   tb_rev_evl_trgt_instt eti\n" +
                "       INNER JOIN  tb_rev_instt i\n" +
                "           ON      i.del_yn = 'N'\n" +
                "           AND     eti.evl_yr = :year\n" +
                "           AND     eti.instt_id IS NOT NULL\n" +
                "           AND     eti.evl_yr IS NOT NULL\n" +
                "           AND     eti.instt_id = i.id";
        Query query = em.createNativeQuery(sql);
        query.setParameter("year", year);
        List<Object[]> objects = query.getResultList();

        List<EvaluationTargetInstitutionDto> results = objects.stream()
                .map(result -> {
                    BigInteger idBigInteger = (BigInteger) result[0];
                    BigInteger insttIdBigInteger = (BigInteger) result[1];
                    String evlYr = (String) result[2];
                    Character trgtInsttYn = (Character) result[3];

                    return new EvaluationTargetInstitutionDto(
                            idBigInteger.longValue(),
                            insttIdBigInteger.longValue(),
                            evlYr,
                            trgtInsttYn.toString()
                    );
                })
                .collect(Collectors.toList());

        return results;
    }

    public Page<EvaluationTargetInstitution> findAllBySearchParam(Pageable pageable, EvaluationTargetInstitutionSearchParam searchParam) {
        Specification specification = Specification.where(EvaluationTargetInstitutionSpecification.search(searchParam));

        return evaluationTargetInstitutionRepository.findAll(specification, pageable);
    }

    @Transactional
    public void saveAll(List<EvaluationTargetInstitution> evaluationTargetInstitutions) {
        this.evaluationTargetInstitutionRepository.saveAll(evaluationTargetInstitutions);
    }

    @Transactional
    public void deleteAllByEvaluationTargetInstitutionDtosAndEvlYr(List<EvaluationTargetInstitutionDto> targets, String evlYr) {
        if (!Objects.isNull(targets) && !targets.isEmpty() && !StringUtils.isNullOrEmpty(evlYr)) {
            for (EvaluationTargetInstitutionDto trgtInsttDto : targets) {
                if ("N".equals(trgtInsttDto.getTrgtInsttYn())) {
                    continue;
                }
                factService.deleteOfInstitutionResultByInstitutionIdAndYear(trgtInsttDto.getInsttId(), evlYr);
                factService.deleteAllOfIndexResultByInstitutionIdAndYear(trgtInsttDto.getInsttId(), evlYr);
                evaluationResultService.deleteAllByInstitutionIdAndYear(trgtInsttDto.getInsttId(), evlYr);
            }
            List<Long> targetIds = targets.stream().map(EvaluationTargetInstitutionDto::getId).collect(Collectors.toList());
            evaluationTargetInstitutionRepository.deleteAllById(targetIds);
        }
    }


    @Transactional
    public void bulkUpdateTrgtInsttYn(List<EvaluationTargetInstitutionDto> targets) {
        List<EvaluationTargetInstitution> evaluationTargetInstitutions = new ArrayList<>();
        List<InstitutionResultFact> institutionResultFacts = new ArrayList<>();

        if (!Objects.isNull(targets) && !targets.isEmpty() && !StringUtils.isNullOrEmpty(targets.get(0).getYear())) {
            List<EvaluationIndex> indices = evaluationIndexService.findByYear(targets.get(0).getYear());

            for (EvaluationTargetInstitutionDto eti : targets) {
                EvaluationTargetInstitution newEti = this.updateTrgtInsttYn(eti, indices);
                if (!Objects.isNull(newEti)) {
                    evaluationTargetInstitutions.add(newEti);
                    Optional<InstitutionResultFact> factOptional = this.factService.findInstitutionResultByInstitutionIdAndYear(eti.getInsttId(), eti.getYear());
                    InstitutionResultFact institutionResultFact = factOptional.orElseGet(() -> new InstitutionResultFact(eti.getInsttId(), eti.getYear()));

                    if ("Y".equals(eti.getTrgtInsttYn())) {
                        institutionResultFacts.add(institutionResultFact);
                    } else {
                        if (factOptional.isPresent()) {
                            this.factService.deleteInstitutionResult(institutionResultFact);
                        }
                    }
                }
            }

            if (evaluationTargetInstitutions.size() > 0)
                this.saveAll(evaluationTargetInstitutions);
            if (institutionResultFacts.size() > 0)
                this.factService.saveAllOfInstitutionResult(institutionResultFacts);
        }
    }

    @Transactional
    public List<EvaluationTargetInstitutionDto> excelUpload(MultipartFile file, String uploadYear) throws ParseException, NumberFormatException {
        List<EvaluationTargetInstitutionDto> evaluationTargetInstitutionDtos = new ArrayList<>();
        List<Institution> institutionList = new ArrayList<>();
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd.");

        // 파일 존재하지 않는 경우
        if (file.getSize() <= 0) {
            throw new NullPointerException();
        }

        List<Map<String, String>> listMap = excelUtils.getListData(file, 0, 4);
        boolean isHeaders = true;
        Map<String, String> headers = listMap.get(0);
        String header1 = headers.get("0");
        String header2 = headers.get("1");
        String header3 = headers.get("2");
        String header4 = headers.get("3");

        if (StringUtils.isNullOrEmpty(header1) || StringUtils.isNullOrEmpty(header2) || StringUtils.isNullOrEmpty(header3) || StringUtils.isNullOrEmpty(header4)) {
            throw new ParseException(null, 0);
        } else if (!"* 연도".equals(header1) || !"* 기관유형코드".equals(header2) || !"* 기관코드".equals(header3) || !"* 평가대상 여부\n(Y/N)".equals(header4)) {
            throw new ParseException(null, 0);
        }

        Map<String, Long> insttCdToInsttIdMap = institutionService.getInsttCdToInsttIdMap();
        Map<String, Long> ctgyCdToCtgyIdMap = institutionCategoryService.getCtgyCdToCtgyIdMap();

        for (Map<String, String> map : listMap) {
            if (isHeaders) {
                isHeaders = false;
                continue;
            }
            String year = map.get("0");
            year = StringUtils.isNullOrEmpty(year) ? year : year.trim();

            String ctgyCd = map.get("1");
            ctgyCd = StringUtils.isNullOrEmpty(ctgyCd) ? ctgyCd : ctgyCd.trim().toUpperCase();

            String insttCd = map.get("2");
            insttCd = StringUtils.isNullOrEmpty(insttCd) ? insttCd : insttCd.trim().toUpperCase();

            String trgtInsttYn = map.get("3");
            trgtInsttYn = StringUtils.isNullOrEmpty(trgtInsttYn) ? trgtInsttYn : trgtInsttYn.trim().toUpperCase();
            trgtInsttYn = "Y".equals(trgtInsttYn) ? "Y" : "N";

            if (StringUtils.isNullOrEmpty(year) || StringUtils.isNullOrEmpty(ctgyCd) || StringUtils.isNullOrEmpty(insttCd) || StringUtils.isNullOrEmpty(trgtInsttYn)) {
                throw new ParseException(null, 0);
            }
            if (!uploadYear.equals(year)) {
                throw new ParseException(null, 0);
            }
            Long ctgyId = ctgyCdToCtgyIdMap.getOrDefault(ctgyCd, null);
            if (Objects.isNull(ctgyId)) {
                throw new ParseException(null, 1);
            }
            Long insttId = insttCdToInsttIdMap.getOrDefault(insttCd, null);
            if (Objects.isNull(insttId)) {
                throw new ParseException(null, 2);
            }

            // 기존 데이터 유무에 따라 DB job 최소화 -> 기존 데이터 전부 delete 후 재생성하는 로직으로 변경하면서 주석 처리
//            Optional<EvaluationTargetInstitutionDto> evaluationTargetInstitutionDtoOptional = InsttIdToTrgtDtoMap.stream().filter(dto -> insttId.equals(dto.getInsttId())).findFirst();
//            EvaluationTargetInstitutionDto evaluationTargetInstitutionDto;
//            if (evaluationTargetInstitutionDtoOptional.isPresent()) {
//                evaluationTargetInstitutionDto = evaluationTargetInstitutionDtoOptional.get();
//                if (trgtInsttYn.equals(evaluationTargetInstitutionDto.getTrgtInsttYn())) {
//                    continue; // 기존 데이터와 동일한 Row는 PASS
//                } else {
//                    evaluationTargetInstitutionDto.setTrgtInsttYn(trgtInsttYn);
//                }
//            } else {
//                evaluationTargetInstitutionDto = new EvaluationTargetInstitutionDto(insttId, year, trgtInsttYn);
//            }

            Institution institution = this.institutionService.findById(insttId);
            InstitutionCategory institutionCategory = this.institutionCategoryService.findByIdNotOptional(ctgyId);
            institution.setType(ctgyCd);
            institution.setCategory(institutionCategory);

            boolean isDetailExist = false;
            for (InstitutionDetail detail : institution.getInstitutionDetails()) {
                if (isDetailExist) {
                    break;
                }
                if (detail.getYear().equals(year)) {
                    detail.setCategory(institutionCategory);
                    detail.setType(ctgyCd);
                    isDetailExist = true;
                }
            }
            if (!isDetailExist) {
                InstitutionDetail detail = InstitutionDetail.builder()
                        .institution(institution).category(institutionCategory)
                        .year(year).type(ctgyCd).build();
                institution.getInstitutionDetails().add(detail);
            }
            institutionList.add(institution);
//            institutionRepository.updateTypeAndCtgyIdById(insttId, ctgyCd, ctgyId);

            EvaluationTargetInstitutionDto evaluationTargetInstitutionDto = new EvaluationTargetInstitutionDto(insttId, year, trgtInsttYn);
            evaluationTargetInstitutionDtos.add(evaluationTargetInstitutionDto);
        }
        institutionService.saveAll(institutionList);

        if (evaluationTargetInstitutionDtos.size() > 0) {
            // 기존 데이터 전부 delete 후 재생성
            List<EvaluationTargetInstitutionDto> InsttIdToTrgtDtoMap = this.getEvaluationTargetInstitutionDtoListByEvlYr(uploadYear);
            this.deleteAllByEvaluationTargetInstitutionDtosAndEvlYr(InsttIdToTrgtDtoMap, uploadYear);

            this.bulkUpdateTrgtInsttYn(evaluationTargetInstitutionDtos);
        }

        return evaluationTargetInstitutionDtos;
    }

    public ByteArrayInputStream databaseToExcel(String year) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        /*
        Sheet 1 - 기관 유형별 평가대상 기관 수
         */
        List<InsttCategoryCountVo> targetGroups = this.findAllInsttCategoryCountVoByYear(year);
        int totalTargetCount = targetGroups.stream().reduce(0, (total, second) -> total + second.getCount(), Integer::sum);
        List<InsttCategoryCountVo> targetYGroups = this.findAllInsttCategoryCountVoByYearAndTrgtYnEqualY(year);
        int totalTargetYCount = targetYGroups.stream().reduce(0, (total, second) -> total + second.getCount(), Integer::sum);

        Sheet sheet1 = workbook.createSheet("기관 유형별 평가대상 기관 수");
        Row headerRow1 = sheet1.createRow(0);

        String[] headers1 = {"번호", "연도", "기관 유형", "평가대상 기관 수"};
        for (int col = 0; col < headers1.length; col++) {
            Cell cell = headerRow1.createCell(col);
            cell.setCellValue(headers1[col]);
        }

        int rowIdx1 = 1;
        for (InsttCategoryCountVo insttCategoryCountVo : targetGroups) {
            int cellIdx1 = 0;
            Row row1 = sheet1.createRow(rowIdx1);

            row1.createCell(cellIdx1++).setCellValue(rowIdx1);
            row1.createCell(cellIdx1++).setCellValue(year);
            row1.createCell(cellIdx1++).setCellValue(insttCategoryCountVo.getName() + " (" + insttCategoryCountVo.getCode() + ") ");
            row1.createCell(cellIdx1++).setCellValue(insttCategoryCountVo.getCount() == 0 ? insttCategoryCountVo.getCount() + " / " + insttCategoryCountVo.getCount().toString() : targetYGroups.get(rowIdx1 - 1).getCount() + " / " + insttCategoryCountVo.getCount());

            rowIdx1++;
        }

        ///// 합계 row 추가 - Start /////
        int cellIdx1 = 0;
        Row row1 = sheet1.createRow(rowIdx1);

        row1.createCell(cellIdx1++).setCellValue(rowIdx1);
        row1.createCell(cellIdx1++).setCellValue(year);
        row1.createCell(cellIdx1++).setCellValue("합 계");
        row1.createCell(cellIdx1++).setCellValue(totalTargetCount == 0 ? String.valueOf(totalTargetCount) : totalTargetYCount + " / " + totalTargetCount);

        rowIdx1++;
        ///// 합계 row 추가 - End /////

        /*
        Sheet 2 - 기관별 평가대상 여부
         */
        List<EvaluationTargetInstitutionDto> evaluationTargetInstitutionDtos = this.findAllEvaluationTargetInstitutionDtoByYear(year);

        Sheet sheet2 = workbook.createSheet("기관별 평가대상 여부");
        Row headerRow2 = sheet2.createRow(0);

        String[] headers2 = {"번호", "연도", "기관 유형코드", "기관 유형명", "기관코드", "기관명", "평가대상 여부"};
        for (int col = 0; col < headers2.length; col++) {
            Cell cell = headerRow2.createCell(col);
            cell.setCellValue(headers2[col]);
        }

        int rowIdx2 = 1;
        for (EvaluationTargetInstitutionDto evaluationTargetInstitutionDto : evaluationTargetInstitutionDtos) {
            int cellIdx = 0;
            Row row2 = sheet2.createRow(rowIdx2);

            row2.createCell(cellIdx++).setCellValue(rowIdx2);
            row2.createCell(cellIdx++).setCellValue(evaluationTargetInstitutionDto.getYear());
            row2.createCell(cellIdx++).setCellValue(evaluationTargetInstitutionDto.getInsttCtgyCd());
            row2.createCell(cellIdx++).setCellValue(evaluationTargetInstitutionDto.getInsttCtgyNm());
            row2.createCell(cellIdx++).setCellValue(evaluationTargetInstitutionDto.getInsttCd());
            row2.createCell(cellIdx++).setCellValue(evaluationTargetInstitutionDto.getInsttNm());
            row2.createCell(cellIdx++).setCellValue(evaluationTargetInstitutionDto.getTrgtInsttYn());

            rowIdx2++;
        }

        workbook.write(out);

        return new ByteArrayInputStream(out.toByteArray());
    }

    public void remapEvalTrgtInstt(Institution newInstt, Institution beforeInstt) {
        evaluationTargetInstitutionRepository.updateByBeforeInstt(newInstt.getId(), beforeInstt.getId());
    }
}
