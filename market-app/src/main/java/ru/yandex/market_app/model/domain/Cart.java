package ru.yandex.market_app.model.domain;

import java.io.Serializable;
import java.util.Objects;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.yandex.market_app.model.domain.id.CartId;

@Getter
@Setter
@Table("cart")
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Cart implements Serializable {

    @Id
    @Embedded.Nullable
    private CartId id;

    @Column("count")
    private Long count;

    @Transient
    private Item item;

    @Override 
    public final boolean equals(Object o) { 
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o.getClass(); 
        Class<?> thisEffectiveClass = this.getClass(); 
        if (thisEffectiveClass != oEffectiveClass) return false; 
        Cart cart = (Cart) o; 
        return getId() != null && Objects.equals(getId(), cart.getId()); 
    }
    
    @Override 
    public final int hashCode() { 
        return getId().hashCode(); 
    }
}
