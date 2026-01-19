package ru.yandex.market_app.model.domain;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
@Builder(toBuilder = true)
public class User implements Serializable {

    @Id
    @Column("id")
    private UUID id;

    @Column("creation_date_time")
    private LocalDateTime creationDateTime;

    @Column("last_action_date_time")
    private LocalDateTime lastActionDateTime;

    @Override 
    public final boolean equals(Object o) { 
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o.getClass(); 
        Class<?> thisEffectiveClass = this.getClass(); 
        if (thisEffectiveClass != oEffectiveClass) return false; 
        User user = (User) o; 
        return getId() != null && Objects.equals(getId(), user.getId()); 
    }
    
    @Override 
    public final int hashCode() { 
        return getId().hashCode(); 
    }
}
