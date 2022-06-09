package kr.co.data_status_evaluation.repository;

import kr.co.data_status_evaluation.model.InstitutionCategory;
import kr.co.data_status_evaluation.model.InstitutionCategoryDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InstitutionCategoryDetailRepository extends JpaRepository<InstitutionCategoryDetail, Long> {
    List<InstitutionCategoryDetail> findAllByInstitutionCategory(InstitutionCategory category);
}
