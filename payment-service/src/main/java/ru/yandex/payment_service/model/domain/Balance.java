package ru.yandex.payment_service.model.domain;

import java.math.BigDecimal;
import java.util.Objects;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Table(name = "balance")
public class Balance {

    @Id
    @Column("id")
    private Long id;

    @Column("balance")
    private BigDecimal balance;

    @Override
    public final boolean equals(Object o) { 
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o.getClass();
        Class<?> thisEffectiveClass = this.getClass(); 
        if (thisEffectiveClass != oEffectiveClass) return false; 
        Balance balance = (Balance) o; 
        return getId() != null && Objects.equals(getId(), balance.getId()); 
    }
}
