package com.subcycle.service;

import com.subcycle.dto.CategoryRequest;
import com.subcycle.dto.CategoryResponse;
import com.subcycle.entity.Category;
import com.subcycle.entity.User;
import com.subcycle.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    public List<CategoryResponse> getCategories(User user) {
        return categoryRepository.findByUser(user).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public CategoryResponse createCategory(User user, CategoryRequest request) {
        Category category = new Category();
        category.setUser(user);
        category.setName(request.getName());
        category.setColor(request.getColor() != null ? request.getColor() : "#409EFF");
        category.setIcon(request.getIcon() != null ? request.getIcon() : "folder");
        category.setSortOrder(request.getSortOrder());
        category = categoryRepository.save(category);
        return toResponse(category);
    }

    public CategoryResponse updateCategory(User user, Long id, CategoryRequest request) {
        Category category = categoryRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "類別不存在或無權限"));

        category.setName(request.getName());
        if (request.getColor() != null) {
            category.setColor(request.getColor());
        }
        if (request.getIcon() != null) {
            category.setIcon(request.getIcon());
        }
        if (request.getSortOrder() != null) {
            category.setSortOrder(request.getSortOrder());
        }

        category = categoryRepository.save(category);
        return toResponse(category);
    }

    public void deleteCategory(User user, Long id) {
        Category category = categoryRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "類別不存在或無權限"));

        categoryRepository.delete(category);
    }

    private CategoryResponse toResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getColor(),
                category.getIcon(),
                category.getSortOrder()
        );
    }
}
