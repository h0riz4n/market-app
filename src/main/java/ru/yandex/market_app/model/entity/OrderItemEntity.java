package ru.yandex.market_app.model.entity;

import java.io.Serializable;
import java.util.Objects;

import org.hibernate.proxy.HibernateProxy;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.yandex.market_app.model.entity.id.OrderItemId;

@Entity
@Getter
@Setter
@Table(name = "order_item")
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class OrderItemEntity implements Serializable {

    @EmbeddedId
    private OrderItemId id;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Override 
    public final boolean equals(Object o) { 
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy proxy ? proxy.getHibernateLazyInitializer().getPersistentClass() : o.getClass(); 
        Class<?> thisEffectiveClass = this instanceof HibernateProxy proxy ? proxy.getHibernateLazyInitializer().getPersistentClass() : this.getClass(); 
        if (thisEffectiveClass != oEffectiveClass) return false; 
        OrderItemEntity orderItem = (OrderItemEntity) o; 
        return getId() != null && Objects.equals(getId(), orderItem.getId()); 
    }
    
    @Override 
    public final int hashCode() { 
        return this instanceof HibernateProxy proxy ? proxy.getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode(); 
    }
}
