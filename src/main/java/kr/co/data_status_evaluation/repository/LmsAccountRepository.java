package kr.co.data_status_evaluation.repository;

import kr.co.data_status_evaluation.model.LmsAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LmsAccountRepository extends JpaRepository<LmsAccount, Long> {
    Optional<LmsAccount> findByMberId(String id);

    void deleteByMberId(String mberId);

    List<LmsAccount> findAllByMberId(String id);

    void deleteAllByMberId(String mberId);

    @Query(value = "SELECT * FROM tb_rev_list_regist_mmg_sys_instt_user_info WHERE mber_id IN (:mberIdList)"
            , nativeQuery = true)
    List<LmsAccount> findAllByMberIds(List<String> mberIdList);

    @Modifying
    @Query(value = "" +
            "UPDATE tb_rev_list_regist_mmg_sys_instt_user_info\n" +
            "SET instt_cd = (:newInsttCd)\n" +
            "WHERE instt_cd = (:beforeInsttCd)", nativeQuery = true)
    void updateByBeforeInstt(String newInsttCd, String beforeInsttCd);
}
