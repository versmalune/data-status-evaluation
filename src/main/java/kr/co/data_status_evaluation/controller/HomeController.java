package kr.co.data_status_evaluation.controller;

import kr.co.data_status_evaluation.model.*;
import kr.co.data_status_evaluation.model.enums.EvaluationType;
import kr.co.data_status_evaluation.model.helper.CurrentUser;
import kr.co.data_status_evaluation.model.search.InstitutionSearchParam;
import kr.co.data_status_evaluation.service.*;
import kr.co.data_status_evaluation.service.dw.FactService;
import kr.co.data_status_evaluation.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

@RequestMapping("/")
@Controller
@RequiredArgsConstructor
public class HomeController {

    private final EvaluationFieldService evaluationFieldService;
    private final EvaluationIndexService evaluationIndexService;
    private final EvaluationScheduleService evaluationScheduleService;
    private final EvaluationResultService evaluationResultService;
    private final InstitutionService institutionService;
    private final InstitutionCategoryService institutionCategoryService;
    private final AccountService accountService;
    private final FactService factService;

    @GetMapping("")
    public String index(@PageableDefault(sort = "code", direction = Sort.Direction.ASC) Pageable pageable,
                        InstitutionSearchParam searchParam, Model model) {
        Account account = (Account) model.getAttribute("currentUser");
        if (account == null) {
            return "redirect:/account/login";
        }
        if (account.isInstitution()) {
            return "redirect:/instt/dashboard";
        }
        if (account.isCommittee()) {
            return "redirect:/committee/dashboard";
        }

        List<String> evlYears = this.evaluationFieldService.findYears();
        if (StringUtils.isNullOrEmpty(searchParam.getYear())) {
            searchParam.setYear(evlYears.get(0));
        }
        String evlYear = searchParam.getYear();

        List<EvaluationSchedule> schedules = this.evaluationScheduleService.findAllByYear(evlYear, Sort.by(Sort.Direction.ASC, "beginDt"));
        EvaluationSchedule currentSchedule = schedules.stream().filter(EvaluationSchedule::isActive)
                .reduce((first, second) -> second).orElse(null);

        Page<Institution> institutions = this.institutionService.findBySearchParam(pageable, searchParam);
        Pagination pagination = new Pagination(pageable);
        pagination.setTotalPages(institutions.getTotalPages());
        pagination.setTotalElements(institutions.getTotalElements());
        pagination.setQuery(searchParam.getQueryParams());

        List<InstitutionCategory> institutionCategoryList = this.institutionCategoryService.findAll();

        List<EvaluationIndex> indices = this.evaluationIndexService.findAllByIndexRateNotInAndTypeAndYear(EvaluationType.QUALITATIVE, evlYear);
        if (!indices.isEmpty()) {
            model.addAttribute("message", "정성지표 중 상대평가 비율설정이 되어있지 않은 지표가 존재합니다.");
        }
        model.addAttribute("searchParam", searchParam);
        model.addAttribute("evlYears", evlYears);
        model.addAttribute("currentYear", evlYear);
        model.addAttribute("schedules", schedules);
        model.addAttribute("currentSchedule", currentSchedule);
        model.addAttribute("institutions", institutions);
        model.addAttribute("categories", institutionCategoryList);
        model.addAttribute("pagination", pagination);

        return "app/home/index";
    }

    @GetMapping("/stats/progress")
    public ResponseEntity<?> getProgress(String evlYear, Model model) {
        Map<String, Integer> progressMap = this.factService.getProgressStatistic(evlYear);

        if (progressMap.isEmpty()) {
            return new ResponseEntity<>("data not found.", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>(progressMap, HttpStatus.OK);
    }

    @GetMapping("/stats/objection")
    public ResponseEntity<?> getObjection(String evlYear, Model model) {
        Map<String, Integer> objectionMap = evaluationResultService.getObjectionStatus(evlYear);
        if (objectionMap.isEmpty()) {
            return new ResponseEntity<>("data not found.", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>(objectionMap, HttpStatus.OK);
    }

    /**
     * 각 상위메뉴의 active 표시하기 위한 controller 별 active 값 set
     *
     * @param model
     */
    @ModelAttribute
    public void setActive(Model model) {
        model.addAttribute("activeMenu", "dashboard");
    }

    @ModelAttribute
    public void setCurrentAccount(Model model, @CurrentUser Account currentUser) {
        Account currentAccount = currentUser == null ? null : this.accountService.findById(currentUser.getId());
        model.addAttribute("currentUser", currentAccount);
    }

//    @ModelAttribute
//    public void setSchedules(Model model) {
//        List<EvaluationSchedule> schedules = evaluationScheduleService.findAllByLastYear();
//        EvaluationSchedule currentSchedule = schedules.stream().filter(EvaluationSchedule::isActive)
//                .reduce((first, second) -> second).orElse(null);
//
//        model.addAttribute("schedules", schedules);
//        model.addAttribute("currentSchedule", currentSchedule);
//    }
}
