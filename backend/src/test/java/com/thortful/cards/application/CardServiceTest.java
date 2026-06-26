package com.thortful.cards.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thortful.cards.domain.CardNotFoundException;
import com.thortful.cards.domain.Category;
import com.thortful.cards.domain.GreetingCard;
import com.thortful.cards.domain.StockStatus;
import com.thortful.cards.infrastructure.persistence.GreetingCardRepository;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private GreetingCardRepository repository;

    @Mock
    private CardMapper mapper;

    @InjectMocks
    private CardService service;

    @Test
    void searchMapsEntityPageToResponsePage() {
        GreetingCard card = card();
        GreetingCardResponse response = response();
        Pageable pageable = PageRequest.of(0, 20);
        when(repository.findAll(ArgumentMatchers.<Specification<GreetingCard>>any(), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(card), pageable, 1));
        when(mapper.toResponse(card)).thenReturn(response);

        Page<GreetingCardResponse> result = service.search("birthday", Category.BIRTHDAY, pageable);

        assertThat(result.getContent()).containsExactly(response);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void createSavesMappedEntityAndReturnsResponse() {
        CreateCardRequest request =
                new CreateCardRequest("Happy Birthday", Category.BIRTHDAY, "Artist", BigDecimal.valueOf(3.50), StockStatus.IN_STOCK);
        GreetingCard entity = card();
        GreetingCard saved = card();
        GreetingCardResponse response = response();
        when(mapper.toEntity(request)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(saved);
        when(mapper.toResponse(saved)).thenReturn(response);

        assertThat(service.create(request)).isEqualTo(response);
    }

    @Test
    void deleteRemovesWhenCardExists() {
        when(repository.existsById(1L)).thenReturn(true);

        service.delete(1L);

        verify(repository).deleteById(1L);
    }

    @Test
    void deleteThrowsWhenCardMissing() {
        when(repository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> service.delete(99L))
                .isInstanceOf(CardNotFoundException.class)
                .hasMessageContaining("99");
        verify(repository, never()).deleteById(any());
    }

    private static GreetingCard card() {
        return GreetingCard.builder()
                .title("Happy Birthday")
                .category(Category.BIRTHDAY)
                .artist("Artist")
                .price(BigDecimal.valueOf(3.50))
                .stockStatus(StockStatus.IN_STOCK)
                .build();
    }

    private static GreetingCardResponse response() {
        return new GreetingCardResponse(1L, "Happy Birthday", Category.BIRTHDAY, "Artist", BigDecimal.valueOf(3.50), StockStatus.IN_STOCK);
    }
}
