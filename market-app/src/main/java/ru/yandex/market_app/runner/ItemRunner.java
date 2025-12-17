package ru.yandex.market_app.runner;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import ru.yandex.market_app.model.domain.Item;
import ru.yandex.market_app.repository.ItemRepository;

@Component
@Profile({ "!test" })
@RequiredArgsConstructor
public class ItemRunner implements CommandLineRunner {

    private final ItemRepository itemRepo;

    @Override
    public void run(String... args) throws Exception {
        getOrCreate(
            "Футбольный мяч",
            "Большой футбольный мяч для игры на улице",
            100,
            "image/1.jpg"
        ).subscribe();

        getOrCreate(
            "Зонтик складной",
            "Компактный и прочный зонт для дождливой погоды",
            150,
            "image/2.jpg"
        ).subscribe();

        getOrCreate(
            "Наушники",
            "Беспроводные наушники с шумоподавлением",
            3200,
            "image/3.jpeg"
        ).subscribe();

        getOrCreate(
            "Рюкзак городской",
            "Лёгкий и вместительный рюкзак для повседневного использования",
            1800,
            "image/4.jpg"
        ).subscribe();

        getOrCreate(
            "Умные часы",
            "Смарт-часы с мониторингом активности и уведомлениями",
            5400,
            "image/5.jpg"
        ).subscribe();
    }

    private Mono<Item> getOrCreate(String title, String description, Integer price, String image) {
        return itemRepo.findByTitle(title)
            .switchIfEmpty(itemRepo.save(createItem(title, description, price, image)));
        
    }

    private Item createItem(String title, String description, Integer price, String imagePath) {
        return Item.builder()
            .title(title)
            .description(description)
            .price(price)
            .image(imagePath)
            .build();
    }
}
