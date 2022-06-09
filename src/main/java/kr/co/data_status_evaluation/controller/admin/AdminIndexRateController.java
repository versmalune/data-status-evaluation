package kr.co.data_status_evaluation.controller.admin;

import kr.co.data_status_evaluation.model.EvaluationIndex;
import kr.co.data_status_evaluation.model.EvaluationIndexRate;
import kr.co.data_status_evaluation.model.EvaluationSchedule;
import kr.co.data_status_evaluation.model.dto.AdminEvaluationIndexRateDto;
import kr.co.data_status_evaluation.model.enums.EvaluationType;
import kr.co.data_status_evaluation.service.EvaluationFieldService;
import kr.co.data_status_evaluation.service.EvaluationIndexRateService;
import kr.co.data_status_evaluation.service.EvaluationIndexService;
import kr.co.data_status_evaluation.service.EvaluationScheduleService;
import kr.co.data_status_evaluation.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/evalRate")
public class AdminIndexRateController {

    private final EvaluationFieldService evaluationFieldService;
    private final EvaluationScheduleService evaluationScheduleService;
    private final EvaluationIndexRateService evaluationIndexRateService;
    private final EvaluationIndexService evaluationIndexService;

    @GetMapping("")
    public String index(@RequestParam(required = false) String year, Model model) {
        List<String> evlYears = evaluationFieldService.findYears();
        year = StringUtils.isNullOrEmpty(year) ? evlYears.get(0) : year;

        List<EvaluationIndexRate> rates = this.evaluationIndexRateService.findAllByYear(year);

        model.addAttribute("evlYears", evlYears);
        model.addAttribute("year", year);
        model.addAttribute("rates", rates);

        return "app/admin/rates/index";
    }

    @GetMapping("/new")
    public String getNew(@RequestParam(required = false) String year, Model model) {
        List<String> evlYears = evaluationFieldService.findYears();
        year = StringUtils.isNullOrEmpty(year) ? evlYears.get(0) : year;
        List<EvaluationIndex> indices = evaluationIndexService.findAllByIndexRateNotInAndTypeAndYear(EvaluationType.QUALITATIVE, year);
        AdminEvaluationIndexRateDto dto = new AdminEvaluationIndexRateDto();

        model.addAttribute("evlYears", evlYears);
        model.addAttribute("year", year);
        model.addAttribute("dto", dto);
        model.addAttribute("indices", indices);

        return "app/admin/rates/form";
    }

    @GetMapping("/{rateId}")
    public String getShow(@PathVariable("rateId") String id, Model model) {
        Optional<EvaluationIndexRate> rateOptional = this.evaluationIndexRateService.findById(Long.parseLong(id));
        if (!rateOptional.isPresent()) {
            return "redirect:/admin/evalRate";
        }
        EvaluationIndexRate rate = rateOptional.get();
        AdminEvaluationIndexRateDto dto = new AdminEvaluationIndexRateDto(rate);

        List<String> evlYears = evaluationFieldService.findYears();
        String year = rate.getYear();
        List<EvaluationIndex> indices = evaluationIndexService.findAllByIndexRateNotInAndTypeAndYear(EvaluationType.QUALITATIVE, year);

        model.addAttribute("dto", dto);
        model.addAttribute("evlYears", evlYears);
        model.addAttribute("year", year);
        model.addAttribute("indices", indices);

        return "app/admin/rates/form";
    }

    @PostMapping("")
    public String post(AdminEvaluationIndexRateDto dto, Model model) {
        this.evaluationIndexRateService.save(dto);

        return "redirect:/admin/evalRate?year=" + dto.getYear();
    }

    @PostMapping("/{rateId}")
    public String put(@PathVariable("rateId") String rateId, AdminEvaluationIndexRateDto dto, Model model) {
        this.evaluationIndexRateService.update(dto, Long.parseLong(rateId));

        return "redirect:/admin/evalRate?year=" + dto.getYear();
    }

    @PostMapping("/{rateId}/delete")
    public String delete(@PathVariable("rateId") String rateId, @RequestParam(required = false) String year, Model model) {
        this.evaluationIndexRateService.deleteById(Long.parseLong(rateId));

        return "redirect:/admin/evalRate?year=" + year;
    }

    /**
     * 각 상위메뉴의 active 표시하기 위한 controller 별 active 값 set
     *
     * @param model
     */
    @ModelAttribute
    public void setActive(Model model) {
        model.addAttribute("activeMenu", "evalManagement");
        model.addAttribute("subActiveMenu", "evalRate");
    }

    @ModelAttribute
    public void setSchedules(Model model) {
        List<EvaluationSchedule> schedules = evaluationScheduleService.findAllByLastYear();
        EvaluationSchedule currentSchedule = schedules.stream().filter(EvaluationSchedule::isActive)
                .reduce((first, second) -> second).orElse(null);

        model.addAttribute("schedules", schedules);
        model.addAttribute("currentSchedule", currentSchedule);
    }
}
