package kr.co.data_status_evaluation.specification;

import kr.co.data_status_evaluation.model.*;
import kr.co.data_status_evaluation.model.search.ObjectionRequestSearchParam;
import kr.co.data_status_evaluation.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.ListJoin;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

public class ObjectionRequestSpecification {
    public static Specification<EvaluationResult> search(ObjectionRequestSearchParam searchParam) {
        Logger logger = LoggerFactory.getLogger(ObjectionRequestSpecification.class);

        return ((root, query, cb) -> {
            //This part allow to use this specification in pageable queries
            //but you must be aware that the results will be paged in
            //application memory!

            Class clazz = query.getResultType();
            if (!clazz.equals(Long.class) && !clazz.equals(long.class)) {
//                root.fetch(Institution_.asset, JoinType.LEFT);
            }
            Root<EvaluationTargetInstitution> targetInstitution = query.from(EvaluationTargetInstitution.class);
            ListJoin<Institution, InstitutionDetail> institutionDetailJoin = root.join(EvaluationResult_.institution).join(Institution_.institutionDetails);

            Predicate result = null;
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.and(
                    cb.equal(root.get(EvaluationResult_.institution), targetInstitution.get(EvaluationTargetInstitution_.institution)),
                    cb.equal(targetInstitution.get(EvaluationTargetInstitution_.trgtInsttYn), "Y")
            ));

            // 검색 조건 predicates
            if (searchParam != null) {
                predicates.add(cb.equal(root.join(EvaluationResult_.evaluationIndex).get(EvaluationIndex_.objectionPossible), true));
                predicates.add(cb.and(
                                cb.isNotNull(root.get(EvaluationResult_.objection)),
                                cb.notEqual(root.get(EvaluationResult_.objection), "없음"),
                                cb.notEqual(root.get(EvaluationResult_.objection), "")
                        )
                );

                if (!StringUtils.isNullOrEmpty(searchParam.getType())) {
//                    predicates.add(cb.equal(root.join(EvaluationResult_.institution).get(Institution_.type), searchParam.getType()));
                    predicates.add(cb.equal(institutionDetailJoin.get(InstitutionDetail_.type), searchParam.getType()));

                }

                if (!StringUtils.isNullOrEmpty(searchParam.getInsttNm())) {
                    predicates.add(cb.like(root.join(EvaluationResult_.institution).get(Institution_.name), "%" + searchParam.getInsttNm() + "%"));
                }

                if (!StringUtils.isNullOrEmpty(searchParam.getYear())) {
                    predicates.add(cb.equal(root.get(EvaluationResult_.year), searchParam.getYear()));
                    predicates.add(cb.equal(targetInstitution.get(EvaluationTargetInstitution_.year), searchParam.getYear()));
                    predicates.add(cb.equal(institutionDetailJoin.get(InstitutionDetail_.year), searchParam.getYear()));
                }

                if (!StringUtils.isNullOrEmpty(searchParam.getIndexName())) {
                    predicates.add(cb.like(
                            cb.concat(
                                    cb.concat(root.join(EvaluationResult_.evaluationIndex).get(EvaluationIndex_.no),
                                            ". "),
                                    root.join(EvaluationResult_.evaluationIndex).get(EvaluationIndex_.name)
                            ),
                            "%" + searchParam.getIndexName() + "%")
                    );
                }

                if (!StringUtils.isNullOrEmpty(searchParam.getObjection())) {
                    predicates.add(cb.like(root.get(EvaluationResult_.objection), "%" + searchParam.getObjection() + "%"));
                }

                if (!StringUtils.isNullOrEmpty(searchParam.getStatus())) {
                    if (searchParam.getStatus().equals("objc")) {
                        predicates.add(
                                cb.or(cb.isNull(root.get(EvaluationResult_.objectionReview)),
                                        cb.equal(root.get(EvaluationResult_.objectionReview), ""))
                        );
                    } else if (searchParam.getStatus().equals("review")) {
                        predicates.add(
                                cb.and(cb.isNotNull(root.get(EvaluationResult_.objectionReview)),
                                        cb.notEqual(root.get(EvaluationResult_.objectionReview), ""))
                        );
                    }
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
            query.orderBy(cb.desc(root.get(EvaluationResult_.id)));

            return result;
        });
    }
}
