package com.thortful.cards.application;

import com.thortful.cards.domain.Category;
import com.thortful.cards.domain.StockStatus;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record CreateCardRequest(
        @NotBlank @Size(max = 200) String title,
        @NotNull Category category,
        @NotBlank @Size(max = 150) String artist,
        @NotNull @Positive @Digits(integer = 6, fraction = 2) BigDecimal price,
        @NotNull StockStatus stockStatus
) {
}
