package com.thortful.cards.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.thortful.cards.TestcontainersConfiguration;
import com.thortful.cards.domain.Category;
import com.thortful.cards.domain.GreetingCard;
import com.thortful.cards.domain.StockStatus;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
class GreetingCardRepositoryIT {

    @Autowired
    private GreetingCardRepository repository;

    @BeforeEach
    void seed() {
        repository.saveAll(List.of(
                card("Happy Birthday", Category.BIRTHDAY),
                card("Birthday Bash", Category.BIRTHDAY),
                card("Get Well Soon", Category.GET_WELL)
        ));
    }

    @Test
    void filtersByCategory() {
        Page<GreetingCard> page =
                repository.findAll(GreetingCardSpecifications.filter(null, Category.BIRTHDAY), PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent()).allMatch(card -> card.getCategory() == Category.BIRTHDAY);
    }

    @Test
    void searchesTitleCaseInsensitive() {
        Page<GreetingCard> page =
                repository.findAll(GreetingCardSpecifications.filter("BIRTHDAY", null), PageRequest.of(0, 10));

        assertThat(page.getContent())
                .extracting(GreetingCard::getTitle)
                .containsExactlyInAnyOrder("Happy Birthday", "Birthday Bash");
    }

    @Test
    void paginatesAndSorts() {
        Page<GreetingCard> page = repository.findAll(
                GreetingCardSpecifications.filter(null, null), PageRequest.of(0, 2, Sort.by("title")));

        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.getNumberOfElements()).isEqualTo(2);
        assertThat(page.getContent().getFirst().getTitle()).isEqualTo("Birthday Bash");
    }

    private static GreetingCard card(String title, Category category) {
        return GreetingCard.builder()
                .title(title)
                .category(category)
                .artist("Test Artist")
                .price(BigDecimal.valueOf(4.50))
                .stockStatus(StockStatus.IN_STOCK)
                .build();
    }
}
