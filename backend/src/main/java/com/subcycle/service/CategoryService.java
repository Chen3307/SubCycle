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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@SuppressWarnings("null")
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    public List<CategoryResponse> getCategories(User user) {
        return ensureDefaultCategories(user).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * 確保該用戶至少擁有預設類別，若沒有則建立並返回。
     */
    public List<Category> ensureDefaultCategories(User user) {
        List<Category> existing = categoryRepository.findByUser(user);
        List<Category> defaults = buildDefaultCategories(user);

        // 以名稱檢查缺少的預設類別（不覆蓋已有類別）
        var existingNames = existing.stream()
                .map(Category::getName)
                .filter(name -> name != null)
                .map(String::trim)
                .collect(Collectors.toSet());

        List<Category> toCreate = defaults.stream()
                .filter(def -> !existingNames.contains(def.getName()))
                .collect(Collectors.toList());

        if (!toCreate.isEmpty()) {
            existing.addAll(categoryRepository.saveAll(toCreate));
        }

        return existing;
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

    private List<Category> buildDefaultCategories(User user) {
        return Arrays.asList(
                buildCategory(user, "影音娛樂", "play-circle", "#EF4444", 1),
                buildCategory(user, "工作生產力", "briefcase", "#3B82F6", 2),
                buildCategory(user, "生活與購物", "shopping-cart", "#10B981", 3),
                buildCategory(user, "遊戲與社群", "gamepad", "#8B5CF6", 4),
                buildCategory(user, "其他項目", "folder", "#F59E0B", 5));
    }

    private Category buildCategory(User user, String name, String icon, String color, int sortOrder) {
        Category category = new Category();
        category.setUser(user);
        category.setName(name);
        category.setIcon(icon);
        category.setColor(color);
        category.setSortOrder(sortOrder);
        category.setIsDefault(true);
        return category;
    }

    private CategoryResponse toResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getColor(),
                category.getIcon(),
                category.getSortOrder(),
                category.getIsDefault());
    }
}
