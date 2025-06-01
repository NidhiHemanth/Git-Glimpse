package org.bootcamp.com.repository.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.*;
import jakarta.transaction.Transactional;
import org.bootcamp.com.dto.GithubUserDto;
import org.bootcamp.com.model.GithubUser;
import org.bootcamp.com.repository.GithubCustomJpaRepository;
import org.bootcamp.com.request.GithubUserFetchRequest;
import org.bootcamp.com.request.filter.ColumnFilter;
import org.bootcamp.com.request.sort.SortColumn;
import org.bootcamp.com.response.GithubUserFetchResponse;
import org.bootcamp.com.utils.GithubUserMapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

public class GithubJpaRepositoryImpl implements GithubCustomJpaRepository {
    private final EntityManager entityManager;

    @Autowired
    public GithubJpaRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public GithubUserFetchResponse fetchUsers(GithubUserFetchRequest fetchRequest) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<GithubUser> criteriaQuery = criteriaBuilder.createQuery(GithubUser.class);
        Root<GithubUser> githubUserRoot = criteriaQuery.from(GithubUser.class);

        List<Predicate> predicates = columnFilters(fetchRequest, criteriaBuilder, githubUserRoot);
        if(!predicates.isEmpty()) {
            criteriaQuery.where(predicates.toArray(new Predicate[0]));
        }

        List<Order> orders = sortingOrder(fetchRequest, criteriaBuilder, githubUserRoot);
        if (!orders.isEmpty()) {
            criteriaQuery.orderBy(orders);
        }

        final List<GithubUserDto> responses = entityManager
                                                .createQuery(criteriaQuery)
                                                .setMaxResults(fetchRequest.getEndRow() - fetchRequest.getStartRow() + 1)
                                                .setFirstResult(fetchRequest.getStartRow())
                                                .getResultList()
                                                .stream()
                                                .map(GithubUserMapper.INSTANCE::toDto)
                                                .toList();

        final Integer lastRow = responses.isEmpty() ? -1 : fetchRequest.getStartRow() + responses.size();
        return new GithubUserFetchResponse(responses, lastRow);
    }

    private List<Predicate> columnFilters(GithubUserFetchRequest request, CriteriaBuilder criteriaBuilder, Root<GithubUser> githubUserRoot) {
        final List<Predicate> predicates = new ArrayList<>();
        final PredicateBuilder predicateBuilder = new PredicateBuilder(criteriaBuilder, githubUserRoot);
        for(ColumnFilter columnFilter: request.getColumnFilters()) {
            predicates.add(predicateBuilder.getPredicate(columnFilter));
        }
        return predicates;
    }

    private List<Order> sortingOrder(GithubUserFetchRequest request, CriteriaBuilder criteriaBuilder, Root<GithubUser> githubUserRoot) {
        return request.getSortColumns()
                .stream()
                .map(sortColumn -> {
                    if(sortColumn.getSortDirection() == SortColumn.Direction.ASCENDING) {
                        return criteriaBuilder.asc(githubUserRoot.get(sortColumn.getColumn()));
                    } else {
                        return criteriaBuilder.desc(githubUserRoot.get(sortColumn.getColumn()));
                    }
                })
                .toList();
    }
}
