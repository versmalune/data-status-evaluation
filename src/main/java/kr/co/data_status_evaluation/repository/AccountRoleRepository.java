package kr.co.data_status_evaluation.repository;

import kr.co.data_status_evaluation.model.Account;
import kr.co.data_status_evaluation.model.AccountRole;
import kr.co.data_status_evaluation.model.enums.Author;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AccountRoleRepository extends JpaRepository<AccountRole, Long> {
    AccountRole findByAccount(Account account);
    AccountRole findByAuthorAndAccount(Author author, Account account);

    @Query(value = "SELECT * FROM tb_rev_account_role WHERE accnt_id IN (:accountIdList)", nativeQuery = true)
    List<AccountRole> findAllByAccountIds(List<Long> accountIdList);
    List<AccountRole> findAllByAccount(Account account);
    long deleteAllByAccount(Account account);
}
