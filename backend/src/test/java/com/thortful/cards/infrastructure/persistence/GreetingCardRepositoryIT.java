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
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
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
                card("Happy Birthday", Category.BIRTHDAY, "Alice Painter", StockStatus.IN_STOCK),
                card("Birthday Bash", Category.BIRTHDAY, "Bob Draws", StockStatus.LOW_STOCK),
                card("Get Well Soon", Category.GET_WELL, "Carol Inkwell", StockStatus.OUT_OF_STOCK)
        ));
    }

    @Test
    void filtersByCategory() {
        Page<GreetingCard> page =
                repository.findAll(GreetingCardSpecifications.filter(null, Category.BIRTHDAY, null), PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent()).allMatch(card -> card.getCategory() == Category.BIRTHDAY);
    }

    @Test
    void searchesTitleCaseInsensitive() {
        Page<GreetingCard> page =
                repository.findAll(GreetingCardSpecifications.filter("BIRTHDAY", null, null), PageRequest.of(0, 10));

        assertThat(page.getContent())
                .extracting(GreetingCard::getTitle)
                .containsExactlyInAnyOrder("Happy Birthday", "Birthday Bash");
    }

    @Test
    void searchesByArtist() {
        Page<GreetingCard> page =
                repository.findAll(GreetingCardSpecifications.filter("inkwell", null, null), PageRequest.of(0, 10));

        assertThat(page.getContent())
                .extracting(GreetingCard::getTitle)
                .containsExactly("Get Well Soon");
    }

    @Test
    void filtersByStockStatus() {
        Page<GreetingCard> page = repository.findAll(
                GreetingCardSpecifications.filter(null, null, StockStatus.OUT_OF_STOCK), PageRequest.of(0, 10));

        assertThat(page.getContent())
                .extracting(GreetingCard::getStockStatus)
                .containsExactly(StockStatus.OUT_OF_STOCK);
    }

    @Test
    void paginatesAndSorts() {
        Page<GreetingCard> page = repository.findAll(
                GreetingCardSpecifications.filter(null, null, null), PageRequest.of(0, 2, Sort.by("title")));

        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.getNumberOfElements()).isEqualTo(2);
        assertThat(page.getContent().getFirst().getTitle()).isEqualTo("Birthday Bash");
    }

    private static GreetingCard card(String title, Category category, String artist, StockStatus stockStatus) {
        return GreetingCard.builder()
                .title(title)
                .category(category)
                .artist(artist)
                .price(BigDecimal.valueOf(4.50))
                .stockStatus(stockStatus)
                .build();
    }
}
