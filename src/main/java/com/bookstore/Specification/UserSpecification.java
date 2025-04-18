package com.bookstore.Specification;

import com.bookstore.Entity.User;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class UserSpecification {

    public static Specification<User> withFilters(Integer isActive, Integer isVerified, String email) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (isActive != null && isActive != 2) {
                predicates.add(cb.equal(root.get("isActive"), isActive == 1));
            }

            if (isVerified != null && isVerified != 2) {
                predicates.add(cb.equal(root.get("isVerified"), isVerified == 1));
            }

            if (email != null && !email.trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("email")), "%" + email.toLowerCase() + "%"));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
