package com.thortful.cards.domain;

public class CardNotFoundException extends RuntimeException {

    public CardNotFoundException(Long id) {
        super("Greeting card not found: " + id);
    }
}
