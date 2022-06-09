package kr.co.data_status_evaluation.controller.committee;

import kr.co.data_status_evaluation.model.Account;
import kr.co.data_status_evaluation.model.EvaluationIndex;
import kr.co.data_status_evaluation.model.EvaluationResult;
import kr.co.data_status_evaluation.model.EvaluationSchedule;
import kr.co.data_status_evaluation.model.helper.CurrentUser;
import kr.co.data_status_evaluation.service.AccountService;
import kr.co.data_status_evaluation.service.EvaluationFieldService;
import kr.co.data_status_evaluation.service.EvaluationResultService;
import kr.co.data_status_evaluation.service.EvaluationScheduleService;
import kr.co.data_status_evaluation.service.dw.FactService;
import kr.co.data_status_evaluation.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/committee")
public class CommitteeHomeController {

    private final EvaluationFieldService evaluationFieldService;
    private final EvaluationScheduleService evaluationScheduleService;
    private final EvaluationResultService evaluationResultService;
    private final AccountService accountService;
    private final FactService factService;


    @GetMapping("/dashboard")
    public String index(String evlYear, Model model) {
        Account currentAccount = (Account) model.getAttribute("currentUser");
        if (currentAccount == null) {
            return "redirect:/account/login";
        }

        List<String> evlYears = evaluationFieldService.findYears();
        String currentYear = StringUtils.isNullOrEmpty(evlYear) ? evlYears.get(0) : evlYear;

        List<EvaluationIndex> indices = currentAccount.getIndicesByYear(currentYear);

        List<EvaluationSchedule> schedules = evaluationScheduleService.findAllByYear(currentYear, Sort.by(Sort.Direction.ASC, "beginDt"));
        EvaluationSchedule currentSchedule = schedules.stream().filter(EvaluationSchedule::isActive)
                .reduce((first, second) -> second).orElse(null);
        if (Objects.isNull(currentSchedule)) {
            currentSchedule = schedules.stream().filter(EvaluationSchedule::isDone)
                    .reduce((first, second) -> second).orElse(null);
        }

        model.addAttribute("evlYears", evlYears);
        model.addAttribute("currentYear", currentYear);
        model.addAttribute("indices", indices);
        model.addAttribute("schedules", schedules);
        model.addAttribute("currentSchedule", currentSchedule);

        return "app/committee/index";
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
        Account currentAccount = (Account) model.getAttribute("currentUser");
        List<EvaluationIndex> indices = currentAccount.getIndices().stream()
                .filter(index-> index.getEvaluationField().getYear().equals(evlYear)).collect(Collectors.toList());

        List<EvaluationResult> results = new ArrayList<>();
        for (EvaluationIndex index : indices) {
            results.addAll(index.getResults());
        }

        Map<String, Integer> objectionMap = this.evaluationResultService.getObjectionStatus(results);
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
        Account currentAccount = this.accountService.findById(currentUser.getId());

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
