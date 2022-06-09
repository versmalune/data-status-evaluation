package kr.co.data_status_evaluation.repository;

import kr.co.data_status_evaluation.model.InstitutionCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface InstitutionCategoryRepository extends JpaRepository<InstitutionCategory, Long>, JpaSpecificationExecutor<InstitutionCategory> {
    InstitutionCategory findByCode(String code);

    @Query(value = "SELECT * FROM tb_rev_instt_category WHERE id = (:id)", nativeQuery = true)
    InstitutionCategory findByIdNotOptional(Long id);
}
