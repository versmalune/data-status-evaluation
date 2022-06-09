package kr.co.data_status_evaluation.controller.admin;

import kr.co.data_status_evaluation.model.EvaluationResult;
import kr.co.data_status_evaluation.model.EvaluationSchedule;
import kr.co.data_status_evaluation.model.InstitutionCategory;
import kr.co.data_status_evaluation.model.Pagination;
import kr.co.data_status_evaluation.model.dto.SchedulesDto;
import kr.co.data_status_evaluation.model.enums.EvaluationStatus;
import kr.co.data_status_evaluation.model.search.ObjectionRequestSearchParam;
import kr.co.data_status_evaluation.service.EvaluationFieldService;
import kr.co.data_status_evaluation.service.EvaluationResultService;
import kr.co.data_status_evaluation.service.EvaluationScheduleService;
import kr.co.data_status_evaluation.service.InstitutionCategoryService;
import kr.co.data_status_evaluation.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/admin/objections")
@RequiredArgsConstructor
public class AdminObjectionRequestController {

    private final EvaluationScheduleService evaluationScheduleService;
    private final EvaluationResultService evaluationResultService;
    private final EvaluationFieldService evaluationFieldService;
    private final InstitutionCategoryService institutionCategoryService;

    @GetMapping("")
    public String index(@PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
                        ObjectionRequestSearchParam searchParam, Model model) {
        List<String> evlYears = evaluationFieldService.findYears();
        if (StringUtils.isNullOrEmpty(searchParam.getYear())) {
            searchParam.setYear(evlYears.get(0));
        }
        String currentYear = searchParam.getYear();
        int currentYearToInt = Integer.parseInt(currentYear);

        List<EvaluationSchedule> schedules = evaluationScheduleService.findAllByYear(currentYear, Sort.by(Sort.Direction.ASC, "beginDt"));
        SchedulesDto schedulesDto = new SchedulesDto(schedules);
        Page<EvaluationResult> evaluationResults = evaluationResultService.findBySearchParam(pageable, searchParam);

        Pagination pagination = new Pagination(pageable);
        pagination.setTotalPages(evaluationResults.getTotalPages());
        pagination.setTotalElements(evaluationResults.getTotalElements());
        pagination.setQuery(searchParam.getQueryParams());

        List<InstitutionCategory> categoryList = this.institutionCategoryService.findAll();

        model.addAttribute("categories", categoryList);
        model.addAttribute("evlYears", evlYears);
        model.addAttribute("currentYear", currentYear);
        model.addAttribute("currentYearToInt", currentYearToInt);
        model.addAttribute("schedules", schedulesDto);
        model.addAttribute("evaluationResults", evaluationResults.getContent());
        model.addAttribute("searchParam", searchParam);
        model.addAttribute("pagination", pagination);

        return "app/admin/objections/index";
    }

    @PostMapping("/update")
    @Transactional
    public ResponseEntity<?> update(@ModelAttribute EvaluationResult eval) {
        EvaluationResult evaluationResult = evaluationResultService.findById(eval.getId());
        if (!eval.getObjectionReview().equals(evaluationResult.getObjectionReview())) {
            evaluationResult.setObjectionReview(eval.getObjectionReview());
        }

        if (!StringUtils.isNullOrEmpty(eval.getObjectionReview()) && EvaluationStatus.OBJ_START.equals(evaluationResult.getProcessStatus())) {
            evaluationResult.setProcessStatus(EvaluationStatus.OBJ_END);
        }
        evaluationResultService.save(evaluationResult);
        
        return ResponseEntity.ok().build();
    }

    /**
     * 각 상위메뉴의 active 표시하기 위한 controller 별 active 값 set
     *
     * @param model
     */
    @ModelAttribute
    public void setActive(Model model) {
        model.addAttribute("activeMenu", "objection");
    }
}
