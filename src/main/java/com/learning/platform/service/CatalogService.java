package com.learning.platform.service;

import com.learning.platform.dto.CatalogDtos.CategoryRequest;
import com.learning.platform.dto.CatalogDtos.CategoryResponse;
import com.learning.platform.dto.CatalogDtos.TagRequest;
import com.learning.platform.dto.CatalogDtos.TagResponse;
import com.learning.platform.entity.Category;
import com.learning.platform.entity.Tag;
import com.learning.platform.exception.ConflictException;
import com.learning.platform.exception.NotFoundException;
import com.learning.platform.repository.CategoryRepository;
import com.learning.platform.repository.TagRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Справочники - категории и теги курсов. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CatalogService {

    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;

    public List<CategoryResponse> listCategories() {
        return categoryRepository.findAll(Sort.by("id")).stream().map(CategoryResponse::from).toList();
    }

    public CategoryResponse getCategory(Long id) {
        return CategoryResponse.from(getCategoryEntity(id));
    }

    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        if (categoryRepository.existsByNameIgnoreCase(request.name())) {
            throw new ConflictException("Category '" + request.name() + "' already exists");
        }
        return CategoryResponse.from(categoryRepository.save(new Category(request.name())));
    }

    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = getCategoryEntity(id);
        if (!category.getName().equalsIgnoreCase(request.name()) && categoryRepository.existsByNameIgnoreCase(request.name())) {
            throw new ConflictException("Category '" + request.name() + "' already exists");
        }
        category.setName(request.name());
        return CategoryResponse.from(category);
    }

    /** Удаление категории - курсы остаются, но теряют категорию. */
    @Transactional
    public void deleteCategory(Long id) {
        Category category = getCategoryEntity(id);
        category.getCourses().forEach(course -> course.setCategory(null));
        categoryRepository.delete(category);
    }

    public List<TagResponse> listTags() {
        return tagRepository.findAll(Sort.by("name")).stream().map(TagResponse::from).toList();
    }

    @Transactional
    public TagResponse createTag(TagRequest request) {
        if (tagRepository.existsByNameIgnoreCase(request.name())) {
            throw new ConflictException("Tag '" + request.name() + "' already exists");
        }
        return TagResponse.from(tagRepository.save(new Tag(request.name())));
    }

    /** Удаление тега - убираем его из курсов (строки course_tag), сами курсы не трогаем. */
    @Transactional
    public void deleteTag(Long id) {
        Tag tag = getTagEntity(id);
        List.copyOf(tag.getCourses()).forEach(course -> course.removeTag(tag));
        tagRepository.delete(tag);
    }

    Category getCategoryEntity(Long id) {
        return categoryRepository.findById(id).orElseThrow(() -> NotFoundException.of("Category", id));
    }

    Tag getTagEntity(Long id) {
        return tagRepository.findById(id).orElseThrow(() -> NotFoundException.of("Tag", id));
    }
}
