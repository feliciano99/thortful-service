package com.thortful.cards.infrastructure.persistence;

import com.thortful.cards.domain.Category;
import com.thortful.cards.domain.GreetingCard;
import com.thortful.cards.domain.StockStatus;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.domain.Specification;

public final class GreetingCardSpecifications {

    private GreetingCardSpecifications() {
    }

    public static Specification<GreetingCard> filter(String search, Category category, StockStatus stockStatus) {
        List<Specification<GreetingCard>> specs = new ArrayList<>();
        Optional.ofNullable(titleOrArtistContains(search)).ifPresent(specs::add);
        Optional.ofNullable(categoryEquals(category)).ifPresent(specs::add);
        Optional.ofNullable(stockStatusEquals(stockStatus)).ifPresent(specs::add);
        return Specification.allOf(specs);
    }

    private static Specification<GreetingCard> titleOrArtistContains(String search) {
        if (search == null || search.isBlank()) {
            return null;
        }
        String pattern = "%" + search.strip().toLowerCase() + "%";
        return (root, _, cb) -> cb.or(
                cb.like(cb.lower(root.get("title")), pattern),
                cb.like(cb.lower(root.get("artist")), pattern)
        );
    }

    private static Specification<GreetingCard> categoryEquals(Category category) {
        if (category == null) {
            return null;
        }
        return (root, _, cb) -> cb.equal(root.get("category"), category);
    }

    private static Specification<GreetingCard> stockStatusEquals(StockStatus stockStatus) {
        if (stockStatus == null) {
            return null;
        }
        return (root, _, cb) -> cb.equal(root.get("stockStatus"), stockStatus);
    }
}
