package kr.co.data_status_evaluation.repository;

import kr.co.data_status_evaluation.model.Material;
import kr.co.data_status_evaluation.model.enums.MaterialType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaterialRepository extends JpaRepository<Material, Long> {
    List<Material> findAllByAtchTrgtId(Long id);

    Material findFirstByMtlTyAndAtchTrgtId(MaterialType materialType, Long id);

    List<Material> findAllByMtlTyAndAtchTrgtId(MaterialType materialType, Long id);

    List<Material> findAllByMtlTyAndAtchTrgtIdIn(MaterialType materialType, List<Long> id);
}
