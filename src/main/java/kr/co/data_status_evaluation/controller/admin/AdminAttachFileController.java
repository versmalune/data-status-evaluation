package kr.co.data_status_evaluation.controller.admin;

import kr.co.data_status_evaluation.model.EvaluationField;
import kr.co.data_status_evaluation.model.search.AttachFileSearchParam;
import kr.co.data_status_evaluation.model.vo.AttachFileCategoryVo;
import kr.co.data_status_evaluation.model.vo.AttachFileIndexVo;
import kr.co.data_status_evaluation.service.EvaluationFieldService;
import kr.co.data_status_evaluation.service.EvaluationIndexService;
import kr.co.data_status_evaluation.service.InstitutionCategoryService;
import kr.co.data_status_evaluation.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/admin/attachFiles")
@RequiredArgsConstructor
public class AdminAttachFileController {

    private final EvaluationFieldService evaluationFieldService;
    private final EvaluationIndexService evaluationIndexService;
    private final InstitutionCategoryService institutionCategoryService;

    @GetMapping("")
    public String index(AttachFileSearchParam searchParam, Model model) {
        if (StringUtils.isNullOrEmpty(searchParam.getTab())) {
            searchParam.setTab("category");
        }
        List<String> evlYears = evaluationFieldService.findYears();
        if (StringUtils.isNullOrEmpty(searchParam.getYear())) {
            searchParam.setYear(evlYears.get(0));
        }
        String currentYear = searchParam.getYear();

        // 기관유형 탭
        List<AttachFileCategoryVo> attachFileCategoryVos = institutionCategoryService.findAllAttachFileCategoryVoByYear(searchParam.getYear());

        // 지표 탭
        List<EvaluationField> fields = this.evaluationFieldService.findAllByYear(currentYear);
        List<AttachFileIndexVo> attachFileIndexVos = evaluationIndexService.findAllAttachFileIndexVoBySearchParam(searchParam);

        model.addAttribute("searchParam", searchParam);
        model.addAttribute("evlYears", evlYears);
        model.addAttribute("evlYear", searchParam.getYear());
        model.addAttribute("attachFileCategoryVos", attachFileCategoryVos);

        model.addAttribute("fields", fields);
        model.addAttribute("attachFileIndexVos", attachFileIndexVos);

        return "app/admin/attachFiles/index";
    }

    /**
     * 각 상위메뉴의 active 표시하기 위한 controller 별 active 값 set
     *
     * @param model
     */
    @ModelAttribute
    public void setActive(Model model) {
        model.addAttribute("activeMenu", "attachFile");
    }
}
