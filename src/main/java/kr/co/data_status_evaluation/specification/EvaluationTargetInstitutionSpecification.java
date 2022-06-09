package kr.co.data_status_evaluation.specification;

import kr.co.data_status_evaluation.model.*;
import kr.co.data_status_evaluation.model.search.EvaluationTargetInstitutionSearchParam;
import kr.co.data_status_evaluation.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.ListJoin;
import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class EvaluationTargetInstitutionSpecification {
    public static Specification<EvaluationTargetInstitution> search(EvaluationTargetInstitutionSearchParam searchParam) {
        Logger logger = LoggerFactory.getLogger(EvaluationTargetInstitutionSpecification.class);

        return ((root, query, cb) -> {
            //This part allow to use this specification in pageable queries
            //but you must be aware that the results will be paged in
            //application memory!

            Class clazz = query.getResultType();
            if (!clazz.equals(Long.class) && !clazz.equals(long.class)) {

            }
            Join<EvaluationTargetInstitution, Institution> institutionJoin = root.join(EvaluationTargetInstitution_.institution);
            ListJoin<Institution, InstitutionDetail> institutionDetailJoin = institutionJoin.join(Institution_.institutionDetails);
            Predicate result = null;
            List<Predicate> predicates = new ArrayList<>();

            // 검색 조건 predicates
            if (searchParam != null) {
                if (!StringUtils.isNullOrEmpty(searchParam.getCategoryId())) {
//                    predicates.add(cb.equal(institutionJoin.join(Institution_.category).get(InstitutionCategory_.id), searchParam.getCategoryId()));
                    predicates.add(cb.equal(institutionDetailJoin.get(InstitutionDetail_.category).get(InstitutionCategory_.id), searchParam.getCategoryId()));
                }
                if (!StringUtils.isNullOrEmpty(searchParam.getYear())) {
                    predicates.add(cb.equal(root.get(EvaluationTargetInstitution_.year), searchParam.getYear()));
                    predicates.add(cb.equal(institutionDetailJoin.get(InstitutionDetail_.year), searchParam.getYear()));
                }
                if (!StringUtils.isNullOrEmpty(searchParam.getTrgtYn())) {
                    predicates.add(cb.equal(root.get(EvaluationTargetInstitution_.trgtInsttYn), searchParam.getTrgtYn()));
                }
                if (!StringUtils.isNullOrEmpty(searchParam.getInstitutionCode())) {
                    predicates.add(cb.like(institutionJoin.get(Institution_.code), "%"+searchParam.getInstitutionCode()+"%"));
                }
                if (!StringUtils.isNullOrEmpty(searchParam.getInstitutionNm())) {
                    predicates.add(cb.like(institutionJoin.get(Institution_.name), "%"+searchParam.getInstitutionNm()+"%"));
                }
            }
            predicates.add(cb.equal(institutionJoin.get(Institution_.deleted), "N"));

            for(Predicate p : predicates) {
                if (result == null) {
                    result = cb.and(p);
                } else {
                    result = cb.and(result, p);
                }
            }

            query.distinct(true);
            query.orderBy(cb.asc(root.get(EvaluationTargetInstitution_.id)));

            return result;
        });
    }
}
