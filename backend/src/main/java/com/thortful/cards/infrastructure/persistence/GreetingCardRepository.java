package com.thortful.cards.infrastructure.persistence;

import com.thortful.cards.domain.GreetingCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface GreetingCardRepository
        extends JpaRepository<GreetingCard, Long>, JpaSpecificationExecutor<GreetingCard> {
}
