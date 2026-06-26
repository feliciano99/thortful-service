package com.thortful.cards.application;

import com.thortful.cards.domain.CardNotFoundException;
import com.thortful.cards.domain.Category;
import com.thortful.cards.domain.GreetingCard;
import com.thortful.cards.infrastructure.persistence.GreetingCardRepository;
import com.thortful.cards.infrastructure.persistence.GreetingCardSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CardService {

    private final GreetingCardRepository repository;
    private final CardMapper mapper;

    @Transactional(readOnly = true)
    public Page<GreetingCardResponse> search(String search, Category category, Pageable pageable) {
        Specification<GreetingCard> specification = GreetingCardSpecifications.filter(search, category);
        return repository.findAll(specification, pageable).map(mapper::toResponse);
    }

    @Transactional
    public GreetingCardResponse create(CreateCardRequest request) {
        GreetingCard saved = repository.save(mapper.toEntity(request));
        return mapper.toResponse(saved);
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new CardNotFoundException(id);
        }
        repository.deleteById(id);
    }
}
