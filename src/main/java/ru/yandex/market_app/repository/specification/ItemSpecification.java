package ru.yandex.market_app.repository.specification;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import ru.yandex.market_app.model.entity.ItemEntity;
import ru.yandex.market_app.model.entity.ItemEntity_;
import ru.yandex.market_app.model.filter.ItemFilterModel;

@RequiredArgsConstructor
public class ItemSpecification implements Specification<ItemEntity> {

    @NotNull
    private final ItemFilterModel filter;

    @Override
    public Predicate toPredicate(Root<ItemEntity> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        List<Predicate> predicates = new ArrayList<>();

        filter.getSearch()
            .filter(search -> !search.isBlank())
            .map(String::trim)
            .ifPresent(search -> {
                String pattern = "%" + search.toLowerCase() + "%";

                Predicate titlePredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get(ItemEntity_.TITLE)), pattern);
                Predicate descriptionPredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get(ItemEntity_.DESCRIPTION)), pattern);
                
                predicates.add(criteriaBuilder.or(titlePredicate, descriptionPredicate));
            });

        return predicates.isEmpty() ? null : criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }
}
