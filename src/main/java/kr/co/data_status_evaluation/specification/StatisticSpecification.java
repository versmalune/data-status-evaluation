package kr.co.data_status_evaluation.specification;

import kr.co.data_status_evaluation.model.EvaluationIndex_;
import kr.co.data_status_evaluation.model.EvaluationResult_;
import kr.co.data_status_evaluation.model.Institution;
import kr.co.data_status_evaluation.model.Institution_;
import kr.co.data_status_evaluation.model.search.StatisticSearchParam;
import kr.co.data_status_evaluation.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class StatisticSpecification {
    public static Specification<Institution> search(StatisticSearchParam searchParam) {
        Logger logger = LoggerFactory.getLogger(StatisticSpecification.class);

        return ((root, query, cb) -> {
            //This part allow to use this specification in pageable queries
            //but you must be aware that the results will be paged in
            //application memory!

            Class clazz = query.getResultType();
            if (!clazz.equals(Long.class) && !clazz.equals(long.class)) {
//                root.fetch(Institution_.asset, JoinType.LEFT);
                root.fetch(Institution_.institutionDetails, JoinType.LEFT);
            }

            Predicate result = null;
            List<Predicate> predicates = new ArrayList<>();

            // 검색 조건 predicates
            if (searchParam != null) {
                if (!StringUtils.isNullOrEmpty(searchParam.getIndex())) {
                    predicates.add(cb.equal(root.join(Institution_.evaluationResults).get(EvaluationResult_.evaluationIndex).get(EvaluationIndex_.id), searchParam.getIndex()));
                }

                if (!StringUtils.isNullOrEmpty(searchParam.getType())) {
                    predicates.add(cb.equal(root.get(Institution_.type), searchParam.getType()));
                }

                predicates.add(cb.isNotNull(root.get(Institution_.category)));

            }

            for (Predicate p : predicates) {
                if (result == null) {
                    result = cb.and(p);
                } else {
                    result = cb.and(result, p);
                }
            }

            query.distinct(true);
            query.orderBy(cb.asc(root.get(Institution_.code)));

            return result;
        });
    }
}
