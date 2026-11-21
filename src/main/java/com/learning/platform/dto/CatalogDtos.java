package com.learning.platform.dto;

import com.learning.platform.entity.Category;
import com.learning.platform.entity.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** DTO для справочников - категории и теги */
public final class CatalogDtos {

    private CatalogDtos() {
    }

    public record CategoryRequest(@NotBlank @Size(max = 100) String name) {
    }

    public record CategoryResponse(Long id, String name) {
        public static CategoryResponse from(Category c) {
            return c == null ? null : new CategoryResponse(c.getId(), c.getName());
        }
    }

    public record TagRequest(@NotBlank @Size(max = 50) String name) {
    }

    public record TagResponse(Long id, String name) {
        public static TagResponse from(Tag t) {
            return new TagResponse(t.getId(), t.getName());
        }
    }
}
