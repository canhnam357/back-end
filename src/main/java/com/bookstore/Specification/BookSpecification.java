package com.bookstore.Specification;

import com.bookstore.Entity.Book;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class BookSpecification {

    public static Specification<Book> withFilters(
            BigDecimal leftBound,
            BigDecimal rightBound,
            List<String> authorIds,
            List<String> publisherIds,
            List<String> distributorIds,
            String bookName,
            String sort
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Lọc theo khoảng giá
            if (leftBound != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), leftBound));
            }

            if (rightBound != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), rightBound));
            }

            // Lọc theo danh sách authorId nếu có
            if (authorIds != null && !authorIds.isEmpty()) {
                predicates.add(root.get("author").get("authorId").in(authorIds));
            }

            // Lọc theo danh sách publisherId nếu có
            if (publisherIds != null && !publisherIds.isEmpty()) {
                predicates.add(root.get("publisher").get("publisherId").in(publisherIds));
            }

            // Lọc theo danh sách distributorId nếu có
            if (distributorIds != null && !distributorIds.isEmpty()) {
                predicates.add(root.get("distributor").get("distributorId").in(distributorIds));
            }

            // Lọc theo tên sách (không phân biệt hoa thường)
            if (bookName != null && !bookName.trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("nameNormalized")), "%" + bookName.toLowerCase() + "%"));
            }

            predicates.add(cb.equal(root.get("isDeleted"), false));

            // Sắp xếp theo giá nếu có
            if ("asc".equalsIgnoreCase(sort)) {
                query.orderBy(cb.asc(root.get("price")));
            } else if ("desc".equalsIgnoreCase(sort)) {
                query.orderBy(cb.desc(root.get("price")));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}