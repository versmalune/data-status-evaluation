package kr.co.data_status_evaluation.service;

import kr.co.data_status_evaluation.model.EvaluationScore;
import kr.co.data_status_evaluation.model.InstitutionCategory;
import kr.co.data_status_evaluation.repository.EvaluationScoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EvaluationScoreService {

    private final EvaluationScoreRepository evaluationScoreRepository;

    @Transactional
    public void saveAll(List<EvaluationScore> scores) {
        this.evaluationScoreRepository.saveAll(scores);
    }

    @Transactional
    public void save(EvaluationScore score) {
        this.evaluationScoreRepository.save(score);
    }

    @Transactional
    public void deleteAllByCategory(InstitutionCategory category) {
        this.evaluationScoreRepository.deleteAllByInstitutionCategory(category);
    }

    public Optional<EvaluationScore> findByIndexIdAndCategoryIdAndLevel(Long indexId, Long categoryId, Integer level) {
        return this.evaluationScoreRepository.findByEvaluationIndexIdAndInstitutionCategoryIdAndLevel(indexId, categoryId, level);
    }
}
