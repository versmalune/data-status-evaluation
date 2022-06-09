package kr.co.data_status_evaluation.repository;

import kr.co.data_status_evaluation.model.Institution;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface InstitutionRepository extends JpaRepository<Institution, Long>, JpaSpecificationExecutor<Institution> {

    Optional<Institution> findByName(String name);

    Page<Institution> findAllByCodeContainingAndNameContaining(Pageable pageable, String code, String Name);

    Institution findByCode(String code);

    List<Institution> findAllByCode(String code);

    void deleteAllByCode(String code);

    @Modifying
    @Query(value = "UPDATE  tb_rev_instt\n" +
                    "SET     instt_ty = :ctgyCd\n" +
                    "    ,   instt_category_id = :ctgyId\n" +
                    "WHERE   id = :insttId", nativeQuery = true)
    void updateTypeAndCtgyIdById(Long insttId, String ctgyCd, Long ctgyId);

    Institution findFirstByCode(String code);

    @Query(value = "SELECT DISTINCT instt FROM Institution instt " +
            "LEFT JOIN FETCH instt.evaluationResults evlResult " +
            "LEFT JOIN FETCH evlResult.evaluationIndex evlIndex " +
            "LEFT JOIN FETCH evlIndex.evaluationField evlFld " +
            "WHERE instt.id = :id " +
            "AND evlFld.year = :year " +
            "ORDER BY evlIndex.createdAt, evlResult.createdAt")
    Optional<Institution> findByIdAndYear(Long id, String year);

    List<Institution> findAllByDeletedAndCategoryIsNotNull(String deleted);

    @Query(value = "SELECT * FROM tb_rev_instt WHERE del_yn = 'N' AND instt_cd IN (:insttCdList)", nativeQuery = true)
    List<Institution> findAllByInsttCds(List<String> insttCdList);

    @Query(value = "SELECT * FROM tb_rev_instt WHERE instt_cd IN (:insttCdList)", nativeQuery = true)
    List<Institution> findAllByInsttCdsRegardlessOfDelYn(List<String> insttCdList);
    
    @Query(value = "SELECT instt.*\n" +
            "FROM tb_rev_instt instt\n" +
            "         JOIN tb_rev_evl_trgt_instt trgt on instt.id = trgt.instt_id\n" +
            "WHERE trgt.trgt_instt_yn = 'Y'\n" +
            "  AND instt.del_yn = 'N'", nativeQuery = true)
    List<Institution> findAllByTarget();

    @Query(value = "SELECT instt.*\n" +
            "FROM tb_rev_instt instt\n" +
            "         JOIN tb_rev_evl_trgt_instt trgt on instt.id = trgt.instt_id\n" +
            "WHERE trgt.trgt_instt_yn = 'Y'\n" +
            "  AND instt.del_yn = 'N'\n" +
            "  AND trgt.evl_yr = :year", nativeQuery = true)
    List<Institution> findAllByTarget(String year);

    @Query(value = "SELECT i.*\n" +
            "FROM tb_rev_evl_trgt_instt trgt\n" +
            "         JOIN tb_rev_instt i on trgt.instt_id = i.id\n" +
            "         JOIN tb_rev_instt_detail ind ON i.id = ind.instt_id AND ind.evl_yr = :year\n" +
            "WHERE i.del_yn = 'N' AND trgt.trgt_instt_yn = 'Y' AND trgt.evl_yr = :year AND ind.instt_category_id = :categoryId", nativeQuery = true)
    List<Institution> findAllByTargetAndCategoryIdAndYear(Long categoryId, String year);

    @Query(value = "SELECT * FROM tb_rev_instt WHERE instt_cd = (:insttCd)", nativeQuery = true)
    Institution findByCodeRegardlessOfDelYn(String insttCd);
}
