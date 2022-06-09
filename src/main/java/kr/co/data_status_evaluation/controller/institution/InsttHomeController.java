package kr.co.data_status_evaluation.controller.institution;

import kr.co.data_status_evaluation.model.Account;
import kr.co.data_status_evaluation.model.Board;
import kr.co.data_status_evaluation.model.EvaluationField;
import kr.co.data_status_evaluation.model.EvaluationSchedule;
import kr.co.data_status_evaluation.model.dto.SchedulesDto;
import kr.co.data_status_evaluation.model.enums.BoardType;
import kr.co.data_status_evaluation.model.helper.CurrentUser;
import kr.co.data_status_evaluation.service.*;
import kr.co.data_status_evaluation.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Objects;

@Controller
@RequiredArgsConstructor
@RequestMapping("/instt")
public class InsttHomeController {

    private final AccountService accountService;
    private final EvaluationTargetInstitutionService evaluationTargetInstitutionService;
    private final EvaluationScheduleService evaluationScheduleService;
    private final EvaluationFieldService evaluationFieldService;
    private final BoardService boardService;

    @GetMapping("/dashboard")
    public String index(@CurrentUser Account account, String evlYear, Model model) {
        if (account == null) {
            return "redirect:/account/login";
        }
        Account currentUser = accountService.findOneWithInstitutionById(account.getId());

        List<String> evlYears = evaluationFieldService.findYears();
        evlYear = StringUtils.isNullOrEmpty(evlYear) ? evlYears.get(0) : evlYear;

        boolean isTargetInstitution = false;
        if (!Objects.isNull(currentUser.getInstitution())) {
            isTargetInstitution = evaluationTargetInstitutionService.isTargetInstitutionByYearAndInsttId(evlYear, currentUser.getInstitution());
        }

        List<EvaluationField> fields = evaluationFieldService.findAllByYear(evlYear);

        List<EvaluationSchedule> schedules = evaluationScheduleService.findAllByYear(evlYear, Sort.by(Sort.Direction.ASC, "beginDt"));
        SchedulesDto schedulesDto = new SchedulesDto(schedules);
        EvaluationSchedule currentSchedule = schedules.stream().filter(EvaluationSchedule::isActive)
                .reduce((first, second) -> second).orElse(null);

        List<Board> boards = boardService.findTop5ByNttTy(BoardType.NOTICE, Sort.by(Sort.Direction.DESC, "createdAt"));

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("evlYears", evlYears);
        model.addAttribute("currentEvlYear", evlYear);
        model.addAttribute("isTargetInstitution", isTargetInstitution);
        model.addAttribute("fields", fields);
        model.addAttribute("schedules", schedulesDto);
        model.addAttribute("currentSchedule", currentSchedule);
        model.addAttribute("notices", boards);

        return "app/institution/index";
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
}
