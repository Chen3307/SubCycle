package com.subcycle.service;

import com.subcycle.dto.CategoryRequest;
import com.subcycle.dto.CategoryResponse;
import com.subcycle.entity.Category;
import com.subcycle.entity.User;
import com.subcycle.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    private User testUser;
    private Category testCategory;
    private CategoryRequest categoryRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setName("Test User");

        testCategory = new Category();
        testCategory.setId(1L);
        testCategory.setUser(testUser);
        testCategory.setName("串流影音");
        testCategory.setIcon("play-circle");
        testCategory.setColor("#EF4444");
        testCategory.setSortOrder(1);

        categoryRequest = new CategoryRequest();
        categoryRequest.setName("串流影音");
        categoryRequest.setIcon("play-circle");
        categoryRequest.setColor("#EF4444");
        categoryRequest.setSortOrder(1);
    }

    @Test
    void testGetCategories_Success() {
        // Arrange
        Category category2 = new Category();
        category2.setId(2L);
        category2.setUser(testUser);
        category2.setName("音樂");
        category2.setIcon("music");
        category2.setColor("#F59E0B");
        category2.setSortOrder(2);

        List<Category> categories = Arrays.asList(testCategory, category2);
        when(categoryRepository.findByUser(testUser)).thenReturn(categories);

        // Act
        List<CategoryResponse> result = categoryService.getCategories(testUser);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("串流影音", result.get(0).getName());
        assertEquals("音樂", result.get(1).getName());
        verify(categoryRepository).findByUser(testUser);
    }

    @Test
    void testGetCategories_EmptyList() {
        // Arrange
        when(categoryRepository.findByUser(testUser)).thenReturn(Arrays.asList());

        // Act
        List<CategoryResponse> result = categoryService.getCategories(testUser);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(categoryRepository).findByUser(testUser);
    }

    @Test
    void testCreateCategory_Success() {
        // Arrange
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);

        // Act
        CategoryResponse result = categoryService.createCategory(testUser, categoryRequest);

        // Assert
        assertNotNull(result);
        assertEquals("串流影音", result.getName());
        assertEquals("play-circle", result.getIcon());
        assertEquals("#EF4444", result.getColor());
        assertEquals(1, result.getSortOrder());
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void testCreateCategory_WithDefaults() {
        // Arrange
        CategoryRequest requestWithoutDefaults = new CategoryRequest();
        requestWithoutDefaults.setName("新類別");

        Category savedCategory = new Category();
        savedCategory.setId(2L);
        savedCategory.setUser(testUser);
        savedCategory.setName("新類別");
        savedCategory.setIcon("folder");
        savedCategory.setColor("#409EFF");

        when(categoryRepository.save(any(Category.class))).thenReturn(savedCategory);

        // Act
        CategoryResponse result = categoryService.createCategory(testUser, requestWithoutDefaults);

        // Assert
        assertNotNull(result);
        assertEquals("新類別", result.getName());
        assertEquals("folder", result.getIcon());
        assertEquals("#409EFF", result.getColor());
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void testUpdateCategory_Success() {
        // Arrange
        when(categoryRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);

        CategoryRequest updateRequest = new CategoryRequest();
        updateRequest.setName("串流影音 Premium");
        updateRequest.setIcon("tv");
        updateRequest.setColor("#FF0000");
        updateRequest.setSortOrder(5);

        // Act
        CategoryResponse result = categoryService.updateCategory(testUser, 1L, updateRequest);

        // Assert
        assertNotNull(result);
        verify(categoryRepository).findByIdAndUser(1L, testUser);
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void testUpdateCategory_PartialUpdate() {
        // Arrange
        when(categoryRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);

        CategoryRequest updateRequest = new CategoryRequest();
        updateRequest.setName("串流影音更新");
        // 沒有設定 icon, color, sortOrder

        // Act
        CategoryResponse result = categoryService.updateCategory(testUser, 1L, updateRequest);

        // Assert
        assertNotNull(result);
        verify(categoryRepository).findByIdAndUser(1L, testUser);
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void testUpdateCategory_NotFound() {
        // Arrange
        when(categoryRepository.findByIdAndUser(999L, testUser)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> {
            categoryService.updateCategory(testUser, 999L, categoryRequest);
        });

        verify(categoryRepository).findByIdAndUser(999L, testUser);
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void testDeleteCategory_Success() {
        // Arrange
        when(categoryRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(testCategory));
        doNothing().when(categoryRepository).delete(testCategory);

        // Act
        categoryService.deleteCategory(testUser, 1L);

        // Assert
        verify(categoryRepository).findByIdAndUser(1L, testUser);
        verify(categoryRepository).delete(testCategory);
    }

    @Test
    void testDeleteCategory_NotFound() {
        // Arrange
        when(categoryRepository.findByIdAndUser(999L, testUser)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> {
            categoryService.deleteCategory(testUser, 999L);
        });

        verify(categoryRepository).findByIdAndUser(999L, testUser);
        verify(categoryRepository, never()).delete(any(Category.class));
    }
}
