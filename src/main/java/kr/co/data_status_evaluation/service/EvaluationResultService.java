package kr.co.data_status_evaluation.service;

import kr.co.data_status_evaluation.model.*;
import kr.co.data_status_evaluation.model.dto.EvaluationResultPercentRankDto;
import kr.co.data_status_evaluation.model.dto.ProgressStatusDto;
import kr.co.data_status_evaluation.model.dw.IndexResultFact;
import kr.co.data_status_evaluation.model.enums.EvaluationStatus;
import kr.co.data_status_evaluation.model.enums.EvaluationType;
import kr.co.data_status_evaluation.model.key.EvalutationResultTotalKey;
import kr.co.data_status_evaluation.model.search.ObjectionRequestSearchParam;
import kr.co.data_status_evaluation.model.vo.EvaluationResultWrap;
import kr.co.data_status_evaluation.repository.EvaluationResultRepository;
import kr.co.data_status_evaluation.repository.EvaluationResultTotalRepository;
import kr.co.data_status_evaluation.service.dw.FactService;
import kr.co.data_status_evaluation.specification.ObjectionRequestSpecification;
import kr.co.data_status_evaluation.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidParameterException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EvaluationResultService {

    @Value("${spring.servlet.multipart.location}")
    private String uploadPath;
    private final static String FILE_DIR = "evaluationResult";
    private final EvaluationResultRepository evaluationResultRepository;
    private final EvaluationResultTotalRepository evaluationResultTotalRepository;
    private final FactService factService;
    private final EntityManager em;
    private final LogService logService;
    private final AccountService accountService;

    public EvaluationResult findById(Long id) {
        return evaluationResultRepository.findById(id).orElseThrow(InvalidParameterException::new);
    }

    public List<EvaluationResult> findAll() {
        return evaluationResultRepository.findAll();
    }

    public List<EvaluationResult> findAllByYear(String year) {
        return evaluationResultRepository.findAllByYear(year);
    }

    public List<EvaluationResult> findAllByYearAndRaoPossible(String year) {
        return this.evaluationResultRepository.findAllByYearAndRaoPossible(year);
    }

    public Page<EvaluationResult> findAll(Pageable pageable) {
        return evaluationResultRepository.findAll(pageable);
    }

    public List<EvaluationResult> findAllByInstitutionAndYear(Institution institution, String year) {
        return evaluationResultRepository.findAllByInstitutionAndYear(institution, year);
    }

    public List<EvaluationResult> findBySearchParam(ObjectionRequestSearchParam searchParam) {
        Specification specification = Specification.where(ObjectionRequestSpecification.search(searchParam));
        return evaluationResultRepository.findAll(specification);
    }

    public Page<EvaluationResult> findBySearchParam(Pageable pageable, ObjectionRequestSearchParam searchParam) {
        Specification specification = Specification.where(ObjectionRequestSpecification.search(searchParam));
        return evaluationResultRepository.findAll(specification, pageable);
    }

    public List<EvaluationResultPercentRankDto> findPercentRank(String year, List<Long> indexIds, String type) {
        String sql = "SELECT r.idx_id, i.id, i.instt_cd, i.instt_nm, r.scr,\n" +
                "       CASE\n" +
                "           WHEN r.scr IS NULL THEN NULL\n" +
                "           ELSE PERCENT_RANK() OVER(PARTITION BY r.idx_id ORDER BY r.scr DESC) * 100\n" +
                "           END AS precent_rank,\n" +
                "       idx.evl_ty\n" +
                "FROM tb_rev_evl_result r\n" +
                "JOIN tb_rev_instt i\n" +
                "ON r.instt_id = i.id\n" +
                "JOIN tb_rev_evl_trgt_instt t\n" +
                "ON i.id = t.instt_id\n" +
                "JOIN tb_rev_evl_idx idx ON r.idx_id = idx.id\n" +
                "WHERE r.evl_yr = :year\n" +
                "AND t.evl_yr = :year\n" +
                "AND i.del_yn = 'N'\n" +
                "AND t.trgt_instt_yn = 'Y'\n" +
                " AND r.idx_id IN (:indices)\n";
        if (type != null) {
            sql += " AND i.instt_ty = :type";
        }
        Query query = em.createNativeQuery(sql);
        query.setParameter("year", year);
        query.setParameter("indices", indexIds);
        if (type != null) {
            query.setParameter("type", type);
        }
        List<Object[]> objects = query.getResultList();
        List<EvaluationResultPercentRankDto> results = objects.stream()
                .map(result -> {
                    BigDecimal scr = (BigDecimal) result[4];
                    Float fScr = Objects.isNull(scr) ? null : scr.floatValue();
                    EvaluationType evaluationType = Objects.isNull(result[6]) ? null : EvaluationType.valueOf((String) result[6]);

                    return new EvaluationResultPercentRankDto(
                            (BigInteger) result[0],
                            (BigInteger) result[1],
                            (String) result[2],
                            (String) result[3],
                            fScr,
                            (Double) result[5],
                            null,
                            evaluationType
                    );
                })
                .collect(Collectors.toList());

        return results;
    }

    public List<EvaluationResultPercentRankDto> findPercentRank(String year, Long id, String type) {
        List<Long> ids = new ArrayList<>();
        ids.add(id);

        return this.findPercentRank(year, ids, type);
    }

    public List<ProgressStatusDto> findProgressStatus() {
        String sql = "SELECT r.process_sttus_cd, count(instt_id)\n" +
                "FROM tb_rev_evl_result r\n" +
                "group by r.process_sttus_cd";
        Query query = em.createNativeQuery(sql);
        List<Object[]> objects = query.getResultList();
        List<ProgressStatusDto> results = objects.stream()
                .map(result -> new ProgressStatusDto(
                        (String) result[0],
                        (Integer) result[1]
                ))
                .collect(Collectors.toList());

        return results;
    }

    @Transactional
    public void save(EvaluationResultWrap wrap, List<EvaluationSchedule> schedules) {
        EvaluationSchedule currentSchedule = schedules.stream().filter(EvaluationSchedule::isActive)
                .reduce((first, second) -> second).orElse(null);
        if (Objects.isNull(currentSchedule)) {
            currentSchedule = schedules.stream().filter(EvaluationSchedule::isDone)
                    .reduce((first, second) -> second).orElse(null);
        }
        EvaluationStatus status = currentSchedule == null ?
                EvaluationStatus.NONE : currentSchedule.getName();
        List<IndexResultFact> facts = new ArrayList<>();

        for (EvaluationResult result : wrap.getEvaluationResultList()) {
            EvaluationResult original = this.findById(result.getId());
            result.setFiles(original.getFiles());
            if (Objects.isNull(result.getProcessStatus())) {
                result.setProcessStatus(EvaluationStatus.NONE);
            }
            if (Objects.isNull(original.getProcessStatus())) {
                original.setProcessStatus(EvaluationStatus.NONE);
            }

            Long institutionId = result.getInstitution().getId();
            Long indexId = result.getEvaluationIndex().getId();
            Long fieldId = result.getEvaluationIndex().getEvaluationField().getId();
            String year = result.getYear();

            // result의 평가상태를 변경한 경우 변경한 평가상태 값을 그대로 저장
            // result의 평가상태를 변경하지 않은 경우 조건에 따라 평가상태 자동계산 후 다음 상태로 변경
            if (result.getProcessStatus().equals(original.getProcessStatus())) {
                if (EvaluationStatus.START.equals(result.getProcessStatus())) {
                    if (!StringUtils.isNullOrEmpty(result.getOpinion())) {
                        if (result.getEvaluationIndex().isObjectionPossible()) {
                            result.setProcessStatus(EvaluationStatus.P1_END);
                        } else {
                            result.setProcessStatus(EvaluationStatus.OBJ_END);
                        }
                    }
                } else if (EvaluationStatus.OBJ_START.equals(result.getProcessStatus())) {
                    if (!StringUtils.isNullOrEmpty(result.getObjectionReview())) {
                        result.setProcessStatus(EvaluationStatus.OBJ_END);
                    }
                }
            }

            if(!result.getEvaluationIndex().isNaLevel() && StringUtils.isNullOrEmpty(result.getScore())) {
                result.setScore("0.0");
            }

            Optional<IndexResultFact> factOptional = this.factService.findIndexResultByInstitutionIdAndIndexIdAndYear(institutionId, indexId, year);
            IndexResultFact fact = factOptional.orElseGet(() -> new IndexResultFact(institutionId, indexId, fieldId, year));
            if(!result.getEvaluationIndex().isNaLevel() && StringUtils.isNullOrEmpty(result.getScore())) {
                fact.setScore(0.0F);
            } else {
                fact.setScore(result.getScoreToFloat());
            }
            facts.add(fact);
        }
        this.saveAll(wrap.getEvaluationResultList());
        this.factService.saveAllOfIndexResult(facts, status, currentSchedule.getYear());

        for (EvaluationResultTotal total : wrap.getEvaluationResultTotals()) {
            total.setId(new EvalutationResultTotalKey(total.getInstitution(), total.getEvaluationField()));
        }
        this.evaluationResultTotalRepository.saveAll(wrap.getEvaluationResultTotals());
    }

    @Transactional
    public void save(EvaluationResult evaluationIndex) {
        evaluationResultRepository.save(evaluationIndex);
    }

    @Transactional
    public void saveAll(List<EvaluationResult> evaluationResults) {
        this.evaluationResultRepository.saveAll(evaluationResults);
    }

    @Transactional
    public void bulkUpdateProcessStatusByYearInsttIdList(String year, List<Long> idList, String status) {
        evaluationResultRepository.bulkUpdateOfInstitutionResultProcessStatusByYearAndInsttIdList(year, idList, status);
    }

    public void deleteById(Long id) {
        evaluationResultRepository.deleteById(id);
    }

    public void deleteAll(List<EvaluationResult> results) {
        this.evaluationResultRepository.deleteAll(results);
    }

    @Transactional
    public void deleteAllByInstitutionIdAndYear(Long institutionId, String year) {
        this.evaluationResultRepository.deleteAllByInstitutionIdAndYear(institutionId, year);
    }

    public List<String> findYearsByInstt(Institution institution) {
        return evaluationResultRepository.findYearsByInstt(institution);
    }

    public EvaluationResult findByInstitutionAndYearAndEvaluationIndex(Institution institution, String year, EvaluationIndex evaluationIndex) {
        return evaluationResultRepository.findByInstitutionAndYearAndEvaluationIndex(institution, year, evaluationIndex);
    }


    @Transactional
    public void uploadFile(MultipartFile[] files, EvaluationResult evaluationResult) throws IOException {
        Set<File> fileSet = evaluationResult.getFiles();

        for (int i = 0; i < files.length; i++) {
            if (files[i].getSize() <= 0)
                continue;
            File file = new File("/" + FILE_DIR + "/" + evaluationResult.getId(), i + 1, files[i]);

            Path filePath = Paths.get(uploadPath, FILE_DIR, evaluationResult.getId().toString(), file.getFileNm());

            // 복사할 대상 폴더가 있는지 체크해서 없으면 생성
            if (!Files.exists(filePath.getParent())) {
                Files.createDirectories(filePath.getParent());
            }

            Files.copy(files[i].getInputStream(), filePath);
            if (Objects.isNull(fileSet)) {
                fileSet = new HashSet<>();
            }
            fileSet.add(file);
        }

        evaluationResult.setFiles(fileSet);
        if (EvaluationStatus.NONE.equals(evaluationResult.getProcessStatus()) && fileSet.size() > 0) {
            evaluationResult.setProcessStatus(EvaluationStatus.START);
        }
        evaluationResultRepository.save(evaluationResult);
        // 실태평가 증빙자료 등록 시 log
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Account account = accountService.findByUserId(userDetails.getUsername());
        logService.logEvalResult(evaluationResult, account);
    }

    public Map<String, Integer> getObjectionStatus(String year) {
        List<EvaluationResult> results = this.findAllByYearAndRaoPossible(year);;

        return this.getObjectionStatus(results);
    }

    public Map<String, Integer> getObjectionStatus(List<EvaluationResult> results) {
        Map<String, Integer> objectionMap = new LinkedHashMap<>();
        objectionMap.put("신청", 0);
        objectionMap.put("검토", 0);
        for (EvaluationResult result : results) {
            String objection = result.getObjection();
            if (!(StringUtils.isNullOrEmpty(objection) || objection.equals("없음"))) {
                if (StringUtils.isNullOrEmpty(result.getObjectionReview())) {
                    objectionMap.put("신청", objectionMap.getOrDefault("신청", 0) + 1);

                } else {
                    objectionMap.put("검토", objectionMap.getOrDefault("검토", 0) + 1);
                }
            }
        }
        return objectionMap;
    }

    public void remapEvalResult(Institution newInstt, Institution beforeInstt) {
        evaluationResultRepository.updateByBeforeInstt(newInstt.getId(), beforeInstt.getId());
    }

    public void remapEvalResultTotal(Institution newInstt, Institution beforeInstt) {
        evaluationResultTotalRepository.updateByBeforeInstt(newInstt.getId(), beforeInstt.getId());
    }
}