package kr.co.data_status_evaluation.specification;

import kr.co.data_status_evaluation.model.*;
import kr.co.data_status_evaluation.model.search.EvaluationIndexSearchParam;
import kr.co.data_status_evaluation.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import java.util.ArrayList;
import java.util.List;

public class EvaluationIndexSpecification {
    public static Specification<EvaluationIndex> search(EvaluationIndexSearchParam searchParam) {
        Logger logger = LoggerFactory.getLogger(EvaluationIndexSpecification.class);

        return ((root, query, cb) -> {
            //This part allow to use this specification in pageable queries
            //but you must be aware that the results will be paged in
            //application memory!

            Class clazz = query.getResultType();
            if (!clazz.equals(Long.class) && !clazz.equals(long.class)) {
//                root.fetch(Institution_.asset, JoinType.LEFT);
                root.fetch(EvaluationIndex_.scores, JoinType.LEFT);
//                root.fetch(EvaluationIndex_.results, JoinType.LEFT);
            }

            Predicate result = null;
            List<Predicate> predicates = new ArrayList<>();

            // 검색 조건 predicates
            if (searchParam != null) {
                if (!StringUtils.isNullOrEmpty(searchParam.getYear())) {
                    predicates.add(cb.equal(root.join(EvaluationIndex_.evaluationField).get(EvaluationField_.year), searchParam.getYear()));
                }

                if (searchParam.getType() != null) {
                    predicates.add(cb.equal(root.get(EvaluationIndex_.type), searchParam.getType()));
                }

                if (!StringUtils.isNullOrEmpty(searchParam.getInstitutionType())) {
                    predicates.add(cb.equal(root.join(EvaluationIndex_.relevantInstitutions).join(RelevantInstitution_.institutionCategory).get(InstitutionCategory_.code), searchParam.getInstitutionType()));
                }
            }

            for (Predicate p : predicates) {
                if (result == null) {
                    result = cb.and(p);
                } else {
                    result = cb.and(result, p);
                }
            }

            query.distinct(true);
            query.orderBy(cb.asc(root.get(EvaluationIndex_.no)));

            return result;
        });
    }
}