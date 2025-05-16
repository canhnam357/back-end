package com.bookstore.Specification;

import com.bookstore.Entity.Book;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Path;
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
            String sort,
            List<String> categoryIds
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            Path<BigDecimal> pricePath = root.get("price");

            if (leftBound != null) {
                predicates.add(cb.greaterThanOrEqualTo(pricePath, leftBound));
            }

            if (rightBound != null) {
                predicates.add(cb.lessThanOrEqualTo(pricePath, rightBound));
            }

            if (authorIds != null && !authorIds.isEmpty()) {
                predicates.add(root.get("author").get("authorId").in(authorIds));
            }

            if (publisherIds != null && !publisherIds.isEmpty()) {
                predicates.add(root.get("publisher").get("publisherId").in(publisherIds));
            }

            if (distributorIds != null && !distributorIds.isEmpty()) {
                predicates.add(root.get("distributor").get("distributorId").in(distributorIds));
            }

            if (bookName != null && !bookName.trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("nameNormalized")), "%" + bookName.toLowerCase() + "%"));
            }

            if (categoryIds != null && !categoryIds.isEmpty()) {
                Join<Object, Object> categoriesJoin = root.join("categories"); // JOIN categories
                predicates.add(categoriesJoin.get("categoryId").in(categoryIds)); // WHERE categoryId IN (....)
                assert query != null;
                query.groupBy(root.get("bookId")); // GROUP BY bookId
                query.having(cb.equal(cb.countDistinct(categoriesJoin.get("categoryId")), categoryIds.size())); // HAVING COUNT(DISTINCT categoryId) = categoryIds.size()
            }

            predicates.add(cb.equal(root.get("deleted"), false));

            // Sắp xếp theo giá nếu có
            if ("asc".equalsIgnoreCase(sort)) {
                assert query != null;
                query.orderBy(cb.asc(root.get("price")));
            } else if ("desc".equalsIgnoreCase(sort)) {
                assert query != null;
                query.orderBy(cb.desc(root.get("price")));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}