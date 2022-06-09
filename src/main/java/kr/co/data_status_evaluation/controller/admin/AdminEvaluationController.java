package kr.co.data_status_evaluation.controller.admin;

import kr.co.data_status_evaluation.model.*;
import kr.co.data_status_evaluation.model.dto.EvaluationResultPercentRankDto;
import kr.co.data_status_evaluation.model.dto.SchedulesDto;
import kr.co.data_status_evaluation.model.dw.InstitutionResultFact;
import kr.co.data_status_evaluation.model.enums.EvaluationStatus;
import kr.co.data_status_evaluation.model.enums.EvaluationType;
import kr.co.data_status_evaluation.model.enums.MaterialType;
import kr.co.data_status_evaluation.model.search.InstitutionSearchParam;
import kr.co.data_status_evaluation.model.vo.EvaluationResultWrap;
import kr.co.data_status_evaluation.model.vo.FieldScoreResultVo;
import kr.co.data_status_evaluation.model.vo.InstitutionResultVo;
import kr.co.data_status_evaluation.model.vo.InsttIndexVo;
import kr.co.data_status_evaluation.service.*;
import kr.co.data_status_evaluation.service.dw.FactService;
import kr.co.data_status_evaluation.util.ExcelUtils;
import kr.co.data_status_evaluation.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Controller
@RequestMapping("/admin/eval")
@RequiredArgsConstructor
public class AdminEvaluationController {

    private final InstitutionService institutionService;
    private final InstitutionCategoryService institutionCategoryService;
    private final FactService factService;
    private final EvaluationResultService evaluationResultService;
    private final EvaluationIndexService evaluationIndexService;
    private final EvaluationFieldService evaluationFieldService;
    private final EvaluationScheduleService evaluationScheduleService;
    private final MaterialService materialService;
    private final ExcelUtils excelUtils;

    @GetMapping("")
    @PreAuthorize("hasRole('ADMIN')")
    public String index(@PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
                        InstitutionSearchParam searchParam, Model model) {
        List<String> evlYears = evaluationFieldService.findYears();
        String currentYear = evlYears.get(0);
        // year 파라미터가 없을 경우 default year 세팅
        if (StringUtils.isNullOrEmpty(searchParam.getYear())) {
            searchParam.setYear(currentYear);
        }
        

        Page<Institution> institutions = institutionService.findBySearchParam(pageable, searchParam);
        Map<Long, InstitutionResultFact> institutionResultFactMap = new HashMap<>();
        if (!Objects.isNull(institutions.getContent()) && !institutions.getContent().isEmpty()) {
            List<Long> insttIds = new ArrayList<>();
            institutions.getContent().forEach(
                    institution -> insttIds.add(institution.getId())
            );
            institutionResultFactMap = factService.findInstitutionResultFactByYearAndInsttIds(searchParam.getYear(), insttIds);
        }

        List<EvaluationSchedule> schedules = evaluationScheduleService.findAllByYear(searchParam.getYear(), Sort.by(Sort.Direction.ASC, "beginDt"));
        EvaluationSchedule currentSchedule = schedules.stream().filter(EvaluationSchedule::isActive)
                .reduce((first, second) -> second).orElse(null);
        if (Objects.isNull(currentSchedule) || (EvaluationStatus.P1_START.equals(currentSchedule.getName()) || EvaluationStatus.P2.equals(currentSchedule.getName()))) {
            currentSchedule = schedules.stream().filter(EvaluationSchedule::isDone)
                    .reduce((first, second) -> second).orElse(null);
        }

        Pagination pagination = new Pagination(pageable);
        pagination.setTotalPages(institutions.getTotalPages());
        pagination.setTotalElements(institutions.getTotalElements());
        pagination.setQuery(searchParam.getQueryParams());

        List<InstitutionCategory> institutionCategoryList = this.institutionCategoryService.findAll();

        model.addAttribute("searchParam", searchParam);
        model.addAttribute("evlYears", evlYears);
        model.addAttribute("pagination", pagination);
        model.addAttribute("currentSchedule", currentSchedule);
        model.addAttribute("currentYear", currentYear);
        model.addAttribute("institutions", institutions);
        model.addAttribute("categories", institutionCategoryList);
        model.addAttribute("institutionResultFactMap", institutionResultFactMap);

        return "app/admin/evaluations/index";
    }

    @PostMapping("/bulkUpdate/{year}/{status}")
    public ResponseEntity<?> bulkUpdateTrgtInsttYn(@PathVariable("year") String year,
                                                   @PathVariable("status") String status,
                                                   @RequestBody(required = false) List<Institution> insttList,
                                                   Model model) {
        List<Long> idList = new ArrayList<>();
        insttList.forEach(instt -> idList.add(instt.getId()));
        factService.bulkUpdateOfInstitutionResultProcessStatusByYearAndInsttIdList(year, idList, status);
        evaluationResultService.bulkUpdateProcessStatusByYearInsttIdList(year, idList, status);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/instt/{id}")
    public String show(@PathVariable("id") String id,
                       @RequestParam(required = false) String year,
                       @RequestParam(required = false) String active,
                       InstitutionSearchParam searchParam,
                       Model model) {
        if (active == null) {
            active = "0";
        }

        Institution institution = institutionService.findById(Long.parseLong(id)); //기관
        InstitutionResultFact insttResultFact = factService.findInstitutionResultByInstitutionIdAndYear(institution.getId(), year).orElseGet(() -> new InstitutionResultFact(institution.getId(), year));
        /* 정성평가 순위 Mapping - Start */
        List<InsttIndexVo> insttIndexVos = this.evaluationIndexService.getInsttIndexVoByInstitutionAndYear(institution, year);
        Map<String, Integer> rowspanMapByField = new HashMap<>(); // 연도별 지표 평가 결과 영역별 rowspan 계산을 위한 map
        List<Long> currentIndexIds = evaluationIndexService.getIndicesIdByYear(insttIndexVos, year);
        List<EvaluationResultPercentRankDto> rankList = evaluationResultService.findPercentRank(year, currentIndexIds, null);

        for (InsttIndexVo insttIndexVo : insttIndexVos) {
            if (!Objects.isNull(insttIndexVo.getType()) && insttIndexVo.getType().equals(EvaluationType.QUALITATIVE)) {
                insttIndexVo.setQualitativeRankByRate(rankList);
            }
            String fieldName = insttIndexVo.getEvaluationField().getName();
            rowspanMapByField.put(fieldName, rowspanMapByField.getOrDefault(fieldName, 0) + 1);
        }
        /* 정성평가 순위 Mapping - End */
        // 지표 양식 파일 매핑
        List<Long> indexIds = insttIndexVos.stream().map(InsttIndexVo::getId).collect(Collectors.toList());
        List<Material> indexMaterials = this.materialService.findAllByMtlTyAndAtchTrgtIds(MaterialType.FORM, indexIds);

        List<EvaluationField> evaluationFields = evaluationFieldService.findByYear(year, Sort.by(Sort.Direction.ASC, "no"));  // 해당년도 필드 ex) 관리체계 등등
        List<EvaluationSchedule> schedules = evaluationScheduleService.findAllByYear(year, Sort.by(Sort.Direction.ASC, "beginDt")); // 해당년도 지표 상태별 스케쥴
        SchedulesDto schedulesDto = new SchedulesDto(schedules);
        EvaluationSchedule currentSchedule = schedules.stream().filter(EvaluationSchedule::isActive)
                .reduce((first, second) -> second).orElse(null);
        boolean isScheduleFinished = false;
        for (EvaluationSchedule schedule : schedules) {
            if (schedule.getName().equals(EvaluationStatus.END)) {
                isScheduleFinished = schedule.isDone();
            }
        }
        model.addAttribute("insttResultFact", insttResultFact);
        model.addAttribute("evaluationIndices", insttIndexVos);
        model.addAttribute("evaluationFields", evaluationFields);
        model.addAttribute("institution", institution);
        model.addAttribute("active", active);
        model.addAttribute("schedules", schedulesDto);
        model.addAttribute("currentSchedule", currentSchedule);
        model.addAttribute("isScheduleFinished", isScheduleFinished);
        model.addAttribute("wrap", new EvaluationResultWrap());
        model.addAttribute("indexMaterials", indexMaterials);
        model.addAttribute("searchParam", searchParam);
        model.addAttribute("rowspanMapByField", rowspanMapByField);

        return "app/admin/evaluations/show";
    }

    @PostMapping("/instt/{id}")
    public ResponseEntity<?> post(@PathVariable("id") String id,
                                  EvaluationResultWrap wrap,
                                  Model model) {
        try {
            String year = wrap.getEvaluationResultList().get(0).getYear();
            List<EvaluationSchedule> schedules = evaluationScheduleService.findAllByYear(year, Sort.by(Sort.Direction.ASC, "beginDt")); // 해당년도 지표 상태별 스케쥴
            this.evaluationResultService.save(wrap, schedules);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("entities not saved.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>("entities saved.", HttpStatus.OK);
    }

    @PostMapping("/instt/{InstitutionId}/insttFactProcessStatus")
    public String updateInsttFactProcessStatus(@PathVariable("InstitutionId") String id,
                                               InstitutionResultFact fact,
                                               Model model,
                                               RedirectAttributes redirect) throws Exception {
        InstitutionResultFact institutionResultFact = factService.findInstitutionResultByInstitutionIdAndYear(fact.getInstitutionId(), fact.getYear()).orElse(null);
        if (!institutionResultFact.getProcessStatus().equals(fact.getProcessStatus())) {
            institutionResultFact.setProcessStatus(fact.getProcessStatus());
            factService.saveOfInstitutionResult(institutionResultFact);
        }

        Institution institution = institutionService.findById(fact.getInstitutionId());
        List<EvaluationResult> evaluationResults = evaluationResultService.findAllByInstitutionAndYear(institution, fact.getYear());
        for (EvaluationResult evaluationResult : evaluationResults) {
            evaluationResult.setProcessStatus(fact.getProcessStatus());
        }
        evaluationResultService.saveAll(evaluationResults);

        redirect.addAttribute("year", fact.getYear());

        return "redirect:/admin/eval/instt/" + id;
    }

    @GetMapping("/api/results")
    public ResponseEntity<?> getResults(@RequestParam(name = "year") String year) {
        String lastYear = String.valueOf(Integer.parseInt(year) - 1);
        List<EvaluationField> fields = this.evaluationFieldService.findAllByYear(year);
        List<EvaluationIndex> indices = this.evaluationIndexService.findAllByYear(year);
        Map<Long, Map<Long, InsttIndexVo>> indexMap = this.evaluationIndexService.getAllInsttIndexVoByYear(year);

        List<Long> qualitativeIndexIds = indices.stream()
                .filter(index -> !Objects.isNull(index.getType()) && index.getType().equals(EvaluationType.QUALITATIVE))
                .map(EvaluationIndex::getId).collect(Collectors.toList());
        List<EvaluationResultPercentRankDto> ranks = this.evaluationResultService.findPercentRank(year, qualitativeIndexIds, null);

        Map<InstitutionResultVo, Map<String, Float>> institutionMap = new HashMap<>();
        List<InstitutionResultVo> institutionsOfCurrent = this.factService.findAllInstitutionResultByCategoryAndYear(null, year);
        institutionsOfCurrent.forEach(institution -> {
            InstitutionResultVo vo = new InstitutionResultVo();
            vo.setCode(institution.getCode());
            vo.setName(institution.getName());
            vo.setCategory(institution.getCategory());
            vo.setId(institution.getId());

            Map<String, Float> map = institutionMap.getOrDefault(vo, new HashMap<>());
            Float standardScore = institution.getStandardScore();
            if (standardScore.isInfinite()) {
                standardScore = 0.0F;
            }
            map.put(year, standardScore);
            institutionMap.put(vo, map);
        });
        List<InstitutionResultVo> institutionsOfLast = this.factService.findAllInstitutionResultByCategoryAndYear(null, lastYear);
        institutionsOfLast.forEach(institution -> {
            InstitutionResultVo vo = new InstitutionResultVo();
            vo.setCode(institution.getCode());
            vo.setName(institution.getName());
            vo.setCategory(institution.getCategory());
            vo.setId(institution.getId());

            if (institutionMap.containsKey(vo)) {
                Map<String, Float> map = institutionMap.getOrDefault(vo, new HashMap<>());
                Float standardScore = institution.getStandardScore();
                if (standardScore.isInfinite()) {
                    standardScore = 0.0F;
                }
                map.put(lastYear, standardScore);
                institutionMap.put(vo, map);
            }
        });

        // 평가영역별 환산점수 리스트
        List<FieldScoreResultVo> fieldScores = this.factService.findAllOfFieldScoreByYear(year);
        // 평가영역을 key로 기관별 평가영역 환산점수 매핑
        Map<String, Map<Long, Float>> fieldScoreMap = new HashMap<>();
        fieldScores.forEach(fieldScore -> {
            String fieldName = fieldScore.getFieldName();
            Long institutionId = fieldScore.getInstitutionId();
            Float score = fieldScore.getStandardScore();
            Map<Long, Float> map = fieldScoreMap.getOrDefault(fieldName, new HashMap<>());
            map.put(institutionId, score);
            fieldScoreMap.put(fieldName, map);
        });

        Workbook workBook = this.excelUtils.makeEvalResultTemplate(year, fields, indices);
        Sheet sheet = workBook.getSheetAt(0);
        int rowNum = 2;
        for (Map.Entry<InstitutionResultVo, Map<String, Float>> entry : institutionMap.entrySet()) {
            InstitutionResultVo instt = entry.getKey();
            Map<String, Float> resultScores = entry.getValue();

            String lastResult = resultScores.get(lastYear) == null ? "" : BigDecimal.valueOf(resultScores.get(lastYear)).setScale(2, RoundingMode.HALF_UP).toString();
            String currentResult = resultScores.get(year) == null ? "" : BigDecimal.valueOf(resultScores.get(year)).setScale(2, RoundingMode.HALF_UP).toString();
            String contrastResult = "";
            if (!(StringUtils.isNullOrEmpty(lastResult) || StringUtils.isNullOrEmpty(currentResult))) {
                contrastResult = new BigDecimal(currentResult).subtract(new BigDecimal(lastResult)).toString();
            }

            Row row = sheet.createRow(rowNum);
            Cell code = row.createCell(0);
            Cell name = row.createCell(1);
            Cell type = row.createCell(2);
            Cell lastScore = row.createCell(3);
            Cell contrastScore = row.createCell(4);
            Cell currentScore = row.createCell(5);

            code.setCellValue(instt.getCode());
            name.setCellValue(instt.getName());
            type.setCellValue(instt.getCategory());
            lastScore.setCellValue(lastResult);
            contrastScore.setCellValue(contrastResult);
            currentScore.setCellValue(currentResult);

            int defaultHeaderSize = row.getLastCellNum();
            for (int i = 0; i < fields.size(); i++) {
                EvaluationField field = fields.get(i);
                Float fieldScore = fieldScoreMap.get(field.getName()).get(instt.getId());
                BigDecimal roundingScore = new BigDecimal(fieldScore).setScale(2, RoundingMode.HALF_UP);

                Cell cell = row.createCell(i + defaultHeaderSize);
                cell.setCellValue(roundingScore.toString());
            }

            int indexHeaderStartAt = defaultHeaderSize + fields.size();
            for (int i = 0; i < indices.size(); i++) {
                int indexStartAt = indexHeaderStartAt + i * 3;
                EvaluationIndex index = indices.get(i);
                InsttIndexVo insttIndexVo = indexMap.get(index.getId()).get(instt.getId());
                EvaluationType indexType = index.getType();
                Integer indexRank = insttIndexVo.getQuantitationRank();
                if (!Objects.isNull(indexType) && indexType.equals(EvaluationType.QUALITATIVE)) {
                    insttIndexVo.setQualitativeRankByRate(ranks);
                    indexRank = insttIndexVo.getQualitativeRank();
                }

                Cell rank = row.createCell(indexStartAt);
                Cell point = row.createCell(indexStartAt + 1);
                Cell score = row.createCell(indexStartAt + 2);

                if (Objects.isNull(indexType)) {
                    rank.setCellValue("");
                } else if (Objects.isNull(indexRank)) {
                    rank.setCellValue("N/A");
                } else {
                    rank.setCellValue(indexRank);
                }
                point.setCellValue(insttIndexVo.getPerfectScore(instt.getCategory()));
                score.setCellValue(insttIndexVo.getResult().getScore());
            }

            rowNum++;
        }

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workBook.write(out);
            InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(out.toByteArray()));

            workBook.close();
            out.close();
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("Application/octet-stream"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + "테스트.xlsx")
                    .body(resource);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 각 상위메뉴의 active 표시하기 위한 controller 별 active 값 set
     *
     * @param model
     */
    @ModelAttribute
    public void setActive(Model model) {
        model.addAttribute("activeMenu", "eval");
    }
}
