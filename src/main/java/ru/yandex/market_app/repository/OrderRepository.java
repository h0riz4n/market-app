package ru.yandex.market_app.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import ru.yandex.market_app.model.entity.OrderEntity;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    @Query(value = """
        select oe from OrderEntity oe 
        join fetch oe.items
        join fetch oe.items.id.item
        """)
    List<OrderEntity> findAll();

    @Query(value = """
        select oe from OrderEntity oe 
        join fetch oe.items
        join fetch oe.items.id.item
        where oe.id = :id
    """)
    Optional<OrderEntity> findById(Long id);
}
