package kr.co.data_status_evaluation.controller.admin;

import kr.co.data_status_evaluation.model.EvaluationField;
import kr.co.data_status_evaluation.service.EvaluationFieldService;
import kr.co.data_status_evaluation.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/evalField")
public class AdminEvaluationFieldController {

    private final EvaluationFieldService evaluationFieldService;

    @GetMapping("")
    public String index(String year, Model model) {
        List<String> evlYears = evaluationFieldService.findYears();
        year = StringUtils.isNullOrEmpty(year) ? evlYears.get(0) : year;

        String seletedYear = year; // collection stream 내 변수 사용을 위한 effectively final temp variable 선언
        // 모든 평가분야 리스트
        List<EvaluationField> allFields = evaluationFieldService.findAll(Sort.by(Sort.Direction.DESC, "year").and(Sort.by(Sort.Direction.ASC, "no")));
        // year별 평가분야 리스트
        List<EvaluationField> filteredFields = allFields.stream().filter(el -> el.getYear().equals(seletedYear)).collect(Collectors.toList());
        // 평가분야 연도 그룹 리스트
        List<String> distinctYears = allFields.stream().map(EvaluationField::getYear).distinct().collect(Collectors.toList());
        // 생성 모달에 필요한 new entity 객체
        EvaluationField field = EvaluationField.getNewInstance();
//        // 평가분야 생성시 번호 중복확인
//        String noList = "";
//        if(filteredFields != null ) {
//            for (EvaluationField filteredField : filteredFields) {
//                noList += "," + filteredField.getNo();
//            }
//        }

        model.addAttribute("evalFields", filteredFields);
        model.addAttribute("evalYears", distinctYears);
        model.addAttribute("year", year);
        model.addAttribute("field", field);
//        model.addAttribute("noList", noList);

        return "app/admin/fields/index";
    }

    @PostMapping("")
    public String createField(EvaluationField field, Model model) {
        this.evaluationFieldService.save(field);

        return "redirect:/admin/evalField?year=" + field.getYear();
    }

    @PostMapping("/{id}")
    public String updateField(@PathVariable("id") String id, EvaluationField field, Model model) {
        field.setId(Long.parseLong(id));
        this.evaluationFieldService.update(field);

        return "redirect:/admin/evalField";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable("id") String id, Model model) {
        try {
            this.evaluationFieldService.deleteById(Long.parseLong(id));
            return "redirect:/admin/evalField";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/admin/evalField?error=1";
        }

    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getField(@PathVariable String id) {
        try {
            EvaluationField field = this.evaluationFieldService.findById(Long.parseLong(id));
            return new ResponseEntity<>(field, HttpStatus.OK);
        } catch (InvalidParameterException e) {
            e.printStackTrace();
            return new ResponseEntity<>("entity not found", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/checkDuplicate")
    public ResponseEntity<?> checkDuplicate(@RequestParam String year,
                                            @RequestParam int no,
                                            @RequestParam String name ) {
        try {
            boolean hasDuplicate = this.evaluationFieldService.hasDuplicateField(year, no, name);
            Map<String, Object> result = new HashMap<>();
            result.put("result", hasDuplicate);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (InvalidParameterException e) {
            e.printStackTrace();
            return new ResponseEntity<>("entity not found", HttpStatus.INTERNAL_SERVER_ERROR);
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
        model.addAttribute("subActiveMenu", "evalField");
    }
}
