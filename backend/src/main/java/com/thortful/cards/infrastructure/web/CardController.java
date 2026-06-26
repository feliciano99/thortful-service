package com.thortful.cards.infrastructure.web;

import com.thortful.cards.application.CardService;
import com.thortful.cards.application.CreateCardRequest;
import com.thortful.cards.application.GreetingCardResponse;
import com.thortful.cards.domain.Category;
import com.thortful.cards.domain.StockStatus;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/cards")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;

    @GetMapping
    public PagedModel<GreetingCardResponse> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Category category,
            @RequestParam(required = false) StockStatus stockStatus,
            Pageable pageable) {
        Page<GreetingCardResponse> page = cardService.search(search, category, stockStatus, pageable);
        return new PagedModel<>(page);
    }

    @PostMapping
    public ResponseEntity<GreetingCardResponse> create(@Valid @RequestBody CreateCardRequest request) {
        GreetingCardResponse created = cardService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        cardService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
