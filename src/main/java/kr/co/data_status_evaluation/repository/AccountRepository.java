package kr.co.data_status_evaluation.repository;

import kr.co.data_status_evaluation.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long>, JpaSpecificationExecutor<Account> {
    Account findByUserIdAndDeleted(String userId, String deleted);

    @Query(value ="SELECT   a\n" +
                "FROM   Account a \n" +
                "       INNER JOIN FETCH     a.institution i \n" +
                "WHERE  a.deleted = 'N'\n" +
                "AND    a.id = :id\n")
    Account findOneWithInstitutionById(Long id);

    @Query(value ="SELECT   a\n" +
                "FROM   Account a \n" +
                "       INNER JOIN FETCH    a.roles ar \n" +
                "       LEFT JOIN FETCH     a.institution i \n" +
                "WHERE  a.deleted = 'N'\n" +
                "AND    a.userId = :userId")
    Account findOneWithRolesAndInstitutionByUserId(String userId);

    Account findByEmail(String emil);
    Account findByEmailAndPassword(String email, String password);
    Account findByName(String name);
    List<Account> findAllByUserId(String userId);
    void deleteAllByUserId(String userId);

    @Query(value = "SELECT * FROM tb_rev_account WHERE del_yn = 'N' AND user_id in (:mberIdList)", nativeQuery = true)
    List<Account> findAllByMberIds(List<String> mberIdList);

    @Query(value = "" +
            "SELECT * " +
            "FROM tb_rev_account " +
            "WHERE del_yn = 'Y' AND user_id = (:userId)", nativeQuery = true)
    Account findByUserIDelYnIsY(String userId);

    @Query(value = "" +
            "SELECT * " +
            "FROM tb_rev_account " +
            "WHERE user_id = (:userId)", nativeQuery = true)
    Account findByUserIdRegardlessOfDelYn(String userId);

    @Query(value = "SELECT * FROM tb_rev_account WHERE user_id in (:mberIdList)", nativeQuery = true)
    List<Account> findAllByMberIdsRegardlessOfDelYn(List<String> mberIdList);

    @Modifying
    @Query(value = "" +
            "UPDATE tb_rev_account\n" +
            "SET instt_id = (:newInsttId)\n" +
            "WHERE instt_id = (:beforeInsttId)", nativeQuery = true)
    void updateByBeforeInstt(Long newInsttId, Long beforeInsttId);
}
