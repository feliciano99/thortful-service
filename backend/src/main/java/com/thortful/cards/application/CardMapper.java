package com.thortful.cards.application;

import com.thortful.cards.domain.GreetingCard;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CardMapper {

    GreetingCardResponse toResponse(GreetingCard card);

    @Mapping(target = "id", ignore = true)
    GreetingCard toEntity(CreateCardRequest request);
}
