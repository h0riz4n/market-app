package ru.yandex.market_app.model.domain;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Table("order")
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Order implements Serializable {

    @Id
    @Column("id")
    private Long id;

    @Column("user_id")
    private UUID userId;

    @Column("creation_date_time")
    private LocalDateTime creationDateTime;

    @Column("total")
    private Integer total;

    @Default
    @Transient
    private List<OrderItem> items = new ArrayList<>();
    
    @Override 
    public final boolean equals(Object o) { 
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o.getClass(); 
        Class<?> thisEffectiveClass = this.getClass(); 
        if (thisEffectiveClass != oEffectiveClass) return false; 
        Order order = (Order) o; 
        return getId() != null && Objects.equals(getId(), order.getId()); 
    }
    
    @Override 
    public final int hashCode() { 
        return getId().hashCode(); 
    }
}
