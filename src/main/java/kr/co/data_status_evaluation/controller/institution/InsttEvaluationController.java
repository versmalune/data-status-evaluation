package kr.co.data_status_evaluation.controller.institution;

import kr.co.data_status_evaluation.model.*;
import kr.co.data_status_evaluation.model.dto.AverageComparisonByFieldDto;
import kr.co.data_status_evaluation.model.dto.EvaluationResultPercentRankDto;
import kr.co.data_status_evaluation.model.dto.SchedulesDto;
import kr.co.data_status_evaluation.model.dto.ScoreSummaryByCategoryDto;
import kr.co.data_status_evaluation.model.dw.InstitutionResultFact;
import kr.co.data_status_evaluation.model.enums.EvaluationStatus;
import kr.co.data_status_evaluation.model.enums.EvaluationType;
import kr.co.data_status_evaluation.model.enums.MaterialType;
import kr.co.data_status_evaluation.model.helper.CurrentUser;
import kr.co.data_status_evaluation.model.vo.InsttIndexVo;
import kr.co.data_status_evaluation.service.*;
import kr.co.data_status_evaluation.service.dw.FactService;
import kr.co.data_status_evaluation.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Controller
@RequestMapping("/instt/eval")
@RequiredArgsConstructor
public class InsttEvaluationController {

    private final AccountService accountService;
    private final EvaluationTargetInstitutionService evaluationTargetInstitutionService;
    private final InstitutionService institutionService;
    private final FactService factService;
    private final EvaluationResultService evaluationResultService;
    private final EvaluationFieldService evaluationFieldService;
    private final EvaluationIndexService evaluationIndexService;
    private final EvaluationScheduleService evaluationScheduleService;
    private final EvaluationIndexRateService evaluationIndexRateService;
    private final FileService fileService;
    private final MaterialService materialService;

    @GetMapping("/instt/{id}")
    public String show(@PathVariable String id,
                       @RequestParam(required = false) String evlYear,
                       @RequestParam(required = false) String active,
                       Model model) {

        Long insttId = Long.parseLong(id);
        List<String> evlYears = evaluationFieldService.findYears();
        if (StringUtils.isNullOrEmpty(evlYear)) {
            evlYear = evlYears.get(0);
        }

        if (active == null) {
            active = "0";
        }

        Institution institution = institutionService.findById(insttId); //??????
        String year = evlYear;
        InstitutionResultFact institutionResultFact = factService.findInstitutionResultByInstitutionIdAndYear(insttId, evlYear).orElseGet(() -> new InstitutionResultFact(institution.getId(), year));

        List<InsttIndexVo> resultList = evaluationIndexService.getInsttIndexVoByInstitutionAndYear(institution, evlYear); // ????????????, ????????????
        Map<String, Integer> rowspanMapByField = new HashMap<>(); // ????????? ?????? ?????? ?????? ????????? rowspan ????????? ?????? map
        /* ???????????? ?????? Mapping - Start */
        List<Long> currentIndexIds = evaluationIndexService.getIndicesIds(resultList);
        List<EvaluationResultPercentRankDto> rankList = evaluationResultService.findPercentRank(evlYear, currentIndexIds, null);

        for (InsttIndexVo insttIndexVo : resultList) {
            if (!Objects.isNull(insttIndexVo.getType()) && insttIndexVo.getType().equals(EvaluationType.QUALITATIVE)) {
                insttIndexVo.setQualitativeRankByRate(rankList);
            }
            String fieldName = insttIndexVo.getEvaluationField().getName();
            rowspanMapByField.put(fieldName, rowspanMapByField.getOrDefault(fieldName, 0) + 1);
        }
        /* ???????????? ?????? Mapping - End */
        // ?????? ?????? ?????? ??????
        List<Long> indexIds = resultList.stream().map(InsttIndexVo::getId).collect(Collectors.toList());
        List<Material> indexMaterials = this.materialService.findAllByMtlTyAndAtchTrgtIds(MaterialType.FORM, indexIds);

        List<EvaluationField> evaluationFields = evaluationFieldService.findByYear(evlYear, Sort.by(Sort.Direction.ASC, "no"));  // ???????????? ?????? ex) ???????????? ??????
        List<EvaluationSchedule> schedules = evaluationScheduleService.findAllByYear(evlYear, Sort.by(Sort.Direction.ASC, "beginDt")); // ???????????? ?????? ????????? ?????????
        SchedulesDto schedulesDto = new SchedulesDto(schedules);
        EvaluationSchedule currentSchedule = schedules.stream().filter(EvaluationSchedule::isActive)
                .reduce((first, second) -> second).orElse(null);
        boolean isScheduleFinished = false;
        for (EvaluationSchedule schedule : schedules) {
            if (schedule.getName().equals(EvaluationStatus.END)) {
                isScheduleFinished = schedule.isDone();
            }
        }

        model.addAttribute("institutionResultFact", institutionResultFact);
        model.addAttribute("evaluationIndices", resultList);
        model.addAttribute("evaluationFields", evaluationFields);
        model.addAttribute("currentEvlYear", evlYear);
        model.addAttribute("evlYears", evlYears);
        model.addAttribute("institution", institution);
        model.addAttribute("active", active);
        model.addAttribute("schedules", schedulesDto);
        model.addAttribute("currentSchedule", currentSchedule);
        model.addAttribute("isScheduleFinished", isScheduleFinished);
        model.addAttribute("indexMaterials", indexMaterials);
        model.addAttribute("rowspanMapByField", rowspanMapByField);

        return "app/institution/evaluations/index";

    }

    @PostMapping("/instt/{insttId}/index/{indexId}/upload")
    @ResponseBody
    public ResponseEntity<?> upload(@PathVariable String insttId,
                                    @PathVariable String indexId,
                                    @RequestParam(required = false) MultipartFile[] uploadFiles,
                                    @RequestParam(required = false) String objection,
                                    @RequestParam(required = false) String evlYear) {

        Long insttIdToLong = Long.valueOf(insttId);
        Long indexIdToLong = Long.valueOf(indexId);

        try {
            Institution institution = institutionService.findById(insttIdToLong);
            EvaluationIndex evaluationIndex = evaluationIndexService.findById(indexIdToLong);
            EvaluationResult evaluationResult = evaluationResultService.findByInstitutionAndYearAndEvaluationIndex(institution, evlYear, evaluationIndex);

            if (Objects.isNull(evaluationResult)) {
                evaluationResult = new EvaluationResult();

                evaluationResult.setInstitution(institution);
                evaluationResult.setEvaluationIndex(evaluationIndex);
                evaluationResult.setProcessStatus(EvaluationStatus.NONE);
                evaluationResult.setYear(evlYear);

                evaluationResultService.save(evaluationResult);
            }

            evaluationResult.setObjection(objection);
            if (!StringUtils.isNullOrEmpty(objection) && EvaluationStatus.P1_END.equals(evaluationResult.getProcessStatus())) {
                evaluationResult.setProcessStatus(EvaluationStatus.OBJ_START);
            }

            if (!Objects.isNull(uploadFiles) && uploadFiles.length > 0) {
                evaluationResultService.uploadFile(uploadFiles, evaluationResult);
            } else {
                evaluationResultService.save(evaluationResult);
            }

            return ResponseEntity.ok().build();

        } catch (IOException e) {
            e.printStackTrace();

            return ResponseEntity.internalServerError().build();

        } catch (Exception e) {
            return ResponseEntity.badRequest().build();

        }

    }

    @PostMapping("/instt/{insttId}/index/{indexId}/idxFileupload")
    public String idxFileUpload(@PathVariable String insttId,
                                @PathVariable String indexId,
                                @RequestParam(required = false) MultipartFile[] uploadFiles,
                                @RequestParam("active") String active,
                                @RequestParam("evlYear") String year,
                                RedirectAttributes redirect) throws IOException {

        Long insttIdToLong = Long.valueOf(insttId);
        Long indexIdToLong = Long.valueOf(indexId);

        Institution institution = institutionService.findById(insttIdToLong);
        EvaluationIndex evaluationIndex = evaluationIndexService.findById(indexIdToLong);
        EvaluationResult evaluationResult = evaluationResultService.findByInstitutionAndYearAndEvaluationIndex(institution, year, evaluationIndex);

        if (Objects.isNull(evaluationResult)) {
            evaluationResult = new EvaluationResult();

            evaluationResult.setInstitution(institution);
            evaluationResult.setEvaluationIndex(evaluationIndex);
            evaluationResult.setProcessStatus(EvaluationStatus.START);
            evaluationResult.setYear(year);

            evaluationResultService.save(evaluationResult);
        }

        evaluationResultService.uploadFile(uploadFiles, evaluationResult);

        redirect.addAttribute("evlYear", year);
        redirect.addAttribute("active", active);

        return "redirect:/instt/eval/instt/" + insttId;

    }

    @GetMapping("/instt/{insttId}/evlResult")
    public String result(@PathVariable("insttId") String insttId,
                         @RequestParam("evlYear") String year,
                         Model model) {
        Institution institution = institutionService.findById(Long.parseLong(insttId)); //??????
        List<String> evlYears = evaluationFieldService.findYears();
        if (StringUtils.isNullOrEmpty(year)) {
            year = evlYears.get(0);
        }
        InstitutionResultFact institutionResultFact = this.factService.findInstitutionResultByInstitutionIdAndYear(Long.parseLong(insttId), year).orElseGet(null);

        Integer currentYear = Integer.parseInt(year);
        Integer previousYear = currentYear - 1;

        Map<EvaluationField, Float> fieldScores = new LinkedHashMap<>();
        Map<EvaluationField, Float> fieldNaScores = new LinkedHashMap<>();
        Float totalScore = 0.0F;
        Float scoreWithoutEtc = 0.0F;
        Float standardScoreWithoutEtc = 0.0F;
        int fieldTotalScore = 0;
        Float etcScore = 0.0F;
        List<InsttIndexVo> resultList = evaluationIndexService.getInsttIndexVoByInstitutionAndYear(institution, year); // year ????????????, ????????????
        Map<String, Integer> rowspanMapByField = new HashMap<>(); // ????????? ?????? ?????? ?????? ????????? rowspan ????????? ?????? map
        resultList.forEach(result -> {
            String fieldName = result.getEvaluationField().getName();
            rowspanMapByField.put(fieldName, rowspanMapByField.getOrDefault(fieldName, 0) + 1);
        });
        List<EvaluationField> evaluationFields = evaluationFieldService.findByYear(year, Sort.by(Sort.Direction.ASC, "no"));  // ???????????? ?????? ex) ???????????? ??????
        /* ???????????? ?????? Mapping */
        List<Long> currentIndexIds = evaluationIndexService.getIndicesIdByYear(resultList, year);
        List<EvaluationResultPercentRankDto> rankList = evaluationResultService.findPercentRank(year, currentIndexIds, null);

        Map<EvaluationField, Float> previousFieldScores = new LinkedHashMap<>();
        Map<EvaluationField, Float> previousFieldNaScores = new LinkedHashMap<>();
        Float previousTotalScore = 0.0F;
        Float previousScoreWithoutEtc = 0.0F;
        Float previousStandardScoreWithoutEtc = 0.0F;
        int previousFieldTotalScore = 0;
        Float previousEtcScore = 0.0F;
        List<InsttIndexVo> previousResultList = evaluationIndexService.getInsttIndexVoByInstitutionAndYear(institution, previousYear.toString()); // year ?????? ????????????, ????????????
        Map<String, Integer> previousRowspanMapByField = new HashMap<>(); // ????????? ?????? ?????? ?????? ????????? rowspan ????????? ?????? map
        previousResultList.forEach(result -> {
            String fieldName = result.getEvaluationField().getName();
            previousRowspanMapByField.put(fieldName, previousRowspanMapByField.getOrDefault(fieldName, 0) + 1);
        });
        List<EvaluationField> previousEvaluationFields = evaluationFieldService.findByYear(previousYear.toString(), Sort.by(Sort.Direction.ASC, "no"));  // ???????????? ?????? ex) ???????????? ??????
        /* ???????????? ?????? Mapping */
        List<Long> previousIndexIds = evaluationIndexService.getIndicesIdByYear(previousResultList, previousYear.toString());
        List<EvaluationResultPercentRankDto> previousRankList = evaluationResultService.findPercentRank(previousYear.toString(), previousIndexIds, null);

        List<EvaluationSchedule> schedules = evaluationScheduleService.findAllByYear(year, Sort.by(Sort.Direction.ASC, "beginDt")); // ???????????? ?????? ????????? ?????????
        EvaluationSchedule currentSchedule = schedules.stream().filter(EvaluationSchedule::isActive)
                .reduce((first, second) -> second).orElse(null);
        if (Objects.isNull(currentSchedule)) {
            currentSchedule = schedules.stream().filter(EvaluationSchedule::isDone)
                    .reduce((first, second) -> second).orElse(null);
        }
        boolean isScheduleFinished = false;
        for (EvaluationSchedule schedule : schedules) {
            if (schedule.getName().equals(EvaluationStatus.END)) {
                isScheduleFinished = schedule.isDone();
            }
        }

        // ?????? fieldScores ????????? ??? ???????????? ??????
        for (EvaluationField field : evaluationFields) {
            if (!field.getName().contains("??????")) {
                fieldTotalScore += field.getScore();
                fieldNaScores.put(field, fieldNaScores.getOrDefault(field, 0.0F));
            }
            fieldScores.put(field, fieldScores.getOrDefault(field, 0.0F));
        }

        // ?????? fieldScores ????????? ??? ???????????? ??????
        for (EvaluationField field : previousEvaluationFields) {
            if (!field.getName().contains("??????")) {
                previousFieldTotalScore += field.getScore();
                previousFieldNaScores.put(field, previousFieldNaScores.getOrDefault(field, 0.0F));
            }
            previousFieldScores.put(field, previousFieldScores.getOrDefault(field, 0.0F));
        }

        // ?????? ???????????? ??????
        for (InsttIndexVo insttIndexVo : resultList) {
            EvaluationField evaluationField = insttIndexVo.getEvaluationField();
            String scoreToString = insttIndexVo.getResult().getScore();
            /* Field ??? score, ?????? ?????? */
            if (Objects.isNull(scoreToString)) {
                Float naScore = Float.parseFloat(insttIndexVo.getPerfectScore(institution.getCategoryByYear(year)));
                fieldNaScores.put(evaluationField, fieldNaScores.getOrDefault(evaluationField, 0.0F) + naScore);
            } else {
                Float score = Float.parseFloat(scoreToString);
                fieldScores.put(evaluationField, fieldScores.getOrDefault(evaluationField, 0.0F) + score);
                if (!evaluationField.getName().contains("??????")) {
                    scoreWithoutEtc += score;
                } else {
                    etcScore += score;
                }
                /* ???????????? ?????? Mapping */
                if (insttIndexVo.getType() != null && insttIndexVo.getType().equals(EvaluationType.QUALITATIVE)) {
                    insttIndexVo.setQualitativeRankByRate(rankList);
                }
            }
        }

        Float fieldTotalNaScore = fieldNaScores.values().stream().reduce(0.0F, Float::sum);
        // ?????? ??? N/A ?????? = ??? ?????? ?????? / (??? ?????? - ??? N/A ?????? ??????) * ?????? ??? N/A ??????
        for (EvaluationField field : fieldNaScores.keySet()) {
            Float naScore = scoreWithoutEtc / (fieldTotalScore - fieldTotalNaScore) * fieldNaScores.get(field);
            fieldScores.put(field, fieldScores.getOrDefault(field, 0.0F) + naScore);
        }
        // ?????? ??? ??????(????????? ??????, N/A?????? ??????) ??????
        for (Map.Entry<EvaluationField, Float> entry : fieldScores.entrySet()) {
            if (!entry.getKey().getName().contains("??????")) {
                standardScoreWithoutEtc += entry.getValue();
            }
        }
        totalScore = standardScoreWithoutEtc + etcScore;
        if (currentYear == 2018) {
            totalScore = institutionResultFact.getScore();
        }

        // ?????? ???????????? ??????
        for (InsttIndexVo insttIndexVo : previousResultList) {
            EvaluationField evaluationField = insttIndexVo.getEvaluationField();
            String scoreToString = insttIndexVo.getResult().getScore();
            /* Field ??? score, ?????? ?????? */
            if (Objects.isNull(scoreToString)) {
                Float naScore = Float.parseFloat(insttIndexVo.getPerfectScore(institution.getCategoryByYear(year)));
                previousFieldNaScores.put(evaluationField, previousFieldNaScores.getOrDefault(evaluationField, 0.0F) + naScore);
            } else {
                Float score = Float.parseFloat(scoreToString);
                previousFieldScores.put(evaluationField, previousFieldScores.getOrDefault(evaluationField, 0.0F) + score);
                if (!evaluationField.getName().contains("??????")) {
                    previousScoreWithoutEtc += score;
                } else {
                    previousEtcScore += score;
                }
                /* ???????????? ?????? Mapping */
                if (insttIndexVo.getType() != null && insttIndexVo.getType().equals(EvaluationType.QUALITATIVE)) {
                    insttIndexVo.setQualitativeRankByRate(previousRankList);
                }
            }
        }

        Float previousFieldTotalNaScore = previousFieldNaScores.values().stream().reduce(0.0F, Float::sum);
        for (EvaluationField field : previousFieldNaScores.keySet()) {
            Float naScore = previousScoreWithoutEtc / (previousFieldTotalScore - previousFieldTotalNaScore) * previousFieldNaScores.get(field);
            previousFieldScores.put(field, previousFieldScores.getOrDefault(field, 0.0F) + naScore);
        }
        // ?????? ??? ??????(????????? ??????, N/A?????? ??????) ??????
        for (Map.Entry<EvaluationField, Float> entry : previousFieldScores.entrySet()) {
            if (!entry.getKey().getName().contains("??????")) {
                previousStandardScoreWithoutEtc += entry.getValue();
            }
        }
        previousTotalScore = previousStandardScoreWithoutEtc + previousEtcScore;

        // ??????????????? ?????? ??????
        double totalAverageByCategory = 0;
        int institutionSize = 0;
        Map<String, ScoreSummaryByCategoryDto> scoreSummaryByCategory = this.institutionService.getScoreSummaryByCategory(institution.getCategoryByYear(year).getId(), year, null);
        for (ScoreSummaryByCategoryDto value : scoreSummaryByCategory.values()) {
            totalAverageByCategory += value.getSum();
            institutionSize = value.getCount();
        }
        totalAverageByCategory /= institutionSize;

        model.addAttribute("institution", institution);
        model.addAttribute("institutionResultFact", institutionResultFact);
        model.addAttribute("evaluationIndices", resultList);
        model.addAttribute("rowspanMapByField", rowspanMapByField);
        model.addAttribute("evaluationFields", evaluationFields);
        model.addAttribute("totalAverageByCategory", totalAverageByCategory);
        model.addAttribute("scoreSummaryByCategory", scoreSummaryByCategory);
        model.addAttribute("fieldScores", fieldScores);
        model.addAttribute("fieldTotalScore", fieldTotalScore);
        model.addAttribute("totalScore", totalScore);
        model.addAttribute("scoreWithoutEtc", scoreWithoutEtc);
        model.addAttribute("standardScoreWithoutEtc", standardScoreWithoutEtc);
        model.addAttribute("schedules", schedules);
        model.addAttribute("currentSchedule", currentSchedule);
        model.addAttribute("isScheduleFinished", isScheduleFinished);
        model.addAttribute("evlYears", evlYears);
        model.addAttribute("currentEvlYear", Integer.parseInt(year));
        model.addAttribute("currentEvlYearStr", year);
        // ?????? ?????????
        model.addAttribute("previousIndices", previousResultList);
        model.addAttribute("previousRowspanMapByField", previousRowspanMapByField);
        model.addAttribute("previousScoreWithoutEtc", previousScoreWithoutEtc);
        model.addAttribute("previousStandardScoreWithoutEtc", previousStandardScoreWithoutEtc);
        model.addAttribute("previousTotalScore", previousTotalScore);

        return "app/institution/evaluations/result";
    }


    @PostMapping("/instt/{insttId}/result/{resultId}/file/{fileId}/fileDelete")
    @ResponseBody
    public ResponseEntity<?> deleteFiles(@PathVariable String insttId,
                                         @PathVariable String resultId,
                                         @PathVariable String fileId) {
        try {
            Optional<File> file = fileService.findById(Long.parseLong(fileId));
            EvaluationResult result = evaluationResultService.findById(Long.parseLong(resultId));
            fileService.deleteFile(file.get(), result);

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }

    }

    @GetMapping("/stats/comparison/{id}")
    public ResponseEntity<?> getStats(@PathVariable("id") String insttId, @RequestParam("evlYear") String year) {
        Institution institution = this.institutionService.findById(Long.parseLong(insttId));

        Map<String, ScoreSummaryByCategoryDto> scoreByField = this.institutionService.getScoreSummaryByCategory(institution.getCategoryByYear(year).getId(), year, institution.getId());
        Map<String, ScoreSummaryByCategoryDto> scoreSummaryByCategory = this.institutionService.getScoreSummaryByCategory(institution.getCategoryByYear(year).getId(), year, null);
        if (scoreSummaryByCategory.isEmpty()) {
            return new ResponseEntity<>("There is no data", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        AverageComparisonByFieldDto dto = new AverageComparisonByFieldDto(scoreByField, scoreSummaryByCategory);
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @GetMapping("/stats/distributionByField/{id}")
    public ResponseEntity<?> getDistributionByField(@PathVariable("id") String insttId, @RequestParam("evlYear") String year) {
        Institution institution = this.institutionService.findById(Long.parseLong(insttId));
        Map<BigInteger, Map<String, Float>> distributionByField = this.institutionService.getDistributionByField(institution.getCategoryByYear(year).getId(), year);
        this.evaluationFieldService.findByYear(year, Sort.by(Sort.Direction.ASC, "no")).forEach(field -> {
            distributionByField.forEach((k, v) -> {
                if (!v.containsKey(field.getName())) {
                    v.put(field.getName(), 0.0F);
                }
                v.put("total", v.getOrDefault("total", 0.0F) + v.getOrDefault(field.getName(), 0.0F));
            });
        });
        if (distributionByField.isEmpty()) {
            return new ResponseEntity<>("There is no data", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(distributionByField, HttpStatus.OK);
    }

    @GetMapping("/stats/distributionByScore/{id}")
    public ResponseEntity<?> getDistributionByScore(@PathVariable("id") String insttId, @RequestParam("evlYear") String year) {
        Institution institution = this.institutionService.findById(Long.parseLong(insttId));
        Map<BigInteger, Float> distributionByCategory = this.institutionService.getDistributionByScore(institution.getCategoryByYear(year).getId(), year);
        Map<String, Integer> results = new LinkedHashMap<>();
        String[] titles = {"80??? ??????", "80??? ??????", "60??? ??????", "40??? ??????", "20??? ??????"};
        for (String title : titles) {
            results.put(title, 0);
        }
        int totalFieldScore = 0;
        for (EvaluationField field : this.evaluationFieldService.findByYear(year, Sort.by(Sort.Direction.ASC, "no"))) {
            if (!field.getName().contains("??????")) {
                totalFieldScore += field.getScore();
            }
        }
        distributionByCategory.forEach((k, v) -> {
            if (v > 80) {
                results.put(titles[0], results.get(titles[0]) + 1);
            }
            if (60 < v && v <= 80) {
                results.put(titles[1], results.get(titles[1]) + 1);
            }
            if (40 < v && v <= 60) {
                results.put(titles[2], results.get(titles[2]) + 1);
            }
            if (20 < v && v <= 40) {
                results.put(titles[3], results.get(titles[3]) + 1);
            }
            if (v < 20) {
                results.put(titles[4], results.get(titles[4]) + 1);
            }
        });
        if (distributionByCategory.isEmpty()) {
            return new ResponseEntity<>("There is no data", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(results, HttpStatus.OK);
    }


    @GetMapping("/form/{id}")
    public ResponseEntity<?> downloadFormFiles(@PathVariable Long id) {
        List<Material> materialList = materialService.findAllByMtlTyAndAtchTrgtId(MaterialType.FORM, id);
        List<ResponseEntity<Resource>> responseEntities = new ArrayList<>();
        if (materialList != null && !materialList.isEmpty()) {
            return fileService.downloadFile(materialList.get(0).getFile().getId());
        } else {
            return ResponseEntity.ok().body("???????????? ????????? ???????????? ????????????.");
        }
    }

    /**
     * ??? ??????????????? active ???????????? ?????? controller ??? active ??? set
     *
     * @param model
     */
    @ModelAttribute
    public void setActive(Model model) {
        model.addAttribute("activeMenu", "eval");
    }

    @ModelAttribute
    public void isTargetInstitution(Model model, @CurrentUser Account account) {
        Account currentUser = accountService.findOneWithInstitutionById(account.getId());
        List<String> evlYears = evaluationFieldService.findYears();
        String evlYear = evlYears.get(0);

        boolean isTargetInstitution = false;
        if (!Objects.isNull(currentUser)
                && currentUser.isInstitution()
                && !Objects.isNull(currentUser.getInstitution())) {
            isTargetInstitution = evaluationTargetInstitutionService.isTargetInstitutionByYearAndInsttId(evlYear, currentUser.getInstitution());
        }

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("isTargetInstitution", isTargetInstitution);
    }
}
