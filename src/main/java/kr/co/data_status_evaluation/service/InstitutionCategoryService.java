package kr.co.data_status_evaluation.service;

import kr.co.data_status_evaluation.model.*;
import kr.co.data_status_evaluation.model.dto.InstitutionCategoryDetailDto;
import kr.co.data_status_evaluation.model.dto.InstitutionCategoryDto;
import kr.co.data_status_evaluation.model.vo.AttachFileCategoryVo;
import kr.co.data_status_evaluation.repository.InstitutionCategoryDetailRepository;
import kr.co.data_status_evaluation.repository.InstitutionCategoryRepository;
import kr.co.data_status_evaluation.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InstitutionCategoryService {

    private final InstitutionCategoryRepository institutionCategoryRepository;
    private final InstitutionCategoryDetailRepository institutionCategoryDetailRepository;
    private final EvaluationIndexService evaluationIndexService;
    private final EvaluationScoreService evaluationScoreService;
    private final EntityManager entityManager;

    public List<InstitutionCategory> findAll() {
        return this.institutionCategoryRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
    }

    public Optional<InstitutionCategory> findById(Long id) {
        return this.institutionCategoryRepository.findById(id);
    }

    @Transactional
    public void upsert(InstitutionCategoryDto dto) throws Exception {
        // save category
        InstitutionCategory category = null;
        if (dto.getId() == null) { // insert
            category = new InstitutionCategory(dto);
        } else { // update
            Optional<InstitutionCategory> categoryOptional = this.findById(dto.getId());
            if (!categoryOptional.isPresent()) throw new Exception("No Category");
            category = categoryOptional.get();
            // delete detail if exists
            Set<Long> deleteCategoryDetailIdSet = this.differenceOf(category, dto);
            if (deleteCategoryDetailIdSet != null && !deleteCategoryDetailIdSet.isEmpty())
                institutionCategoryDetailRepository.deleteAllById(deleteCategoryDetailIdSet);
            category.setFromDto(dto);
        }
        institutionCategoryRepository.save(category);
        if (dto.getDetailCategories() != null
                && !dto.getDetailCategories().isEmpty()) {
            for (InstitutionCategoryDetailDto detailDto: dto.getDetailCategories()) {
                InstitutionCategoryDetail detail = null;
                if (detailDto.getId() == null) { // insert
                    detail = new InstitutionCategoryDetail(detailDto);
                    category.getDetailCategories().add(detail);
                } else { // update
                    Optional<InstitutionCategoryDetail> detailOptional = institutionCategoryDetailRepository.findById(detailDto.getId());
                    if (detailOptional.isPresent()) detail = detailOptional.get();
                    else throw new Exception();
                    detail.setFromDto(detailDto);
                }
                detail.setInstitutionCategory(category);
                institutionCategoryDetailRepository.save(detail);
            }
        }

        List<EvaluationIndex> indices = this.evaluationIndexService.findAll();
        for (EvaluationIndex index : indices) {
            List<EvaluationScore> indexScores = index.getScores();
            InstitutionCategory finalCategory = category;
            EvaluationScore isScoreExist = indexScores.stream().filter(score -> score.getInstitutionCategory().getId().equals(finalCategory.getId())).findFirst().orElse(null);
            if (isScoreExist != null) {
                break;
            }

            if (index.getType() == null) {
                EvaluationScore score = new EvaluationScore();
                score.setScore(0.0F);
                score.setEvaluationIndex(index);
                score.setInstitutionCategory(category);
                this.evaluationScoreService.save(score);
                index.getScores().add(score);
            } else {
                List<Integer> levels = indexScores.stream().map(EvaluationScore::getLevel).distinct().collect(Collectors.toList());
                for (Integer level : levels) {
                    EvaluationScore score = new EvaluationScore();
                    score.setLevel(level);
                    score.setScore(0.0F);
                    score.setEvaluationIndex(index);
                    score.setInstitutionCategory(category);
                    this.evaluationScoreService.save(score);
                    index.getScores().add(score);
                }
            }
        }
    }

    private Set<Long> differenceOf(InstitutionCategory category, InstitutionCategoryDto dto) {
        Set<Long> categoryIdSet = new HashSet<>();
        for (InstitutionCategoryDetail detail: category.getDetailCategories())
            categoryIdSet.add(detail.getId());
        Set<Long> categoryForUpsertIdSet = new HashSet<>();
        for (InstitutionCategoryDetailDto detail: dto.getDetailCategories())
            categoryForUpsertIdSet.add(detail.getId());
        categoryIdSet.removeAll(categoryForUpsertIdSet);
        return categoryIdSet;
    }

//    public void update(InstitutionCategory category) throws Exception {
//        InstitutionCategory institutionCategory = this.findById(category.getId()).orElseThrow(InvalidParameterException::new);
//        institutionCategory.setCode(category.getCode());
//        institutionCategory.setName(category.getName());
//        institutionCategory.setDescription(category.getDescription());
//        this.save(institutionCategory);
//    }

    @Transactional
    public void deleteById(Long id) {
        Optional<InstitutionCategory> optional = this.institutionCategoryRepository.findById(id);
        if (!optional.isPresent()) {
            return;
        }
        List<EvaluationIndex> indices = evaluationIndexService.findAll();
        for (EvaluationIndex index : indices) {
            RelevantInstitution relevantInstitution = index.getRelevantInstitutions().stream().filter(el -> el.getInstitutionCategory().getId().equals(id)).findFirst().orElse(null);
            if (relevantInstitution == null) {
                continue;
            }
            index.getRelevantInstitutions().remove(relevantInstitution);
            this.evaluationIndexService.save(index);
        }
        InstitutionCategory category = optional.get();
        this.evaluationScoreService.deleteAllByCategory(category);
        this.institutionCategoryRepository.delete(category);
    }

    public InstitutionCategory findByIdNotOptional(Long id) {
        return institutionCategoryRepository.findByIdNotOptional(id);
    }



    public Map<String, Long> getCtgyCdToCtgyIdMap() {
        String sql = "SELECT  category_cd, id\n" +
                    "FROM    tb_rev_instt_category";
        Query query = entityManager.createNativeQuery(sql);
        List<Object[]> objects = query.getResultList();
        Map<String, Long> results = new HashMap<>();
        objects.forEach(result -> {
            String ctgyCd = (String) result[0];
            BigInteger ctgyIdBigInteger = (BigInteger) result[1];
            ctgyCd = StringUtils.isNullOrEmpty(ctgyCd) ? null : ctgyCd.trim().toUpperCase();
            Long ctgyId = Objects.isNull(ctgyIdBigInteger) ? null : ctgyIdBigInteger.longValue();

            results.put(ctgyCd, ctgyId);
        });

        return results;
    }

    public List<AttachFileCategoryVo> findAllAttachFileCategoryVoByYear(String year) {
        String sql = "SELECT  ic.id, ic.category_cd, ic.category_nm, count(fer.file_id)\n" +
                    "FROM    tb_rev_evl_trgt_instt eti\n" +
                    "        INNER JOIN  tb_rev_instt i\n" +
                    "            ON      eti.evl_yr = :year\n" +
                    "            AND     eti.trgt_instt_yn = 'Y'\n" +
                    "            AND     i.del_yn = 'N'\n" +
                    "            AND     eti.instt_id = i.id\n" +
                    "        INNER JOIN tb_rev_instt_detail ind\n" +
                    "            ON      i.id = ind.instt_id AND ind.evl_yr = :year\n" +
                    "        INNER JOIN  tb_rev_instt_category ic\n" +
                    "            ON      ind.instt_category_id = ic.id\n" +
                    "        INNER JOIN  tb_rev_evl_result er\n" +
                    "            ON      eti.evl_yr = er.evl_yr\n" +
                    "            AND     i.id = er.instt_id\n" +
                    "        LEFT JOIN  tb_rev_file_evl_result fer\n" +
                    "            ON      er.id = fer.evl_result_id\n" +
                    "GROUP BY ic.id, ic.category_cd, ic.category_nm\n" +
                    "ORDER BY ic.id";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("year", year);

        List<Object[]> objects = query.getResultList();
        List<AttachFileCategoryVo> results = objects.stream()
                .map(result -> {
                    BigInteger idBigInteger = (BigInteger) result[0];
                    Long id = Objects.isNull(idBigInteger) ? null : idBigInteger.longValue();
                    String code = (String) result[1];
                    String name = (String) result[2];
                    int fileCount = (int) result[3];
                    boolean hasAttachFile = fileCount > 0 ? true : false;

                    return new AttachFileCategoryVo(id, code, name, hasAttachFile);
                }).collect(Collectors.toList());

        return results;
    }

    public List<File> findAllFileByYearAndCategoryId(String year, Long categoryId) {
        String sql = "SELECT  f.id, f.orgnl_file_nm, f.relative_path\n" +
                    "FROM    tb_rev_evl_trgt_instt eti\n" +
                    "        INNER JOIN  tb_rev_instt i\n" +
                    "            ON      eti.evl_yr = :year\n" +
                    "            AND     eti.trgt_instt_yn = 'Y'\n" +
                    "            AND     i.del_yn = 'N'\n" +
                    "            AND     eti.instt_id = i.id\n" +
                    "         INNER JOIN tb_rev_instt_detail ind\n" +
                    "            ON i.id = ind.instt_id\n" +
                    "            AND ind.evl_yr = :year\n" +
                    "            AND ind.instt_category_id = :categoryId\n" +
                    "        INNER JOIN  tb_rev_evl_result er\n" +
                    "            ON      eti.evl_yr = er.evl_yr\n" +
                    "            AND     i.id = er.instt_id\n" +
                    "        INNER JOIN  tb_rev_file_evl_result fer\n" +
                    "            ON      er.id = fer.evl_result_id\n" +
                    "        INNER JOIN  tb_rev_file f\n" +
                    "            ON      fer.file_id = f.id\n" +
                    "GROUP BY f.id, f.orgnl_file_nm, f.relative_path";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("year", year);
        query.setParameter("categoryId", categoryId);

        List<Object[]> objects = query.getResultList();
        List<File> results = objects.stream()
                .map(result -> {
                    BigInteger idBigInteger = (BigInteger) result[0];
                    Long id = Objects.isNull(idBigInteger) ? null : idBigInteger.longValue();
                    String orgnlFileNm = (String) result[1];
                    String relativePath = (String) result[2];

                    File file = new File();
                    file.setId(id);
                    file.setOrgnlFileNm(orgnlFileNm);
                    file.setRelativePath(relativePath);
                    return file;
                }).collect(Collectors.toList());

        return results;
    }
}
