package kr.co.data_status_evaluation.controller.admin;

import cubrid.jdbc.driver.CUBRIDException;
import kr.co.data_status_evaluation.model.InstitutionCategory;
import kr.co.data_status_evaluation.model.InstitutionCategoryDetail;
import kr.co.data_status_evaluation.model.dto.InstitutionCategoryDto;
import kr.co.data_status_evaluation.service.InstitutionCategoryDetailService;
import kr.co.data_status_evaluation.service.InstitutionCategoryService;
import kr.co.data_status_evaluation.util.ModelConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/institutions")
public class AdminInstitutionController {

    private final InstitutionCategoryService institutionCategoryService;
    private final InstitutionCategoryDetailService institutionCategoryDetailService;

    @GetMapping("/categories")
    public String index(Model model) {
        List<InstitutionCategory> categories = this.institutionCategoryService.findAll();
        model.addAttribute("categories", categories);
        model.addAttribute("category", new InstitutionCategory());

        return "app/admin/institutions/categories/index";
    }

    @PostMapping("/categories")
    public String post(InstitutionCategoryDto category, Model model) throws Exception {
        this.institutionCategoryService.upsert(category);
        return "redirect:/admin/institutions/categories";
    }

//    @PostMapping("/categories/{id}")
//    public String put(@PathVariable("id") String id, InstitutionCategory category, Model model) throws Exception {
//        category.setId(Long.parseLong(id));
//        this.institutionCategoryService.update(category);
//
//        return "redirect:/admin/institutions/categories";
//    }

    @PostMapping("/categories/{id}/delete")
    public String delete(@PathVariable("id") String id, Model model) {
        try{
            this.institutionCategoryService.deleteById(Long.parseLong(id));
            return "redirect:/admin/institutions/categories";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/admin/institutions/categories?error=1";
        }
    }

    @GetMapping("/categories/new")
    public String newCategory(Model model) {
        model.addAttribute("category", new InstitutionCategory());
        return "app/admin/institutions/categories/show";
    }

    @GetMapping("/categories/{id}")
    public String getCategory(@PathVariable("id") Long id, Model model) {
        InstitutionCategory category = this.institutionCategoryService.findByIdNotOptional(id);
        if (category == null) return "/commons/error";
        InstitutionCategoryDto categoryDto = ModelConverter.convertObject(category, InstitutionCategoryDto.class);
        model.addAttribute("category", categoryDto);
        return "app/admin/institutions/categories/show";
    }

    /**
     * 각 상위메뉴의 active 표시하기 위한 controller 별 active 값 set
     *
     * @param model
     */
    @ModelAttribute
    public void setActive(Model model) {
        model.addAttribute("activeMenu", "account");
        model.addAttribute("subActiveMenu", "institutionCategory");
    }
}
