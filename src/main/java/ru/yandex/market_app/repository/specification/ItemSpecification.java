package ru.yandex.market_app.repository.specification;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.springframework.data.relational.core.query.Criteria;

import lombok.NoArgsConstructor;
import ru.yandex.market_app.model.filter.ItemFilterModel;

@NoArgsConstructor
public class ItemSpecification {

    public Criteria toCriteria(ItemFilterModel filter) {
        List<Criteria> criterias = new ArrayList<>();

        filter.getSearch()
            .filter(Predicate.not(String::isBlank))
            .map(String::trim)
            .ifPresent(search -> {
                String pattern = "%" + search.toLowerCase() + "%";

                criterias.add(
                    Criteria
                        .where("title").like(pattern)
                        .or(Criteria.where("description").like(pattern))
                );
            });

        return Criteria.from(criterias);
    }
}
