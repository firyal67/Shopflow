package com.shopflow.service;

import com.shopflow.dto.request.CategoryRequest;
import com.shopflow.dto.response.CategoryResponse;
import com.shopflow.entity.Category;
import com.shopflow.exception.BusinessException;
import com.shopflow.exception.ResourceNotFoundException;
import com.shopflow.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategoryTree() {
        return categoryRepository.findByParentIsNull()
                .stream()
                .map(this::toResponseWithChildren)
                .collect(Collectors.toList());
    }

    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        if (categoryRepository.existsByNom(request.getNom())) {
            throw new BusinessException("Catégorie déjà existante : " + request.getNom());
        }
        Category category = Category.builder()
                .nom(request.getNom())
                .description(request.getDescription())
                .build();

        if (request.getParentId() != null) {
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Catégorie parente", request.getParentId()));
            category.setParent(parent);
        }

        return toResponse(categoryRepository.save(category));
    }

    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Catégorie", id));

        category.setNom(request.getNom());
        category.setDescription(request.getDescription());

        if (request.getParentId() != null) {
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Catégorie parente", request.getParentId()));
            category.setParent(parent);
        } else {
            category.setParent(null);
        }

        return toResponse(categoryRepository.save(category));
    }

    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Catégorie", id));
        categoryRepository.delete(category);
    }

    private CategoryResponse toResponseWithChildren(Category category) {
        List<CategoryResponse> children = category.getChildren().stream()
                .map(this::toResponseWithChildren)
                .collect(Collectors.toList());

        return CategoryResponse.builder()
                .id(category.getId())
                .nom(category.getNom())
                .description(category.getDescription())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .children(children)
                .build();
    }

    private CategoryResponse toResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .nom(category.getNom())
                .description(category.getDescription())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .build();
    }
}
