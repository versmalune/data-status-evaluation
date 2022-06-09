package kr.co.data_status_evaluation.repository;

import kr.co.data_status_evaluation.model.LmsInstitution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LmsInstitutionRepository extends JpaRepository<LmsInstitution, Long> {
    Optional<LmsInstitution> findByInsttCd(String insttCd);
    void deleteByInsttCd(String insttCd);
    List<LmsInstitution> findAllByInsttCd(String insttCd);
    void deleteAllByInsttCd(String insttCd);

    @Query(value = "SELECT * FROM tb_rev_list_regist_mmg_sys_instt_info WHERE instt_cd IN (:insttCds)",
            nativeQuery = true)
    List<LmsInstitution> findAllByInsttCds(List<String> insttCds);

    @Query(value = "SELECT * FROM tb_rev_list_regist_mmg_sys_instt_info WHERE instt_cd = (:insttCd)", nativeQuery = true)
    Optional<LmsInstitution> findByCodeRegardlessOfMntnabYn(String insttCd);
}
