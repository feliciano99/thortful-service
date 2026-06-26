package com.thortful.cards.application;

import com.thortful.cards.domain.Category;
import com.thortful.cards.domain.StockStatus;
import java.math.BigDecimal;

public record GreetingCardResponse(
        Long id,
        String title,
        Category category,
        String artist,
        BigDecimal price,
        StockStatus stockStatus
) {
}
