package kr.co.data_status_evaluation.service;

import kr.co.data_status_evaluation.model.EvaluationIndex;
import kr.co.data_status_evaluation.model.EvaluationIndexRate;
import kr.co.data_status_evaluation.model.EvaluationIndexRateDetail;
import kr.co.data_status_evaluation.model.dto.AdminEvaluationIndexRateDto;
import kr.co.data_status_evaluation.repository.EvaluationIndexRateDetailRepository;
import kr.co.data_status_evaluation.repository.EvaluationIndexRateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EvaluationIndexRateService {

    private final EvaluationIndexRateRepository evaluationIndexRateRepository;
    private final EvaluationIndexRateDetailRepository evaluationIndexRateDetailRepository;

    public List<EvaluationIndexRate> findAllByYear(String year) {
        return this.evaluationIndexRateRepository.findAllByYearOrderById(year);
    }

    public Optional<EvaluationIndexRate> findById(Long id) {
        return this.evaluationIndexRateRepository.findById(id);
    }

    @Transactional
    public void save(AdminEvaluationIndexRateDto dto) {
        EvaluationIndexRate indexRate = new EvaluationIndexRate();
        List<EvaluationIndexRateDetail> details = dto.getRateDetails();
        List<EvaluationIndex> indices = dto.getIndices();

        indexRate.setYear(dto.getYear());
        details.forEach(detail -> detail.setIndexRate(indexRate));
        indices.forEach(index -> index.setRate(indexRate));
        indexRate.getDetails().addAll(details);
        indexRate.getIndices().addAll(indices);
        this.evaluationIndexRateRepository.save(indexRate);
    }

    @Transactional
    public void update(AdminEvaluationIndexRateDto dto, Long rateId) {
        Optional<EvaluationIndexRate> indexRateOptional = this.evaluationIndexRateRepository.findById(rateId);
        if (!indexRateOptional.isPresent()) {
            return;
        }
        EvaluationIndexRate indexRate = indexRateOptional.get();
        List<EvaluationIndexRateDetail> details = dto.getRateDetails();
        List<EvaluationIndex> indices = dto.getIndices();

        indexRate.setYear(dto.getYear());
        details.forEach(detail -> detail.setIndexRate(indexRate));
        indices.forEach(index -> index.setRate(indexRate));
        indexRate.getDetails().clear();
        indexRate.getDetails().addAll(details);
        indexRate.getIndices().clear();
        indexRate.getIndices().addAll(indices);
        this.evaluationIndexRateRepository.save(indexRate);
    }

    @Transactional
    public void deleteById(Long rateId) {
        this.evaluationIndexRateRepository.deleteById(rateId);
    }
}
