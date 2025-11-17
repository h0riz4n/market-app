package ru.yandex.market_app.runner;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ru.yandex.market_app.model.entity.ItemEntity;
import ru.yandex.market_app.repository.ItemRepository;

@Component
@Profile({ "!test" })
@RequiredArgsConstructor
public class ItemRunner implements CommandLineRunner {

    private final ItemRepository itemRepo;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        ItemEntity ball = getOrCreate(
            "Футбольный мяч",
            "Большой футбольный мяч для игры на улице",
            100,
            "image/1.jpg"
        );

        ItemEntity umbrella = getOrCreate(
            "Зонтик складной",
            "Компактный и прочный зонт для дождливой погоды",
            150,
            "image/2.jpg"
        );

        ItemEntity headphones = getOrCreate(
            "Наушники",
            "Беспроводные наушники с шумоподавлением",
            3200,
            "image/3.jpeg"
        );

        ItemEntity backpack = getOrCreate(
            "Рюкзак городской",
            "Лёгкий и вместительный рюкзак для повседневного использования",
            1800,
            "image/4.jpg"
        );

        ItemEntity watch = getOrCreate(
            "Умные часы",
            "Смарт-часы с мониторингом активности и уведомлениями",
            5400,
            "image/5.jpg"
        );

        itemRepo.saveAll(List.of(ball, umbrella, headphones, backpack, watch));
    }

    private ItemEntity getOrCreate(String title, String description, Integer price, String image) {
        return itemRepo.findByTitle(title)
            .orElse(createItem(title, description, price, image));
    }
    
    private ItemEntity createItem(String title, String description, Integer price, String imagePath) {
        return ItemEntity.builder()
            .title(title)
            .description(description)
            .price(price)
            .image(imagePath)
            .build();
    }
}
