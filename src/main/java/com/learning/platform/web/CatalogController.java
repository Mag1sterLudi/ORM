package com.learning.platform.web;

import com.learning.platform.dto.CatalogDtos.CategoryRequest;
import com.learning.platform.dto.CatalogDtos.CategoryResponse;
import com.learning.platform.dto.CatalogDtos.TagRequest;
import com.learning.platform.dto.CatalogDtos.TagResponse;
import com.learning.platform.service.CatalogService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Catalog", description = "Категории и теги курсов")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CatalogController {

    private final CatalogService catalogService;

    @GetMapping("/categories")
    public List<CategoryResponse> listCategories() {
        return catalogService.listCategories();
    }

    @GetMapping("/categories/{id}")
    public CategoryResponse getCategory(@PathVariable Long id) {
        return catalogService.getCategory(id);
    }

    @PostMapping("/categories")
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponse createCategory(@Valid @RequestBody CategoryRequest request) {
        return catalogService.createCategory(request);
    }

    @PutMapping("/categories/{id}")
    public CategoryResponse updateCategory(@PathVariable Long id, @Valid @RequestBody CategoryRequest request) {
        return catalogService.updateCategory(id, request);
    }

    @DeleteMapping("/categories/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable Long id) {
        catalogService.deleteCategory(id);
    }

    @GetMapping("/tags")
    public List<TagResponse> listTags() {
        return catalogService.listTags();
    }

    @PostMapping("/tags")
    @ResponseStatus(HttpStatus.CREATED)
    public TagResponse createTag(@Valid @RequestBody TagRequest request) {
        return catalogService.createTag(request);
    }

    @DeleteMapping("/tags/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTag(@PathVariable Long id) {
        catalogService.deleteTag(id);
    }
}
