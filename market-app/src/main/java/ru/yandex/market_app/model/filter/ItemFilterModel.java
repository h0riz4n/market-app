package ru.yandex.market_app.model.filter;

import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ItemFilterModel {

    private String search;

    public Optional<String> getSearch() {
        return Optional.ofNullable(this.search);
    }
}
