package ru.yandex.market_app.model.entity;

import java.io.Serializable;
import java.util.Objects;

import org.hibernate.proxy.HibernateProxy;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder.Default;

@Getter
@Setter
@Entity
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "item")
public class ItemEntity implements Serializable {

    @Id
    @Column(name = "id")
    @SequenceGenerator(name = "item_id_seq_gen", sequenceName = "item_id_seq")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "item_id_seq_gen")
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "text", nullable = false)
    private String description;

    @Column(name = "image", nullable = false)
    private String image;

    @Column(name = "price", nullable = false)
    private Integer price;

    @Default
    @Column(name = "cart_count", nullable = false)
    private Integer cartCount = 0;

    @Override 
    public final boolean equals(Object o) { 
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy proxy ? proxy.getHibernateLazyInitializer().getPersistentClass() : o.getClass(); 
        Class<?> thisEffectiveClass = this instanceof HibernateProxy proxy ? proxy.getHibernateLazyInitializer().getPersistentClass() : this.getClass(); 
        if (thisEffectiveClass != oEffectiveClass) return false; 
        ItemEntity item = (ItemEntity) o; 
        return getId() != null && Objects.equals(getId(), item.getId()); 
    }
    
    @Override 
    public final int hashCode() { 
        return this instanceof HibernateProxy proxy ? proxy.getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode(); 
    }
}
