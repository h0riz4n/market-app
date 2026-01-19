package ru.yandex.market_app.model.domain;

import java.io.Serializable;
import java.util.Objects;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder.Default;

@Table("item")
@Builder(toBuilder = true)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Item implements Serializable {

    @Id
    @Column("id")
    private Long id;

    @Column("title")
    private String title;

    @Column("description")
    private String description;

    @Column("image")
    private String image;

    @Column("price")
    private Integer price;

    @Default
    @Transient
    private Long cartCount = 0L;

    @Override 
    public final boolean equals(Object o) { 
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o.getClass(); 
        Class<?> thisEffectiveClass = this.getClass(); 
        if (thisEffectiveClass != oEffectiveClass) return false; 
        Item item = (Item) o; 
        return getId() != null && Objects.equals(getId(), item.getId()); 
    }
    
    @Override 
    public final int hashCode() { 
        return getId().hashCode(); 
    }
}
