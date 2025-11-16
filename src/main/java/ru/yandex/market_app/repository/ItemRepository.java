package ru.yandex.market_app.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import ru.yandex.market_app.model.entity.ItemEntity;

@Repository
public interface ItemRepository extends JpaRepository<ItemEntity, Long>, JpaSpecificationExecutor<ItemEntity> {

    Optional<ItemEntity> findByTitle(String title);

    List<ItemEntity> findAllByCartCountGreaterThan(Integer count);

    @Modifying
    @Query(value = """
        update ItemEntity ie set ie.cartCount = 0 where ie.cartCount != 0 
    """)
    void upadteAll();
}
