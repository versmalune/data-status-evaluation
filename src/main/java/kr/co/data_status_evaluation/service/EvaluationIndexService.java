package kr.co.data_status_evaluation.service;

import kr.co.data_status_evaluation.model.*;
import kr.co.data_status_evaluation.model.dw.IndexResultFact;
import kr.co.data_status_evaluation.model.enums.EvaluationStatus;
import kr.co.data_status_evaluation.model.enums.EvaluationType;
import kr.co.data_status_evaluation.model.enums.MaterialType;
import kr.co.data_status_evaluation.model.key.RelevantInstitutionKey;
import kr.co.data_status_evaluation.model.search.AttachFileSearchParam;
import kr.co.data_status_evaluation.model.search.EvaluationIndexSearchParam;
import kr.co.data_status_evaluation.model.vo.AttachFileIndexVo;
import kr.co.data_status_evaluation.model.vo.EvaluationIndexVo;
import kr.co.data_status_evaluation.model.vo.InsttIndexVo;
import kr.co.data_status_evaluation.repository.EvaluationIndexRepository;
import kr.co.data_status_evaluation.service.dw.FactService;
import kr.co.data_status_evaluation.specification.EvaluationIndexSpecification;
import kr.co.data_status_evaluation.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileSystemUtils;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.math.BigInteger;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidParameterException;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EvaluationIndexService {

    private final EvaluationIndexRepository evaluationIndexRepository;
    private final EvaluationFieldService evaluationFieldService;
    private final EvaluationResultService evaluationResultService;
    private final InstitutionService institutionService;
    private final FactService factService;
    private final EntityManager em;
    private final MaterialService materialService;

    public EvaluationIndex findById(Long id) {
        return this.evaluationIndexRepository.findById(id).orElseThrow(InvalidParameterException::new);
    }

    public List<EvaluationIndex> findAll() {
        return this.evaluationIndexRepository.findAll();
    }

    public List<EvaluationIndex> findAll(Sort sort) {
        return this.evaluationIndexRepository.findAll(sort);
    }

    public Page<EvaluationIndex> findAll(Pageable pageable) {
        return this.evaluationIndexRepository.findAll(pageable);
    }

    public List<EvaluationIndex> findAllByYear(String year) {
        EvaluationIndexSearchParam searchParam = new EvaluationIndexSearchParam();
        searchParam.setYear(year);
        Specification specification = Specification.where(EvaluationIndexSpecification.search(searchParam));
        return this.evaluationIndexRepository.findAll(specification);
    }

    public Page<EvaluationIndex> findBySearchParam(Pageable pageable, EvaluationIndexSearchParam searchParam) {
        Specification specification = Specification.where(EvaluationIndexSpecification.search(searchParam));
        return this.evaluationIndexRepository.findAll(specification, pageable);
    }

    public List<EvaluationIndex> findByYear(String year) {
        EvaluationIndexSearchParam searchParam = new EvaluationIndexSearchParam();
        searchParam.setYear(year);
        Specification specification = Specification.where(EvaluationIndexSpecification.search(searchParam));
        return this.evaluationIndexRepository.findAll(specification);
    }

    public List<EvaluationIndex> findAllByType(EvaluationType type) {
        return this.evaluationIndexRepository.findAllByType(type);
    }

    public List<EvaluationIndex> findAllByIndexRateNotInAndTypeAndYear(EvaluationType type, String year) {
        String sql = "SELECT idx_id FROM tb_rev_evl_idx_idx_rate";
        Query query = em.createNativeQuery(sql);
        List<Object> objects = query.getResultList();
        List<Long> ids = objects.stream()
                .map(result -> {
                    BigInteger id = (BigInteger) result;
                    return id.longValue();
                }).collect(Collectors.toList());

        return this.findAllByTypeAndYear(type, year).stream()
                .filter(index -> !ids.contains(index.getId()))
                .collect(Collectors.toList());
    }

    public List<EvaluationIndex> findAllByTypeAndYear(EvaluationType type, String year) {
        EvaluationIndexSearchParam searchParam = new EvaluationIndexSearchParam();
        searchParam.setYear(year);
        searchParam.setType(type);
        Specification specification = Specification.where(EvaluationIndexSpecification.search(searchParam));
        return this.evaluationIndexRepository.findAll(specification);
    }

    @Transactional
    public void save(EvaluationIndex evaluationIndex) {
        this.evaluationIndexRepository.save(evaluationIndex);
    }

    @Transactional
    public void save(EvaluationIndexVo vo) throws ParseException {
        EvaluationIndex evaluationIndex = new EvaluationIndex(vo);

        String year = vo.getYear();
        if (StringUtils.isNullOrEmpty(vo.getYear())) {
            year = String.valueOf(LocalDate.now().getYear());
        }
        EvaluationField evaluationField = evaluationFieldService.findByNoAndYear(vo.getFieldNo(), year);
        evaluationIndex.setEvaluationField(evaluationField);
        this.save(evaluationIndex);
        evaluationField.getEvaluationIndices().add(evaluationIndex);

        List<EvaluationResult> results = new ArrayList<>();
        List<IndexResultFact> indexResultFacts = new ArrayList<>();
        for (RelevantInstitution relevantInstitution : vo.getRelevantInstitutions()) {
            List<Institution> institutions = this.institutionService.findAllByTargetAndCategoryIdAndYear(relevantInstitution.getInstitutionCategory().getId(), year);
            for (Institution institution : institutions) {
                EvaluationResult result = new EvaluationResult();
                IndexResultFact indexResultFact = new IndexResultFact(institution.getId(), evaluationIndex.getId(), evaluationField.getId(), year);
                indexResultFact.setScore(0F);
                indexResultFacts.add(indexResultFact);

                result.setInstitution(institution);
                result.setEvaluationIndex(evaluationIndex);
                result.setYear(year);
                result.setScore("0");
                result.setProcessStatus(EvaluationStatus.NONE);
                if (!evaluationIndex.isAttachFile()) {
                    result.setProcessStatus(EvaluationStatus.START);
                }
                results.add(result);
            }
        }

        this.factService.saveAllOfIndexResult(indexResultFacts);
        this.evaluationResultService.saveAll(results);
        evaluationIndex.setResults(results);
        // 양식자료 업로드
        if (vo.getFormFile().getSize() > 0) {
            materialService.save(MaterialType.FORM, evaluationIndex.getId(), vo.getFormFile());
        }
    }

    @Transactional
    public void update(EvaluationIndexVo vo) throws ParseException {
        EvaluationIndex evaluationIndex = this.findById(vo.getId());
        List<InstitutionCategory> beforeCategories = evaluationIndex.getRelevantInstitutions().stream().map(RelevantInstitution::getInstitutionCategory).collect(Collectors.toList());
        List<RelevantInstitution> relevantInstitutions = vo.getRelevantInstitutions();
        for (RelevantInstitution relevantInstitution : relevantInstitutions) {
            if (!beforeCategories.contains(relevantInstitution.getInstitutionCategory())) {
                relevantInstitution.setId(new RelevantInstitutionKey(evaluationIndex, relevantInstitution.getInstitutionCategory()));
                relevantInstitution.setEvaluationIndex(evaluationIndex);
                evaluationIndex.getRelevantInstitutions().add(relevantInstitution);
            }

            RelevantInstitution originReleventInstitution = evaluationIndex.getRelevantInstitutions().stream().filter(el -> el.getInstitutionCategory().equals(relevantInstitution.getInstitutionCategory())).findFirst().get();
            originReleventInstitution.setYn(relevantInstitution.isYn());
        }

        String year = vo.getYear();
        if (StringUtils.isNullOrEmpty(vo.getYear())) {
            year = String.valueOf(LocalDate.now().getYear());
        }
        EvaluationField evaluationField = evaluationFieldService.findByNoAndYear(vo.getFieldNo(), year);
        evaluationIndex.setAll(vo);
        evaluationIndex.setEvaluationField(evaluationField);
        // 양식 자료 DB 정보 삭제
        List<Material> deletedMaterialList = null;
        if (vo.hasDeletedMaterialIds()) {
            deletedMaterialList = new ArrayList<>();
            for (String i : vo.getDeleteMaterialIds()) {
                Long id = Long.parseLong(i);
                Material deletedMaterial = materialService.findById(id).get();
                deletedMaterialList.add(deletedMaterial);
                materialService.deleteById(id);
            }
        }
        // 추가양식 자료 저장
        if (vo.getFormFile().getSize() > 0) {
            materialService.save(MaterialType.FORM, evaluationIndex.getId(), vo.getFormFile());
        }
        // 양식자료 실제 파일 삭제
        if (deletedMaterialList != null) {
            for (Material deletedMaterial : deletedMaterialList) {
                Path path = Paths.get(deletedMaterial.getFile().getRelativePath());
                try {
                    FileSystemUtils.deleteRecursively(path);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        // 지표의 증빙자료 첨부 가능 여부에 따른 result 평가상태 초기화
        for (EvaluationResult result : evaluationIndex.getResults()) {
            if (result.getYear().equals(year)) {
                if (evaluationIndex.isAttachFile()) {
                    result.setProcessStatus(EvaluationStatus.NONE);
                } else {
                    result.setProcessStatus(EvaluationStatus.START);
                }
            }
        }

        this.save(evaluationIndex);
    }

    public List<EvaluationIndex> findByCategoryAndYear(InstitutionCategory category, String year) {
        EvaluationIndexSearchParam searchParam = new EvaluationIndexSearchParam();
        searchParam.setYear(year);
        searchParam.setInstitutionType(category.getCode());
        Specification specification = Specification.where(EvaluationIndexSpecification.search(searchParam));

        return this.evaluationIndexRepository.findAll(specification);
    }

    public List<InsttIndexVo> getInsttIndexVoByInstitutionAndYear(Institution institution, String year) {
        List<EvaluationIndex> evaluationIndices = this.findByCategoryAndYear(institution.getCategoryByYear(year), year);
        List<InsttIndexVo> resultList = new ArrayList<>();

        for (EvaluationIndex i : evaluationIndices) {
            EvaluationResult r = this.evaluationResultService.findByInstitutionAndYearAndEvaluationIndex(institution, year, i);
            if (r != null) {
                InsttIndexVo insttIndexVo = new InsttIndexVo(i, r);
                insttIndexVo.setScores(i.getScores(institution.getCategoryByYear(year)));
                resultList.add(insttIndexVo);
            }
        }

        return resultList;
    }

    public Map<Long, Map<Long, InsttIndexVo>> getAllInsttIndexVoByYear(String year) {
        List<EvaluationResult> results = this.evaluationResultService.findAllByYear(year);
        Map<Long, Map<Long, InsttIndexVo>> resultMap = new HashMap<>();
        results.forEach(result-> {
            EvaluationIndex index = result.getEvaluationIndex();
            Institution institution = result.getInstitution();
            InsttIndexVo insttIndexVo = new InsttIndexVo(index, result);
            insttIndexVo.setScores(index.getScores(institution.getCategoryByYear(year)));

            Map<Long, InsttIndexVo> institutionMap = resultMap.getOrDefault(index.getId(), new HashMap<>());
            institutionMap.put(institution.getId(), insttIndexVo);
            resultMap.put(index.getId(), institutionMap);
        });

        return resultMap;
    }

    @Transactional
    public void deleteById(Long id) {
        // TODO : 최종평가결과가 있으면 삭제 및 수정 안되도록 수정.
        EvaluationIndex index = this.findById(id);
        for (Account account : index.getAccounts()) {
            account.getIndices().remove(index);
        }
        this.evaluationIndexRepository.delete(index);
        this.factService.deleteAllOfIndexResultByIndexId(index.getId());
    }

    public List<Long> getIndicesIds(List<InsttIndexVo> evaluationIndices) {
        List<Long> ids = new ArrayList<>();
        for (InsttIndexVo index : evaluationIndices) {
            ids.add(index.getId());
        }
        return ids;
    }

    public List<Long> getIndicesIdByYear(List<InsttIndexVo> evaluationIndices, String year) {
        List<Long> ids = new ArrayList<>();
        for (InsttIndexVo index : evaluationIndices) {
            if (index.getEvaluationField().getYear().equals(year)) {
                ids.add(index.getId());
            }
        }
        return ids;
    }

    public List<AttachFileIndexVo> findAllAttachFileIndexVoBySearchParam(AttachFileSearchParam searchParam) {
        String sql = "SELECT  ei.id, ef.fld_nm, ei.evl_ty, ei.idx_no, ei.idx_nm, ei.atch_file_yn, count(fer.file_id)\n" +
                    "FROM    tb_rev_evl_idx ei\n" +
                    "        INNER JOIN  tb_rev_evl_fld ef\n" +
                    "            ON      ef.evl_yr = :year\n";
        if (!StringUtils.isNullOrEmpty(searchParam.getField())) {
            sql +=  "            AND     ef.id = :field\n";
        }
        if (!StringUtils.isNullOrEmpty(searchParam.getIndexNm())) {
            sql +=  "            AND     ei.idx_nm LIKE :indexNm\n";
        }
//        if (!StringUtils.isNullOrEmpty(searchParam.getAttachFile())) {
//            sql +=  "            AND     ei.atch_file_yn = :attachFile\n";
//        }
        sql +=      "            AND     ei.fld_id = ef.id\n" +
                    "        INNER JOIN  tb_rev_evl_result er\n" +
                    "            ON      ef.evl_yr = er.evl_yr\n" +
                    "            AND     ei.id = er.idx_id\n" +
                    "        LEFT JOIN  tb_rev_file_evl_result fer\n" +
                    "            ON      er.id = fer.evl_result_id\n" +
                    "GROUP BY ei.id, ef.fld_nm, ei.evl_ty, ei.idx_no, ei.idx_nm, ei.atch_file_yn\n" +
                    "ORDER BY ei.idx_no";
        Query query = em.createNativeQuery(sql);
        query.setParameter("year", searchParam.getYear());
        if (!StringUtils.isNullOrEmpty(searchParam.getField())) {
            query.setParameter("field", searchParam.getField());
        }
        if (!StringUtils.isNullOrEmpty(searchParam.getIndexNm())) {
            query.setParameter("indexNm", "%" + searchParam.getIndexNm() + "%");
        }
//        if (!StringUtils.isNullOrEmpty(searchParam.getAttachFile())) {
//            query.setParameter("attachFile", searchParam.getAttachFile());
//        }

        List<Object[]> objects = query.getResultList();
        List<AttachFileIndexVo> results = objects.stream()
                .map(result -> {
                    BigInteger idBigInteger = (BigInteger) result[0];
                    Long id = Objects.isNull(idBigInteger) ? null : idBigInteger.longValue();
                    String fieldNm = (String) result[1];
                    String evlType = (String) result[2];
                    String indexNo = (String) result[3];
                    String indexNm = (String) result[4];
                    Character attachFileCharacter = (Character) result[5];
                    String attachFile = attachFileCharacter.toString();
                    int fileCount = (int) result[6];
                    boolean hasAttachFile = fileCount > 0 ? true : false;

                    return new AttachFileIndexVo(id, fieldNm, evlType, indexNo, indexNm, attachFile, hasAttachFile);
                }).collect(Collectors.toList());

        return results;
    }
}
