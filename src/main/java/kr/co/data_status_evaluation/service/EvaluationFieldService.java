package kr.co.data_status_evaluation.service;

import kr.co.data_status_evaluation.model.EvaluationField;
import kr.co.data_status_evaluation.model.dto.EvaluationScheduleSummaryDto;
import kr.co.data_status_evaluation.repository.EvaluationFieldRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.InvalidParameterException;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EvaluationFieldService {

    private final EvaluationFieldRepository evaluationFieldRepository;

    public List<EvaluationField> findAllByYear(String year) {
        return this.evaluationFieldRepository.findAllByYear(year);
    }

    public List<EvaluationField> findAll() {
        return this.evaluationFieldRepository.findAll();
    }

    public List<EvaluationField> findAll(Sort sort) {
        return this.evaluationFieldRepository.findAll(sort);
    }

    public EvaluationField findById(Long id) {
        Optional<EvaluationField> fieldOptional = this.evaluationFieldRepository.findById(id);
        if (!fieldOptional.isPresent()) {
            throw new InvalidParameterException();
        }
        return fieldOptional.get();
    }

    public EvaluationField findByNoAndYear(int no, String year) {
        return this.evaluationFieldRepository.findByNoAndYear(no, year).orElseThrow(InvalidParameterException::new);
    }

    public List<EvaluationScheduleSummaryDto> findAllSummary() {
        return this.evaluationFieldRepository.findAllSummary();
    }

    public List<EvaluationField> findByYear(String year, Sort sort) {
        return evaluationFieldRepository.findAllByYear(year, sort);
    }

    public List<String> findYears() {
        return evaluationFieldRepository.findYears();
    }

    @Transactional
    public void save(EvaluationField field) {
        this.evaluationFieldRepository.save(field);
    }

    @Transactional
    public void update(EvaluationField field) {
        EvaluationField origin = this.evaluationFieldRepository.findById(field.getId()).orElseThrow(InvalidParameterException::new);
        field.setEvaluationIndices(origin.getEvaluationIndices());
        this.save(field);

    }

    @Transactional
    public void deleteById(Long id) {
        this.evaluationFieldRepository.deleteById(id);
    }

    public boolean hasDuplicateField(String year, int no, String name) {
        return this.evaluationFieldRepository.countAllByYearAndNoAndName(year, no, name) > 0;
    }
}
