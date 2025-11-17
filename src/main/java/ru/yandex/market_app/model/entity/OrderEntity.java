package ru.yandex.market_app.model.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.hibernate.proxy.HibernateProxy;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder.Default;

@Entity
@Getter
@Setter
@Table(name = "order")
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class OrderEntity implements Serializable {

    @Id
    @Column(name = "id")
    @SequenceGenerator(name = "order_id_seq_gen", sequenceName = "order_id_seq")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "order_id_seq_gen")
    private Long id;

    @Column(name = "total", nullable = false)
    private Integer total;

    @Default
    @OneToMany(mappedBy = "id.order", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<OrderItemEntity> items = new ArrayList<>();

    @Override 
    public final boolean equals(Object o) { 
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy proxy ? proxy.getHibernateLazyInitializer().getPersistentClass() : o.getClass(); 
        Class<?> thisEffectiveClass = this instanceof HibernateProxy proxy ? proxy.getHibernateLazyInitializer().getPersistentClass() : this.getClass(); 
        if (thisEffectiveClass != oEffectiveClass) return false; 
        OrderEntity order = (OrderEntity) o; 
        return getId() != null && Objects.equals(getId(), order.getId()); 
    }
    
    @Override 
    public final int hashCode() { 
        return this instanceof HibernateProxy proxy ? proxy.getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode(); 
    }
}
