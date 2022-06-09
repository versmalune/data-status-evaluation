package kr.co.data_status_evaluation.controller.admin;

import kr.co.data_status_evaluation.model.EvaluationSchedule;
import kr.co.data_status_evaluation.model.EvaluationTargetInstitution;
import kr.co.data_status_evaluation.model.InstitutionCategory;
import kr.co.data_status_evaluation.model.Pagination;
import kr.co.data_status_evaluation.model.dto.EvaluationTargetInstitutionDto;
import kr.co.data_status_evaluation.model.enums.EvaluationStatus;
import kr.co.data_status_evaluation.model.search.EvaluationTargetInstitutionSearchParam;
import kr.co.data_status_evaluation.model.vo.InsttCategoryCountVo;
import kr.co.data_status_evaluation.service.EvaluationFieldService;
import kr.co.data_status_evaluation.service.EvaluationScheduleService;
import kr.co.data_status_evaluation.service.EvaluationTargetInstitutionService;
import kr.co.data_status_evaluation.service.InstitutionCategoryService;
import kr.co.data_status_evaluation.util.DateUtils;
import kr.co.data_status_evaluation.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/targetInstitutions")
public class AdminEvaluationTargetInstitutionController {

    private final EvaluationFieldService evaluationFieldService;
    private final EvaluationScheduleService evaluationScheduleService;
    private final EvaluationTargetInstitutionService evaluationTargetInstitutionService;
    private final InstitutionCategoryService institutionCategoryService;
    private final ResourceLoader resourceLoader;

    @GetMapping("")
    public String index(@ModelAttribute EvaluationTargetInstitutionSearchParam searchParam,
                        Model model) {
        List<String> evlYears = evaluationFieldService.findYears();
        // year 파라미터가 없을 경우 default year 세팅
        if (StringUtils.isNullOrEmpty(searchParam.getYear())) {
            searchParam.setYear(evlYears.get(0));
        }
        String evlYear = searchParam.getYear();

        List<InsttCategoryCountVo> targetGroups = evaluationTargetInstitutionService.findAllInsttCategoryCountVoByYear(evlYear);
        int totalTargetCount = targetGroups.stream().reduce(0, (total, second) -> total + second.getCount(), Integer::sum);
        List<InsttCategoryCountVo> targetYGroups = evaluationTargetInstitutionService.findAllInsttCategoryCountVoByYearAndTrgtYnEqualY(evlYear);
        int totalTargetYCount = targetYGroups.stream().reduce(0, (total, second) -> total + second.getCount(), Integer::sum);

        String isUploadable = "false";
        EvaluationSchedule evaluationSchedule = evaluationScheduleService.findByYearAndName(evlYear, EvaluationStatus.NONE);
        if (!Objects.isNull(evaluationSchedule)) {
            Date now = new Date();
            Date beginDt = evaluationSchedule.getBeginDt();
            Date endDt = evaluationSchedule.getEndDt();
            endDt = DateUtils.getDateWithLastTime(endDt);
            if (beginDt.before(now) && endDt.after(now))
                isUploadable = "true";

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String uploadableBeginDt = simpleDateFormat.format(beginDt);
            String uploadableEndDt = simpleDateFormat.format(endDt);
            model.addAttribute("uploadableBeginDt", uploadableBeginDt);
            model.addAttribute("uploadableEndDt", uploadableEndDt);
        }

        model.addAttribute("evlYears", evlYears);
        model.addAttribute("searchParam", searchParam);
        model.addAttribute("targetGroups", targetGroups);
        model.addAttribute("totalTargetCount", totalTargetCount);
        model.addAttribute("targetYGroups", targetYGroups);
        model.addAttribute("totalTargetYCount", totalTargetYCount);
        model.addAttribute("isUploadable", isUploadable);

        return "app/admin/targetInstitutions/index";
    }

    @GetMapping("/category/{categoryId}")
    public String getCategory(@PageableDefault(page = 0, size = 50, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
                              @PathVariable("categoryId") Long categoryId,
                              @ModelAttribute("searchParam") EvaluationTargetInstitutionSearchParam searchParam,
                              Model model) {
        List<String> evlYears = evaluationFieldService.findYears();

        Optional<InstitutionCategory> insttCategory = institutionCategoryService.findById(categoryId);

        Page<EvaluationTargetInstitution> targets = evaluationTargetInstitutionService.findAllBySearchParam(pageable, searchParam);
        Pagination pagination = new Pagination(pageable);

        pagination.setTotalPages(targets.getTotalPages());
        pagination.setTotalElements(targets.getTotalElements());
        pagination.setQuery(searchParam.getQueryParams());

        model.addAttribute("pagination", pagination);
        model.addAttribute("searchParam", searchParam);
        model.addAttribute("evlYears", evlYears);
        model.addAttribute("insttCategory", insttCategory.get());
        model.addAttribute("targets", targets.getContent());

        return "app/admin/targetInstitutions/show";
    }

    @PostMapping("/bulkUpdate")
    public ResponseEntity<?> bulkUpdateTrgtInsttYn(@RequestBody List<EvaluationTargetInstitutionDto> targets,
                                                   Model model) {
        if (!Objects.isNull(targets) && !targets.isEmpty()) {
            this.evaluationTargetInstitutionService.bulkUpdateTrgtInsttYn(targets);
        }

        return ResponseEntity.ok().build();
    }

    @PostMapping("/excelUpload")
    @ResponseBody
    public ResponseEntity<?> upload(MultipartFile file,
                                    @RequestParam String year) {
        Map<String, String> responseBody = new HashMap<>();
        try {
            evaluationTargetInstitutionService.excelUpload(file, year);
            responseBody.put("status", "200");
            responseBody.put("message", "데이터 업로드가 성공하였습니다.");

            return new ResponseEntity<>(responseBody, HttpStatus.OK);
        } catch (ParseException e) {
            responseBody.put("status", "500");
            if (e.getErrorOffset() == 1) {
                responseBody.put("message", "엑셀 업로드에 실패하였습니다.\n기관 유형 코드 매핑 오류.");
            } else if (e.getErrorOffset() == 2) {
                responseBody.put("message", "엑셀 업로드에 실패하였습니다.\n기관 코드 매핑 오류.");
            } else {
                responseBody.put("message", "엑셀 업로드에 실패하였습니다.\n양식 혹은 데이터를 확인해주세요.");
            }

            return new ResponseEntity<>(responseBody, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (NullPointerException e) {
            responseBody.put("status", "400");
            responseBody.put("message", "잘못된 요청입니다.\n파일이 존재하지 않습니다.");

            return new ResponseEntity<>(responseBody, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            responseBody.put("status", "500");
            responseBody.put("message", "업로드 중 에러가 발생하였습니다.");

            return new ResponseEntity<>(responseBody, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/templateDownload")
    public ResponseEntity<?> downloadTemplate() {
        Resource resource = resourceLoader.getResource("classpath:static/files/eval_trgt_instt_template.xlsx");
        try {
            InputStreamResource inputStreamResource = new InputStreamResource(resource.getInputStream());
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + "template.xlsx")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(inputStreamResource);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/excelDownload")
    public ResponseEntity<?> downloadAll(@RequestParam String year) {
        try {
            InputStreamResource resource = new InputStreamResource(evaluationTargetInstitutionService.databaseToExcel(year));

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + "평가대상_기관_" + year + "년.xlsx")
                    .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                    .body(resource);

        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

//    @GetMapping("/excelDownload/catogoryId/{catogoryId}")
//    public ResponseEntity<?> downloadByCategory(@RequestParam String year) {
//        try {
//            InputStreamResource resource = new InputStreamResource(evaluationTargetInstitutionService.databaseToExcel());
//
//            return ResponseEntity.ok()
//                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + "평가대상_기관.xlsx")
//                    .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
//                    .body(resource);
//
//        } catch (IOException e) {
//            return ResponseEntity.internalServerError().build();
//        }
//    }

    /**
     * 각 상위메뉴의 active 표시하기 위한 controller 별 active 값 set
     *
     * @param model
     */
    @ModelAttribute
    public void setActive(Model model) {
        model.addAttribute("activeMenu", "targetInstitutions");
    }
}
