package com.subcycle.specification;

import com.subcycle.entity.Subscription;
import com.subcycle.entity.User;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class SubscriptionSpecification {

    public static Specification<Subscription> filterSubscriptions(
            User user,
            String search,
            String status,
            Long categoryId,
            String billingCycle
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 必須是該使用者的訂閱
            predicates.add(criteriaBuilder.equal(root.get("user"), user));

            // 搜尋：名稱包含關鍵字
            if (search != null && !search.trim().isEmpty()) {
                String searchPattern = "%" + search.trim().toLowerCase() + "%";
                Predicate namePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")), searchPattern);
                predicates.add(namePredicate);
            }

            // 篩選狀態
            if (status != null && !status.trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            // 篩選類別
            if (categoryId != null) {
                predicates.add(criteriaBuilder.equal(root.get("category").get("id"), categoryId));
            }

            // 篩選計費週期
            if (billingCycle != null && !billingCycle.trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("billingCycle"), billingCycle));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
