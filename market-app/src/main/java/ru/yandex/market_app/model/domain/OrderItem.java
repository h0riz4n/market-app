package ru.yandex.market_app.model.domain;

import java.io.Serializable;
import java.util.Objects;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Embedded;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.yandex.market_app.model.domain.id.OrderItemId;

@Getter
@Setter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class OrderItem implements Serializable {

    @Id
    @Embedded.Nullable
    private OrderItemId id;

    @Column("quantity")
    private Long quantity;

    @Transient
    private Item item;

    @Transient
    private Order order;

    @Override 
    public final boolean equals(Object o) { 
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o.getClass(); 
        Class<?> thisEffectiveClass = this.getClass(); 
        if (thisEffectiveClass != oEffectiveClass) return false; 
        OrderItem orderItem = (OrderItem) o; 
        return getId() != null && Objects.equals(getId(), orderItem.getId()); 
    }
    
    @Override 
    public final int hashCode() { 
        return getId().hashCode(); 
    }
}
