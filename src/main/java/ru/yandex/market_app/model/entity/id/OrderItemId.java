package ru.yandex.market_app.model.entity.id;

import java.io.Serializable;
import java.util.Objects;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import jakarta.persistence.Embeddable;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.yandex.market_app.model.entity.ItemEntity;
import ru.yandex.market_app.model.entity.OrderEntity;

@Getter
@Setter
@Builder(toBuilder = true)
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemId implements Serializable {

    @OnDelete(action = OnDeleteAction.CASCADE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private OrderEntity order;

    @OnDelete(action = OnDeleteAction.CASCADE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private ItemEntity item;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrderItemId)) return false;
        OrderItemId id = (OrderItemId) o;
        return Objects.equals(getOrder(), id.getOrder()) && Objects.equals(getItem(), id.getItem());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getOrder(), getItem());
    }
}
