package kr.co.data_status_evaluation.controller.committee;

import kr.co.data_status_evaluation.model.*;
import kr.co.data_status_evaluation.model.dto.EvaluationResultPercentRankDto;
import kr.co.data_status_evaluation.model.dto.SchedulesDto;
import kr.co.data_status_evaluation.model.enums.EvaluationStatus;
import kr.co.data_status_evaluation.model.enums.EvaluationType;
import kr.co.data_status_evaluation.model.enums.MaterialType;
import kr.co.data_status_evaluation.model.helper.CurrentUser;
import kr.co.data_status_evaluation.model.search.EvaluationIndexSearchParam;
import kr.co.data_status_evaluation.model.vo.EvaluationResultWrap;
import kr.co.data_status_evaluation.service.*;
import kr.co.data_status_evaluation.util.PageUtil;
import kr.co.data_status_evaluation.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping("/committee/eval")
@RequiredArgsConstructor
public class CommitteeEvaluationController {

    private final EvaluationFieldService evaluationFieldService;
    private final EvaluationResultService evaluationResultService;
    private final EvaluationIndexService evaluationIndexService;
    private final EvaluationScheduleService evaluationScheduleService;
    private final AccountService accountService;
    private final EvaluationIndexRateService evaluationIndexRateService;
    private final InstitutionService institutionService;
    private final MaterialService materialService;

    @GetMapping("")
    public String index(@RequestParam(required = false) String year,
                        Model model) {
        List<String> evlYears = evaluationFieldService.findYears();
        // year 파라미터가 없을 경우 default year 세팅
        if (StringUtils.isNullOrEmpty(year)) {
            year = evlYears.get(0);
        }
        model.addAttribute("evlYears", evlYears);
        model.addAttribute("year", year);

        Account currentAccount = (Account) model.getAttribute("currentUser");

        List<EvaluationIndex> indices = currentAccount.getIndices();
        String currentYear = year;
        List<EvaluationIndex> filteredIndices = indices.stream()
                .filter(evaluationIndex -> evaluationIndex.getEvaluationField().getYear().equals(currentYear))
                .collect(Collectors.toList());
        model.addAttribute("indices", filteredIndices);

        if (filteredIndices.isEmpty()) {
            model.addAttribute("rateMap", new HashMap<>());
            return "app/committee/evaluations/index";
        }
        List<Long> ids = currentAccount.getIndicesIdByYear(year);

        List<EvaluationResultPercentRankDto> ranks = this.evaluationResultService.findPercentRank(year, ids, null);
        // 지표별 백분위 등급 기관 count
        Map<Long, Map<Integer, Integer>> rateMap = new HashMap<>();
        for (EvaluationIndex index : filteredIndices) {
            Map<Integer, Integer> rankMap = new HashMap<>();
            for (EvaluationResultPercentRankDto rank : ranks) {
                if (Objects.equals(index.getId(), rank.getIndexId().longValue())) {
                    Double percentRank = rank.getPercentRank();
                    Float score = rank.getScore();

                    if (index.getType() == EvaluationType.QUANTITATION) {
                        // N/A 등급(result의 score가 null인 경우)이면 key를 0으로 설정
                        if (index.isNaLevel() && score == null) {
                            rankMap.put(0, rankMap.getOrDefault(0, 0) + 1);
                            continue;
                        }
                        List<EvaluationScore> evaluationScores = index.getScores();
                        for (EvaluationScore evaluationScore : evaluationScores) {
                            if(score.equals(evaluationScore.getScore())) {
                                Integer level = evaluationScore.getLevel();
                                rankMap.put(level, rankMap.getOrDefault(level, 0) + 1);
                                break;
                            }
                        }
                    }

                    if (index.getType() == EvaluationType.QUALITATIVE) {
                        List<EvaluationIndexRateDetail> rateDetails = index.getRate().getDetails();
                        for (EvaluationIndexRateDetail rateDetail : rateDetails) {
                            if (percentRank <= rateDetail.getRate()) {
                                Integer level = rateDetail.getLevel();
                                rankMap.put(level, rankMap.getOrDefault(level, 0) + 1);
                                break;
                            }
                        }
                    }
                }
            }
            rateMap.put(index.getId(), rankMap);
        }

        model.addAttribute("rateMap", rateMap);
        return "app/committee/evaluations/index";
    }

    @GetMapping("/{id}")
    public String show(@PageableDefault(sort = "code", direction = Sort.Direction.ASC) Pageable pageable,
                       EvaluationIndexSearchParam searchParam,
                       @PathVariable("id") String id, Model model) {
        List<String> evlYears = evaluationFieldService.findYears();
        // year 파라미터가 없을 경우 default year 세팅
        if (StringUtils.isNullOrEmpty(searchParam.getYear())) {
            searchParam.setYear(evlYears.get(0));
        }
        String year = searchParam.getYear();

        EvaluationIndex index = this.evaluationIndexService.findById(Long.parseLong(id));
        List<Material> indexMaterials = this.materialService.findAllByMtlTyAndAtchTrgtId(MaterialType.FORM, index.getId());

//        List<Institution> assignedInstitutions = index.getAssignedInstitutions();
        List<Institution> assignedInstitutions = new ArrayList<>();
        for (InstitutionCategory category : index.getAssignedCategories()) {
            assignedInstitutions.addAll(this.institutionService.findAllByTargetAndCategoryIdAndYear(category.getId(), year));
        }
        if (!StringUtils.isNullOrEmpty(searchParam.getInstitutionName())) {
            assignedInstitutions = assignedInstitutions.stream()
                    .filter(el -> el.getName().matches("(.*)" + searchParam.getInstitutionName() + "(.*)"))
                    .collect(Collectors.toList());
        }

        if (!StringUtils.isNullOrEmpty(searchParam.getInstitutionType())) {
            assignedInstitutions = assignedInstitutions.stream()
                    .filter(el -> el.getCategoryByYear(year).getCode().equals(searchParam.getInstitutionType()))
                    .collect(Collectors.toList());
        }

        Page<Institution> institutions = (Page<Institution>) PageUtil.listToPage(assignedInstitutions, pageable);

        Pagination pagination = new Pagination(pageable);
        pagination.setTotalPages(institutions.getTotalPages());
        pagination.setTotalElements(institutions.getTotalElements());
        pagination.setQuery(searchParam.getQueryParams());

        EvaluationResultWrap evaluationResultWrap = new EvaluationResultWrap();

        List<EvaluationSchedule> schedules = evaluationScheduleService.findAllByYear(year, Sort.by(Sort.Direction.ASC, "beginDt")); // 해당년도 지표 상태별 스케쥴
        SchedulesDto schedulesDto = new SchedulesDto(schedules);
        EvaluationSchedule currentSchedule = schedules.stream().filter(EvaluationSchedule::isActive)
                .reduce((first, second) -> second).orElse(null);
        if (Objects.isNull(currentSchedule)) {
            currentSchedule = schedules.stream().filter(EvaluationSchedule::isDone)
                    .reduce((first, second) -> second).orElse(null);
        }
        boolean isScheduleFinished = false;
        for (EvaluationSchedule schedule : schedules) {
            if (schedule.getName().equals(EvaluationStatus.END)) {
                isScheduleFinished = schedule.isDone();
            }
        }

        model.addAttribute("isScheduleFinished", isScheduleFinished);
        model.addAttribute("searchParam", searchParam);
        model.addAttribute("pagination", pagination);
        model.addAttribute("schedules", schedulesDto);
        model.addAttribute("currentSchedule", currentSchedule);
        model.addAttribute("evalIndex", index);
        model.addAttribute("institutions", institutions);
        model.addAttribute("evalResultWrap", evaluationResultWrap);
        model.addAttribute("assignedCategories", index.getAssignedCategories());
        model.addAttribute("indexMaterials", indexMaterials);

        return "app/committee/evaluations/show";
    }

    @PostMapping("/{id}")
    public String saveResults(@PathVariable("id") String id,
                              @RequestParam(required = false) String year,
                              EvaluationResultWrap wrap,
                              Model model) {
        Account currentAccount = (Account) model.getAttribute("currentUser");
        List<Long> ids = currentAccount.getIndicesIdByYear(year);
        String nextId = id;
        for (int i = 0; i < ids.size(); i++) {
            if (ids.get(i).equals(Long.parseLong(id))) {
                if (i < ids.size() - 1) {
                    nextId = String.valueOf(ids.get(i + 1));
                } else {
                    nextId = "";
                }
            }
        }
        List<EvaluationSchedule> schedules = evaluationScheduleService.findAllByYear(year, Sort.by(Sort.Direction.ASC, "beginDt")); // 해당년도 지표 상태별 스케쥴
        for (EvaluationResult result : wrap.getEvaluationResultList()) {
            Institution institution = result.getInstitution();
            EvaluationIndex index = result.getEvaluationIndex();
            Float totalScore = index.getTotalScore(institution.getCategoryByYear(year));
            String score = StringUtils.isNullOrEmpty(result.getScore()) ? "0.0" : result.getScore();

            if (Objects.isNull(index.getType()) || EvaluationType.QUANTITATION.equals(index.getType())) { // 가감점, 정량평가
                result.setScore(score);
            } else { // 정성평가 // if (EvaluationType.QUALITATIVE.equals(index.getType()))
                float convertedScore = Float.parseFloat(score) * totalScore / 100;
                result.setScore(Float.toString(convertedScore));
            }
        }
        this.evaluationResultService.save(wrap, schedules);

        return "redirect:/committee/eval/" + nextId + "?year=" + year;
    }

    @GetMapping("/stats/{id}")
    public ResponseEntity<?> getStats(@PathVariable("id") String id,
                                      @RequestParam(required = false) String year,
                                      Model model) {
        try {
            EvaluationIndex index = evaluationIndexService.findById(Long.parseLong(id));

            Map<String, Integer> statusMap = new LinkedHashMap<>();
            for (EvaluationStatus value : EvaluationStatus.values()) {
                statusMap.put(value.getTitle(), 0);
            }
            List<Institution> institutions = new ArrayList<>();
            for (InstitutionCategory category : index.getAssignedCategories()) {
                institutions.addAll(this.institutionService.findAllByTargetAndCategoryIdAndYear(category.getId(), year));
            }
            for (Institution institution : institutions) {
                EvaluationResult result = institution.getEvaluationResult(index);
                String status = result.getProcessStatus().getTitle();
                statusMap.put(status, statusMap.getOrDefault(status, 0) + 1);
            }

            return new ResponseEntity<>(statusMap, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("There is no indices.", HttpStatus.INTERNAL_SERVER_ERROR);
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

    @ModelAttribute
    public void setCurrentAccount(Model model, @CurrentUser Account currentUser) {
        Account currentAccount = this.accountService.findById(currentUser.getId());
        Collections.sort(currentAccount.getIndices(), Comparator.comparing(EvaluationIndex::getNo));
        model.addAttribute("currentUser", currentAccount);
    }
}
