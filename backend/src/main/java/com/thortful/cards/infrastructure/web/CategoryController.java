package com.thortful.cards.infrastructure.web;

import com.thortful.cards.application.CategoryResponse;
import com.thortful.cards.domain.Category;
import java.util.Arrays;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    @GetMapping
    public List<CategoryResponse> list() {
        return Arrays.stream(Category.values())
                .map(category -> new CategoryResponse(category.name(), category.getLabel()))
                .toList();
    }
}
