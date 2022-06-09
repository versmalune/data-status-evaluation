package kr.co.data_status_evaluation.controller.admin;

import kr.co.data_status_evaluation.model.*;
import kr.co.data_status_evaluation.model.dto.EvaluationResultPercentRankDto;
import kr.co.data_status_evaluation.model.enums.EvaluationType;
import kr.co.data_status_evaluation.model.helper.CurrentUser;
import kr.co.data_status_evaluation.model.search.InstitutionSearchParam;
import kr.co.data_status_evaluation.model.search.StatisticSearchParam;
import kr.co.data_status_evaluation.model.vo.EvaluationIndexVo;
import kr.co.data_status_evaluation.model.vo.FieldScoreResultVo;
import kr.co.data_status_evaluation.model.vo.IndexScoreResultVo;
import kr.co.data_status_evaluation.model.vo.InstitutionResultVo;
import kr.co.data_status_evaluation.service.*;
import kr.co.data_status_evaluation.service.dw.FactService;
import kr.co.data_status_evaluation.util.PageUtil;
import kr.co.data_status_evaluation.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/statistics")
public class AdminStatisticController {

    private final AccountService accountService;
    private final InstitutionService institutionService;
    private final InstitutionCategoryService institutionCategoryService;
    private final EvaluationFieldService evaluationFieldService;
    private final EvaluationIndexService evaluationIndexService;
    private final EvaluationIndexRateService evaluationIndexRateService;
    private final EvaluationResultService evaluationResultService;
    private final EvaluationScheduleService evaluationScheduleService;
    private final FactService factService;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @GetMapping("/qualitativeRank")
    public String qualitativeRankIndex(@PageableDefault(size = 50, sort = "code", direction = Sort.Direction.ASC) Pageable pageable,
                                       StatisticSearchParam searchParam, Model model) {
        List<String> evlYears = evaluationFieldService.findYears();
        if (StringUtils.isNullOrEmpty(searchParam.getYear())) {
            searchParam.setYear(evlYears.get(0));
        }
        String evlYear = searchParam.getYear();

        List<EvaluationIndex> indices = this.evaluationIndexService.findAllByTypeAndYear(EvaluationType.QUALITATIVE, evlYear);
        if (StringUtils.isNullOrEmpty(searchParam.getIndex())) {
            searchParam.setIndex(indices.get(0).getId().toString());
        }

        List<EvaluationResultPercentRankDto> ranks = this.evaluationResultService.findPercentRank(evlYear, Long.parseLong(searchParam.getIndex()), searchParam.getType());
        // 지표별 백분위 등급 기관 count
        for (EvaluationResultPercentRankDto rank : ranks) {
            Double percentRank = rank.getPercentRank();
            Optional<EvaluationIndex> indexOptional = indices.stream().filter(index->index.getId().equals(rank.getIndexId().longValue())).findFirst();
            if(!indexOptional.isPresent()) {
                continue;
            }
            List<EvaluationIndexRateDetail> rateDetails = indexOptional.get().getRate().getDetails();
            for (EvaluationIndexRateDetail rateDetail : rateDetails) {
                if (percentRank <= rateDetail.getRate()) {
                    Integer level = rateDetail.getLevel();
                    rank.setRank(level);
                    break;
                }
                rank.setRank(rateDetails.size());
            }
        }
        Page<EvaluationResultPercentRankDto> institutions = (Page<EvaluationResultPercentRankDto>) PageUtil.listToPage(ranks, pageable);

//        Page<Institution> institutions = this.institutionService.findBySearchParam(pageable, searchParam);
        Pagination pagination = new Pagination(pageable);
        pagination.setTotalPages(institutions.getTotalPages());
        pagination.setTotalElements(institutions.getTotalElements());
        pagination.setQuery(searchParam.getQueryParams());

        List<InstitutionCategory> institutionCategoryList = this.institutionCategoryService.findAll();

        model.addAttribute("categories", institutionCategoryList);
        model.addAttribute("institutions", institutions);
        model.addAttribute("indices", indices);
        model.addAttribute("pagination", pagination);
        model.addAttribute("searchParam", searchParam);
        model.addAttribute("evlYears", evlYears);
        model.addAttribute("subActiveMenu", "qualitativeRank");
        model.addAttribute("contextPath", contextPath);

        return "app/admin/statistics/qualitativeRank";
    }

    @GetMapping("/progressStatus")
    public String progressStatus(@RequestParam(value = "status", required = false) String status, Model model) {
        String currentYear = (String) model.getAttribute("currentYear");
        Map<String, Integer> progressMap = this.factService.getProgressStatistic(currentYear);
        List<InstitutionResultVo> institutions = this.factService.findAllInstitutionResultByStatusAndYear(status, currentYear);

        model.addAttribute("institutions", institutions);
        model.addAttribute("progressMap", progressMap);
        model.addAttribute("subActiveMenu", "progressStatus");

        return "app/admin/statistics/progressStatus";
    }

    @GetMapping("/evaluationRank")
    public String getEvaluationRank(StatisticSearchParam searchParam, Model model) {
        if (StringUtils.isNullOrEmpty(searchParam.getTab())) {
            searchParam.setTab("category");
        }

        List<String> evlYears = this.evaluationFieldService.findYears();
        if (StringUtils.isNullOrEmpty(searchParam.getYear())) {
            searchParam.setYear(evlYears.get(0));
        }
        String currentYear = searchParam.getYear();
        List<EvaluationField> fields = this.evaluationFieldService.findAllByYear(currentYear);
        if (StringUtils.isNullOrEmpty(searchParam.getField())) {
            searchParam.setField(fields.get(0).getId().toString());
            if (fields.get(0).getEvaluationIndices().isEmpty()) {
                searchParam.setIndex(null);
            } else {
                searchParam.setIndex(fields.get(0).getEvaluationIndices().get(0).getId().toString());
            }
        }
        // 기관유형 검색조건
        String categoryCode = searchParam.getType() == null ? null : searchParam.getType();
        // 기관평가결과(fact) 리스트
        List<InstitutionResultVo> institutions = this.factService.findAllInstitutionResultByCategoryAndYear(categoryCode, currentYear);
        // 평가영역별 환산점수 리스트
        List<FieldScoreResultVo> fieldScores = this.factService.findAllOfFieldScoreByYear(currentYear);
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
        // 평가영역별 지표 리스트 매핑
        Map<String, List<EvaluationIndexVo>> indexMap = new HashMap<>();
        fields.forEach(field -> {
            indexMap.put(field.getName(), field.getEvaluationIndices().stream().map(index -> {
                EvaluationIndexVo indexVo = new EvaluationIndexVo();
                indexVo.setId(index.getId());
                indexVo.setName(index.getName());
                return indexVo;
            }).collect(Collectors.toList()));
        });
        // 지표영역별 평가순위
        List<IndexScoreResultVo> indexScores = this.factService.findAllRankingByCategoryAndIndexAndYear(categoryCode, searchParam.getIndex(), currentYear);
        List<InstitutionCategory> institutionCategoryList = this.institutionCategoryService.findAll();

        model.addAttribute("categories", institutionCategoryList);
        model.addAttribute("indexScores", indexScores);
        model.addAttribute("fieldScoreMap", fieldScoreMap);
        model.addAttribute("indexMap", indexMap);
        model.addAttribute("evlYears", evlYears);
        model.addAttribute("searchParam", searchParam);
        model.addAttribute("fields", fields);
        model.addAttribute("institutions", institutions);
        model.addAttribute("subActiveMenu", "evaluationRank");
        model.addAttribute("contextPath", contextPath);

        return "app/admin/statistics/evaluationRank";
    }

    @GetMapping("/improvementRank")
    public String getImprovementRank(StatisticSearchParam searchParam, Model model) {
        List<String> evlYears = this.evaluationFieldService.findYears();
        if (StringUtils.isNullOrEmpty(searchParam.getYear())) {
            searchParam.setYear(evlYears.get(0));
        }
        String currentYear = searchParam.getYear();
        String lastYear = String.valueOf(Integer.parseInt(currentYear) - 1);
        List<EvaluationField> fields = this.evaluationFieldService.findAllByYear(currentYear);
        Map<InstitutionResultVo, Map<String, Float>> institutionMap = new HashMap<>();

        // 기관유형 검색조건
        String categoryCode = searchParam.getType() == null ? null : searchParam.getType();
        // 기관평가결과(fact) 리스트
        List<InstitutionResultVo> institutionsOfCurrent = this.factService.findAllInstitutionResultByCategoryAndYear(categoryCode, currentYear);
        institutionsOfCurrent.forEach(institution -> {
            InstitutionResultVo vo = new InstitutionResultVo();
            vo.setCategory(institution.getCategory());
            vo.setName(institution.getName());
            vo.setId(institution.getId());
            Map<String, Float> map = institutionMap.getOrDefault(vo, new HashMap<>());
            Float standardScore = institution.getStandardScore();
            if (standardScore.isInfinite()) {
                standardScore = 0.0F;
            }
            map.put(currentYear, standardScore);
            institutionMap.put(vo, map);
        });
        List<InstitutionResultVo> institutionsOfLast = this.factService.findAllInstitutionResultByCategoryAndYear(categoryCode, lastYear);
        institutionsOfLast.forEach(institution -> {
            InstitutionResultVo vo = new InstitutionResultVo();
            vo.setCategory(institution.getCategory());
            vo.setName(institution.getName());
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

        institutionMap.forEach((k, v) -> {
            Float currentScore = institutionMap.get(k).get(currentYear);
            Float lastScore = institutionMap.get(k).get(lastYear);
            Float rate = lastScore == null || currentScore == null ? Float.POSITIVE_INFINITY : (currentScore / lastScore - 1) * 100;
            k.setImprovementRate(rate);
        });

        // TODO : 개선율 순위 알고리즘 필요.
//        institutionMap.entrySet().stream().sorted((o1, o2) -> {
//            int rank = institutionMap.size() - 1;
//            rank = o1.getKey().getImprovementRate() < o2.getKey().getImprovementRate() ? rank + 1 : rank;
//            return rank;
//        });

        // 평가영역별 환산점수 리스트
        List<FieldScoreResultVo> currentFieldScores = this.factService.findAllOfFieldScoreByYear(currentYear);
        List<FieldScoreResultVo> lastFieldScores = this.factService.findAllOfFieldScoreByYear(lastYear);
        // 평가영역을 key로 기관별 평가영역 환산점수 매핑
        Map<String, Map<Long, Float>> currentFieldScoreMap = new HashMap<>();
        Map<String, Map<Long, Float>> lastFieldScoreMap = new HashMap<>();
        currentFieldScores.forEach(fieldScore -> {
            String fieldName = fieldScore.getFieldName();
            Long institutionId = fieldScore.getInstitutionId();
            Float score = fieldScore.getStandardScore();
            Map<Long, Float> map = currentFieldScoreMap.getOrDefault(fieldName, new HashMap<>());
            map.put(institutionId, score);
            currentFieldScoreMap.put(fieldName, map);
        });
        lastFieldScores.forEach(fieldScore -> {
            String fieldName = fieldScore.getFieldName();
            Long institutionId = fieldScore.getInstitutionId();
            Float score = fieldScore.getStandardScore();
            Map<Long, Float> map = lastFieldScoreMap.getOrDefault(fieldName, new HashMap<>());
            map.put(institutionId, score);
            lastFieldScoreMap.put(fieldName, map);
        });

        List<InstitutionCategory> institutionCategoryList = this.institutionCategoryService.findAll();

        model.addAttribute("categories", institutionCategoryList);
        model.addAttribute("currentFieldScoreMap", currentFieldScoreMap);
        model.addAttribute("lastFieldScoreMap", lastFieldScoreMap);
        model.addAttribute("evlYears", evlYears);
        model.addAttribute("lastYear", lastYear);
        model.addAttribute("currentYearToInteger", Integer.parseInt(currentYear));
        model.addAttribute("searchParam", searchParam);
        model.addAttribute("fields", fields);
        model.addAttribute("institutions", institutionMap);
        model.addAttribute("subActiveMenu", "improvementRank");
        model.addAttribute("contextPath", contextPath);

        return "app/admin/statistics/improvementRank";
    }

    @GetMapping("/statusNone")
    public String getStatusNone(@PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
                                InstitutionSearchParam searchParam, Model model) {
        List<String> evlYears = this.evaluationFieldService.findYears();
        if (StringUtils.isNullOrEmpty(searchParam.getYear())) {
            searchParam.setYear(evlYears.get(0));
        }
        String currentYear = searchParam.getYear();

        searchParam.setTrgtInsttYn("Y"); // 해당년도 평가대상인 기관들만
        Page<Institution> institutionPages = null;
        if (StringUtils.isNullOrEmpty(searchParam.getStatus()) || searchParam.getStatus().equals("noLogin")) { // 미접속 기관
            searchParam.setStatus("noLogin");
        } else { // 미등록 기관
            searchParam.setStatus("noFile");
        }
        List<Institution> institutions = institutionService.findAllBySearchParamNativeQuery(searchParam);
        institutionPages = (Page<Institution>) PageUtil.listToPage(institutions, pageable);

        Pagination pagination = new Pagination(pageable);
        pagination.setTotalPages(institutionPages.getTotalPages());
        pagination.setTotalElements(institutionPages.getTotalElements());
        pagination.setQuery(searchParam.getQueryParams());

        List<InstitutionCategory> institutionCategoryList = this.institutionCategoryService.findAll();
        Map<String, String> categoryCodeMap = new HashMap<>();
        for (InstitutionCategory institutionCategory : institutionCategoryList) {
            categoryCodeMap.put(institutionCategory.getCode(), institutionCategory.getName());
        }

        model.addAttribute("evlYears", evlYears);
        model.addAttribute("searchParam", searchParam);
        model.addAttribute("pagination", pagination);
        model.addAttribute("institutions", institutionPages.getContent());
        model.addAttribute("categories", institutionCategoryList);
        model.addAttribute("categoryCodeMap", categoryCodeMap);
        model.addAttribute("subActiveMenu", "statusNone");

        return "app/admin/statistics/statusNone";
    }

    @GetMapping("/statusNone/excelDownload")
    public ResponseEntity<?> downloadAll(InstitutionSearchParam searchParam) {
        if (StringUtils.isNullOrEmpty(searchParam.getStatus()) || "noLogin".equals(searchParam.getStatus())) { // 미접속 기관
            searchParam.setStatus("noLogin");
        } else { // 미등록 기관
            searchParam.setStatus("noFile");
        }

        try {
            InputStreamResource resource = new InputStreamResource(institutionService.statusNoneDatabaseToExcel(searchParam));

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + "미등록기관" + searchParam.getYear() + "년.xlsx")
                    .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                    .body(resource);

        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

//    @GetMapping("/insttType/evaluationRank/excelDownload")
//    public ResponseEntity<?> insttTypeEvaluationRankDownloadAll(StatisticSearchParam searchParam) {
//
//        try {
//            InputStreamResource resource = new InputStreamResource(institutionService.insttTypeEvaluationRankDatabaseToExcel(searchParam));
//
//            return ResponseEntity.ok()
//                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + "기관유형별_평가순위" + searchParam.getYear() + "년.xlsx")
//                    .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
//                    .body(resource);
//
//        } catch (IOException e) {
//            return ResponseEntity.internalServerError().build();
//        }
//    }
//
//
//    @GetMapping("/evaluationRank/excelDownload")
//    public ResponseEntity<?> evaluationRankDownloadAll(StatisticSearchParam searchParam) {
//
//        try {
//            InputStreamResource resource = new InputStreamResource(institutionService.evaluationRankDatabaseToExcel(searchParam));
//
//            return ResponseEntity.ok()
//                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + "지표영역별_평가순위" + searchParam.getYear() + "년.xlsx")
//                    .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
//                    .body(resource);
//
//        } catch (IOException e) {
//            return ResponseEntity.internalServerError().build();
//        }
//    }

    @GetMapping("/stats/progress")
    public ResponseEntity<?> getProgress(Model model) {
        String currentYear = (String) model.getAttribute("currentYear");
        Map<String, Integer> progressMap = this.factService.getProgressStatistic(currentYear);
        if (progressMap.isEmpty()) {
            return new ResponseEntity<>("data not found.", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>(progressMap, HttpStatus.OK);
    }

    /**
     * 각 상위메뉴의 active 표시하기 위한 controller 별 active 값 set
     *
     * @param model
     */
    @ModelAttribute
    public void setActive(Model model) {
        model.addAttribute("activeMenu", "statistic");
    }

    @ModelAttribute
    public void setCurrentAccount(Model model, @CurrentUser Account currentUser) {
        Account currentAccount = this.accountService.findById(currentUser.getId());
        model.addAttribute("currentUser", currentAccount);
    }

    @ModelAttribute
    public void setCurrentUser(Model model) {
        List<String> evlYears = this.evaluationFieldService.findYears();
        String currentYear = evlYears.get(0);
        model.addAttribute("currentYear", currentYear);
    }
}
