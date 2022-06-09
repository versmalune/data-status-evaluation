package kr.co.data_status_evaluation.specification;

import kr.co.data_status_evaluation.model.Account;
import kr.co.data_status_evaluation.model.AccountRole_;
import kr.co.data_status_evaluation.model.Account_;
import kr.co.data_status_evaluation.model.Institution_;
import kr.co.data_status_evaluation.model.search.AccountSearchParam;
import kr.co.data_status_evaluation.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class AccountSpecification {

    public static Specification<Account> search(AccountSearchParam searchParam) {
        Logger logger = LoggerFactory.getLogger(AccountSpecification.class);

        return ((root, query, cb) -> {
            //This part allow to use this specification in pageable queries
            //but you must be aware that the results will be paged in
            //application memory!

            Class clazz = query.getResultType();
            if (!clazz.equals(Long.class) && !clazz.equals(long.class)) {
                root.fetch(Account_.institution, JoinType.LEFT);
            }

            Predicate result = null;
            List<Predicate> predicates = new ArrayList<>();

            // 검색 조건 predicates
            if (searchParam != null) {
                if (!StringUtils.isNullOrEmpty(searchParam.getUserId())) {
                    predicates.add(cb.like(root.get(Account_.userId), "%" + searchParam.getUserId() + "%"));
                }

                if (!StringUtils.isNullOrEmpty(searchParam.getUser())) {
                    predicates.add(cb.like(root.get(Account_.name), "%" + searchParam.getUser() + "%"));
                }

                if (searchParam.getType() != null) {
                    predicates.add(cb.equal(root.join(Account_.roles).get(AccountRole_.author), searchParam.getType()));
                }

                if (!StringUtils.isNullOrEmpty(searchParam.getInstitution())) {
                    predicates.add(cb.like(root.join(Account_.institution).get(Institution_.name), "%" + searchParam.getInstitution() + "%"));
                }
            }
             predicates.add(cb.equal(root.get(Account_.deleted), "N"));

            for (Predicate p : predicates) {
                if (result == null) {
                    result = cb.and(p);
                } else {
                    result = cb.and(result, p);
                }
            }

            query.distinct(true);

            return result;
        });
    }
}
