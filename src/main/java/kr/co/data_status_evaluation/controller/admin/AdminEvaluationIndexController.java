package kr.co.data_status_evaluation.controller.admin;

import kr.co.data_status_evaluation.model.*;
import kr.co.data_status_evaluation.model.enums.EvaluationStatus;
import kr.co.data_status_evaluation.model.enums.EvaluationType;
import kr.co.data_status_evaluation.model.enums.MaterialType;
import kr.co.data_status_evaluation.model.search.EvaluationIndexSearchParam;
import kr.co.data_status_evaluation.model.vo.EvaluationIndexVo;
import kr.co.data_status_evaluation.service.EvaluationFieldService;
import kr.co.data_status_evaluation.service.EvaluationIndexService;
import kr.co.data_status_evaluation.service.InstitutionCategoryService;
import kr.co.data_status_evaluation.service.MaterialService;
import kr.co.data_status_evaluation.util.ModelConverter;
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
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/evalIndex")
public class AdminEvaluationIndexController {

    private final EvaluationIndexService evaluationIndexService;
    private final EvaluationFieldService evaluationFieldService;
    private final InstitutionCategoryService institutionCategoryService;
    private final MaterialService materialService;

    @GetMapping("")
    public String index(@PageableDefault(sort = "no", direction = Sort.Direction.ASC) Pageable pageable, EvaluationIndexSearchParam searchParam, Model model) {
        List<String> evalYears = this.evaluationFieldService.findYears();
        if (StringUtils.isNullOrEmpty(searchParam.getYear())) {
            searchParam.setYear(evalYears.get(0));
        }
        Page<EvaluationIndex> evalIndices = this.evaluationIndexService.findBySearchParam(pageable, searchParam);
        Pagination pagination = new Pagination(pageable);
        pagination.setTotalPages(evalIndices.getTotalPages());
        pagination.setTotalElements(evalIndices.getTotalElements());
        pagination.setQuery(searchParam.getQueryParams());
        List<InstitutionCategory> institutionCategories = this.institutionCategoryService.findAll();

        List<String> isDeletableIndices = new ArrayList<>();

        for (EvaluationIndex index : evalIndices) {
            List<EvaluationResult> results = index.getResults();
            List<EvaluationResult> filteredResults = results.stream().filter(
                    evaluationResult -> evaluationResult.getProcessStatus() == EvaluationStatus.END
            ).collect(Collectors.toList());

            if (filteredResults.isEmpty())
                isDeletableIndices.add("Y");
            else
                isDeletableIndices.add("N");
        }

        List<EvaluationIndex> indices = this.evaluationIndexService.findAllByIndexRateNotInAndTypeAndYear(EvaluationType.QUALITATIVE, searchParam.getYear());
        if (!indices.isEmpty()) {
            model.addAttribute("message", "정성지표 중 상대평가 비율설정이 되어있지 않은 지표가 존재합니다.");
        }
        model.addAttribute("searchParam", searchParam);
        model.addAttribute("pagination", pagination);
        model.addAttribute("evalIndices", evalIndices);
        model.addAttribute("isDeletableIndices", isDeletableIndices);
        model.addAttribute("evalYears", evalYears);
        model.addAttribute("institutionCategories", institutionCategories);

        return "app/admin/indices/index";
    }

    @GetMapping("/{id}")
    public String show(@PathVariable String id, Model model) {
        EvaluationIndex evaluationIndex = this.evaluationIndexService.findById(Long.parseLong(id));
        EvaluationIndexVo evaluationIndexVo = ModelConverter.convertObject(evaluationIndex, EvaluationIndexVo.class);
        evaluationIndexVo.setDateRange(evaluationIndex.getBeginDt(), evaluationIndex.getEndDt());
        evaluationIndexVo.setYear(evaluationIndex.getEvaluationField().getYear());

        List<EvaluationField> evaluationFields = this.evaluationFieldService.findAll(Sort.by(Sort.Direction.DESC, "year").and(Sort.by(Sort.Direction.ASC, "no")));
        List<String> distinctYears = evaluationFields.stream().map(EvaluationField::getYear).distinct().collect(Collectors.toList());
        List<InstitutionCategory> institutionCategories = this.institutionCategoryService.findAll();
        // 양식 자료
        Material material = materialService.findFirstByMtlTyAndAtchTrgtId(MaterialType.FORM, evaluationIndex.getId());

        model.addAttribute("evalIndex", evaluationIndexVo);
        model.addAttribute("evalFields", evaluationFields);
        model.addAttribute("evalYears", distinctYears);
        model.addAttribute("institutionCategories", institutionCategories);
        model.addAttribute("material", material);

        return "app/admin/indices/show";
    }

    @GetMapping("/new")
    public String newEvaluationIndex(Model model) {
        EvaluationIndexVo evaluationIndex = new EvaluationIndexVo();
        List<EvaluationField> evaluationFields = this.evaluationFieldService.findAll(Sort.by(Sort.Direction.DESC, "year").and(Sort.by(Sort.Direction.ASC, "no")));
        List<String> distinctYears = evaluationFields.stream().map(EvaluationField::getYear).distinct().collect(Collectors.toList());
        List<InstitutionCategory> institutionCategories = this.institutionCategoryService.findAll();

        model.addAttribute("evalIndex", evaluationIndex);
        model.addAttribute("evalFields", evaluationFields);
        model.addAttribute("evalYears", distinctYears);
        model.addAttribute("institutionCategories", institutionCategories);

        return "app/admin/indices/new";
    }

    @PostMapping("")
    @Validated
    public String post(@ModelAttribute("evalIndex") @Valid EvaluationIndexVo vo, BindingResult bindingResult, Model model) throws ParseException {
        if (bindingResult.hasErrors()) {
            List<EvaluationField> evaluationFields = this.evaluationFieldService.findAll();
            List<String> distinctYears = evaluationFields.stream().map(EvaluationField::getYear).distinct().collect(Collectors.toList());
            List<InstitutionCategory> institutionCategories = this.institutionCategoryService.findAll();

            model.addAttribute("evalFields", evaluationFields);
            model.addAttribute("evalYears", distinctYears);
            model.addAttribute("institutionCategories", institutionCategories);
            return "app/admin/indices/new";
        }
        this.evaluationIndexService.save(vo);
        return "redirect:/admin/evalIndex?year=" + vo.getYear();
    }

    @PostMapping("/{id}")
    @Validated
    public String put(@PathVariable String id, @ModelAttribute("evalIndex") @Valid EvaluationIndexVo vo, BindingResult bindingResult, Model model) throws ParseException {
        if (bindingResult.hasErrors()) {
            List<EvaluationField> evaluationFields = this.evaluationFieldService.findAll();
            List<String> distinctYears = evaluationFields.stream().map(EvaluationField::getYear).distinct().collect(Collectors.toList());
            List<InstitutionCategory> institutionCategories = this.institutionCategoryService.findAll();

            model.addAttribute("evalFields", evaluationFields);
            model.addAttribute("evalYears", distinctYears);
            model.addAttribute("institutionCategories", institutionCategories);
            return "app/admin/indices/show";
        }
        vo.setId(Long.parseLong(id));
        this.evaluationIndexService.update(vo);
        return "redirect:/admin/evalIndex";
    }


    @PostMapping("/{id}/delete")
    public String delete(@PathVariable String id) {
        this.evaluationIndexService.deleteById(Long.parseLong(id));
        return "redirect:/admin/evalIndex";
    }


    @GetMapping("/{fieldId}/indices")
    @ResponseBody
    public ResponseEntity<?> findIndexByFieldAndYear(@PathVariable("fieldId") String fieldId,
                                                     @RequestParam(name = "index", required = false) String indexId) {
        try {
            EvaluationField field = this.evaluationFieldService.findById(Long.parseLong(fieldId));
            List<InstitutionCategory> categories = this.institutionCategoryService.findAll();
            List<EvaluationIndex> indices = field.getEvaluationIndices();
            Map<Long, Float> scoreByCategoryMap = new HashMap<>();
            for (EvaluationIndex index : indices) {
                if (indexId != null && index.getId().equals(Long.parseLong(indexId))) {
                    continue;
                }

                for (InstitutionCategory category : categories) {
                    Float score = index.getTotalScore(category);
                    Long categoryId = category.getId();
                    scoreByCategoryMap.put(categoryId, scoreByCategoryMap.getOrDefault(categoryId, 0.0F) + score);
                }
            }

            return new ResponseEntity<>(scoreByCategoryMap, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("cannot find field", HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    /**
     * 각 상위메뉴의 active 표시하기 위한 controller 별 active 값 set
     *
     * @param model
     */
    @ModelAttribute
    public void setActive(Model model) {
        model.addAttribute("activeMenu", "evalManagement");
        model.addAttribute("subActiveMenu", "evalIndex");
    }
}
