package com.thortful.cards.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Category {
    BIRTHDAY("Birthday"),
    ANNIVERSARY("Anniversary"),
    THANK_YOU("Thank You"),
    CONGRATULATIONS("Congratulations"),
    GET_WELL("Get Well"),
    WEDDING("Wedding"),
    NEW_BABY("New Baby"),
    CHRISTMAS("Christmas"),
    VALENTINES_DAY("Valentine's Day"),
    SYMPATHY("Sympathy");

    private final String label;
}
