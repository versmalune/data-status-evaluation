package kr.co.data_status_evaluation.service;

import kr.co.data_status_evaluation.model.InstitutionCategory;
import kr.co.data_status_evaluation.model.InstitutionCategoryDetail;
import kr.co.data_status_evaluation.repository.InstitutionCategoryDetailRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InstitutionCategoryDetailService {
    private final InstitutionCategoryDetailRepository institutionCategoryDetailRepository;

    public List<InstitutionCategoryDetail> findAllByInstitutionCategory(InstitutionCategory category) {
        return institutionCategoryDetailRepository.findAllByInstitutionCategory(category);
    }
}
