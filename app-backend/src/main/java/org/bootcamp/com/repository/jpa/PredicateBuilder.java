package org.bootcamp.com.repository.jpa;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.AllArgsConstructor;
import org.bootcamp.com.exception.InvalidQueryException;
import org.bootcamp.com.model.GithubUser;
import org.bootcamp.com.request.filter.ColumnFilter;
import org.bootcamp.com.request.filter.NumericColumnFilter;
import org.bootcamp.com.request.filter.StringColumnFilter;
import org.bootcamp.com.utils.GithubUserMapper;

@AllArgsConstructor
public class PredicateBuilder {
    private CriteriaBuilder criteriaBuilder;
    private Root<GithubUser> githubUserRoot;

    public Predicate getPredicate(ColumnFilter columnFilter) {
        if (columnFilter instanceof StringColumnFilter) {
            return stringPredicateBuilder((StringColumnFilter) columnFilter);
        } else if (columnFilter instanceof NumericColumnFilter) {
            return numericPredicateBuilder((NumericColumnFilter) columnFilter);
        }
        throw new InvalidQueryException("ColumnFilter not an instance of StringColumnFilter/NumericColumnFilter");
    }

    private Predicate stringPredicateBuilder(StringColumnFilter columnFilter) {
        final Path<String> path = githubUserRoot.get(GithubUserMapper.COLUMN_TO_FIELD_MAP.getOrDefault(columnFilter.getColumn(), columnFilter.getColumn()));
        return switch (columnFilter.getOperator()) {
            case EQUALS -> criteriaBuilder.equal(path, columnFilter.getValue());
            case NOT_EQUALS -> criteriaBuilder.notEqual(path, columnFilter.getValue());
            case CONTAINS -> criteriaBuilder.like(path, "%" + columnFilter.getValue() + "%");
            case BLANK -> criteriaBuilder.or(criteriaBuilder.isNull(path), criteriaBuilder.equal(criteriaBuilder.trim(path), ""));
            case NOT_BLANK -> criteriaBuilder.and(criteriaBuilder.isNotNull(path), criteriaBuilder.notEqual(criteriaBuilder.trim(path), ""));
            case null -> throw new InvalidQueryException("StringColumnFilter operator not recognised");
        };
    }

    private Predicate numericPredicateBuilder(NumericColumnFilter columnFilter) {
        final Path<Number> path = githubUserRoot.get(GithubUserMapper.COLUMN_TO_FIELD_MAP.getOrDefault(columnFilter.getColumn(), columnFilter.getColumn()));
        return switch (columnFilter.getOperator()) {
            case EQUALS -> criteriaBuilder.equal(path, columnFilter.getValue());
            case NOT_EQUALS -> criteriaBuilder.notEqual(path, columnFilter.getValue());
            case GREATER_THAN -> criteriaBuilder.gt(path, columnFilter.getValue());
            case LESSER_THAN -> criteriaBuilder.lt(path, columnFilter.getValue());
            case null, default -> throw new IllegalStateException("NumericColumnFilter operator not recognised: " + columnFilter.getOperator());
        };
    }
}
