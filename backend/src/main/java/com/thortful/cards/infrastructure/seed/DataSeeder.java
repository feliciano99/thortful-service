package com.thortful.cards.infrastructure.seed;

import com.thortful.cards.domain.Category;
import com.thortful.cards.domain.GreetingCard;
import com.thortful.cards.domain.StockStatus;
import com.thortful.cards.infrastructure.persistence.GreetingCardRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Gatherers;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import net.datafaker.Faker;
import org.jspecify.annotations.NullMarked;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@NullMarked
@Component
@ConditionalOnProperty(prefix = "app.seeding", name = "enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class DataSeeder implements ApplicationRunner {

    private static final int TARGET_COUNT = 1_200;
    private static final int BATCH_SIZE = 300;

    private final GreetingCardRepository repository;
    private final Faker faker = new Faker();

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (repository.count() > 0) {
            return;
        }
        Stream.generate(this::randomCard)
                .limit(TARGET_COUNT)
                .gather(Gatherers.windowFixed(BATCH_SIZE))
                .forEach(this::persist);
    }

    private void persist(List<GreetingCard> batch) {
        repository.saveAll(batch);
    }

    private GreetingCard randomCard() {
        return GreetingCard.builder()
                .title(faker.book().title())
                .category(faker.options().option(Category.class))
                .artist(faker.name().fullName())
                .price(randomPrice())
                .stockStatus(faker.options().option(StockStatus.class))
                .build();
    }

    private BigDecimal randomPrice() {
        return BigDecimal.valueOf(faker.number().randomDouble(2, 1, 12))
                .setScale(2, RoundingMode.HALF_UP);
    }
}
