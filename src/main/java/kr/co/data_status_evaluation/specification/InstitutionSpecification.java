package kr.co.data_status_evaluation.specification;

import kr.co.data_status_evaluation.model.*;
import kr.co.data_status_evaluation.model.search.InstitutionSearchParam;
import kr.co.data_status_evaluation.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.ListJoin;
import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class InstitutionSpecification {
    public static Specification<Institution> search(InstitutionSearchParam searchParam) {
        Logger logger = LoggerFactory.getLogger(InstitutionSpecification.class);

        return ((root, query, cb) -> {
            //This part allow to use this specification in pageable queries
            //but you must be aware that the results will be paged in
            //application memory!

            Class clazz = query.getResultType();
            if (!clazz.equals(Long.class) && !clazz.equals(long.class)) {
//                root.fetch(Institution_.asset, JoinType.LEFT);
                root.fetch(Institution_.institutionDetails, JoinType.LEFT);
            }
            ListJoin<Institution, EvaluationTargetInstitution> targetInstitutionJoin = root.join(Institution_.targetInstitution, JoinType.INNER);
            ListJoin<Institution, InstitutionDetail> institutionDetailJoin = root.join(Institution_.institutionDetails);

            Predicate result = null;
            List<Predicate> predicates = new ArrayList<>();

            // 검색 조건 predicates
            if (searchParam != null) {
                if (!StringUtils.isNullOrEmpty(searchParam.getYear())) {
                    predicates.add(cb.equal(targetInstitutionJoin.get(EvaluationTargetInstitution_.year), searchParam.getYear()));
                    predicates.add(cb.equal(institutionDetailJoin.get(InstitutionDetail_.year), searchParam.getYear()));
                }

                if (!StringUtils.isNullOrEmpty(searchParam.getType())) {
//                    predicates.add(cb.equal(root.get(Institution_.type), searchParam.getType()));
                    predicates.add(cb.equal(institutionDetailJoin.get(InstitutionDetail_.type), searchParam.getType()));
                }

                if (!StringUtils.isNullOrEmpty(searchParam.getCode())) {
                    predicates.add(cb.like(root.get(Institution_.code), "%" + searchParam.getCode() + "%"));
                }

                if (!StringUtils.isNullOrEmpty(searchParam.getName())) {
                    predicates.add(cb.like(root.get(Institution_.name), "%" + searchParam.getName() + "%"));
                }

                // 자체실적 미등록 기관 조회용
                if (!StringUtils.isNullOrEmpty(searchParam.getTrgtInsttYn())) {
                    predicates.add(cb.equal(targetInstitutionJoin.get(EvaluationTargetInstitution_.trgtInsttYn), searchParam.getTrgtInsttYn()));
                }
                if (!StringUtils.isNullOrEmpty(searchParam.getStatus())) {
                    if (searchParam.getStatus().equals("noLogin") && !Objects.isNull(searchParam.getStdDt())) {
                        predicates.add(
                                cb.or(cb.isNull(root.get(Institution_.lastAccessDt)),
                                        cb.lessThan(root.get(Institution_.lastAccessDt), searchParam.getStdDt()))
                        );
                    }
                }
            }
            predicates.add(cb.equal(root.get(Institution_.deleted), "N"));
            predicates.add(cb.isNotNull(root.get(Institution_.category)));
            predicates.add(cb.equal(targetInstitutionJoin.get(EvaluationTargetInstitution_.trgtInsttYn.getName()), "Y"));

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
