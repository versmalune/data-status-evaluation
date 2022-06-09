package kr.co.data_status_evaluation.controller.admin;

import kr.co.data_status_evaluation.model.EvaluationSchedule;
import kr.co.data_status_evaluation.model.dto.EvaluationScheduleSummaryDto;
import kr.co.data_status_evaluation.model.enums.EvaluationStatus;
import kr.co.data_status_evaluation.model.vo.EvaluationScheduleVo;
import kr.co.data_status_evaluation.model.vo.EvaluationScheduleWrap;
import kr.co.data_status_evaluation.service.EvaluationFieldService;
import kr.co.data_status_evaluation.service.EvaluationScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.text.ParseException;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/evalSchedules")
public class AdminEvaluationScheduleController {

    private final EvaluationFieldService evaluationFieldService;
    private final EvaluationScheduleService evaluationScheduleService;

    @GetMapping("")
    public String getIndex(Model model) throws Exception {
        List<EvaluationScheduleSummaryDto> summaries = evaluationFieldService.findAllSummary();
        model.addAttribute("summaries", summaries);

        return "app/admin/schedules/index";
    }

    @GetMapping("/{year}")
    public String getShow(@PathVariable String year, Model model) throws Exception {
        List<EvaluationSchedule> schedules = this.evaluationScheduleService.findAllByYear(year, Sort.by(Sort.Direction.ASC, "beginDt"));
        EvaluationScheduleWrap wrap = new EvaluationScheduleWrap();
        wrap.setYear(year);
        if (!(schedules.size() > 0)) {
            List<EvaluationScheduleVo> scheduleVoList = wrap.getSchedules();

            EvaluationStatus[] evaluationStatusList = EvaluationStatus.values();
            for (EvaluationStatus status : evaluationStatusList) {
                EvaluationScheduleVo scheduleVo = new EvaluationScheduleVo();
                scheduleVo.setName(status);

                scheduleVoList.add(scheduleVo);
            }

            model.addAttribute("scheduleWrap", wrap);
            return "app/admin/schedules/new";
        }
        for (EvaluationSchedule schedule : schedules) {
            wrap.getSchedules().add(new EvaluationScheduleVo(schedule));
        }
        model.addAttribute("scheduleWrap", wrap);

        return "app/admin/schedules/show";
    }

    @GetMapping("/new")
    public String getNew(Model model) {
        EvaluationScheduleWrap wrap = new EvaluationScheduleWrap();

        List<EvaluationScheduleVo> scheduleVoList = wrap.getSchedules();

        EvaluationStatus[] evaluationStatusList = EvaluationStatus.values();
        for (EvaluationStatus status : evaluationStatusList) {
            EvaluationScheduleVo scheduleVo = new EvaluationScheduleVo();
            scheduleVo.setName(status);

            scheduleVoList.add(scheduleVo);
        }

        model.addAttribute("scheduleWrap", wrap);

        return "app/admin/schedules/new";
    }

    @PostMapping("")
    @Validated
    public String post(@ModelAttribute("scheduleWrap") @Valid EvaluationScheduleWrap wrap, BindingResult bindingResult, Model model) throws ParseException {
        if (bindingResult.hasErrors()) {
            return "app/admin/schedules/new";
        }

        List<EvaluationSchedule> schedules = this.evaluationScheduleService.findAllByYear(wrap.getYear());
        if (schedules.size()>0) {
            return "forward:/admin/evalSchedules/" + wrap.getYear();
        }

        this.evaluationScheduleService.save(wrap);

        return "redirect:/admin/evalSchedules";
    }

    @PostMapping("/{id}")
    public String put(EvaluationScheduleWrap wrap, @PathVariable String id) throws ParseException {
        this.evaluationScheduleService.update(wrap);

        return "redirect:/admin/evalSchedules";
    }

    /**
     * 각 상위메뉴의 active 표시하기 위한 controller 별 active 값 set
     *
     * @param model
     */
    @ModelAttribute
    public void setActive(Model model) {
        model.addAttribute("activeMenu", "evalManagement");
        model.addAttribute("subActiveMenu", "evalSchedules");
    }
}
