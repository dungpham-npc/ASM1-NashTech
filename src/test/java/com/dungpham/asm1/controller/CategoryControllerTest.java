package com.dungpham.asm1.controller;

import com.dungpham.asm1.common.mapper.CategoryMapper;
import com.dungpham.asm1.entity.Category;
import com.dungpham.asm1.request.CategoryRequest;
import com.dungpham.asm1.service.CategoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.lang.reflect.Field;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CategoryService categoryService;

    @MockBean
    private CategoryMapper categoryMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "ADMIN")
    void createCategory_WithValidData_ReturnsCreatedCategory() throws Exception {
        // Arrange
        CategoryRequest request = CategoryRequest.builder()
                .name("Electronics")
                .description("Electronic devices")
                .build();

        Category category = Category.builder()
                .name("Electronics")
                .description("Electronic devices")
                .build();

        Category savedCategory = Category.builder()
                .name("Electronics")
                .description("Electronic devices")
                .build();
        setId(savedCategory, 1L);

        when(categoryMapper.toEntity(any(CategoryRequest.class))).thenReturn(category);
        when(categoryService.createCategory(any(Category.class))).thenReturn(savedCategory);
        when(categoryMapper.toDetailedResponse(any(Category.class))).thenReturn(
                com.dungpham.asm1.response.CategoryResponse.builder()
                        .id(1L)
                        .name("Electronics")
                        .description("Electronic devices")
                        .build()
        );

        // Act & Assert
        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code", is("200")))
                .andExpect(jsonPath("$.message", is("Success")))
                .andExpect(jsonPath("$.data.id", is(1)))
                .andExpect(jsonPath("$.data.name", is("Electronics")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateCategory_WithValidData_ReturnsUpdatedCategory() throws Exception {
        // Arrange
        Long categoryId = 1L;
        CategoryRequest request = CategoryRequest.builder()
                .name("Updated Electronics")
                .description("Updated electronic devices")
                .build();

        Category existingCategory = Category.builder()
                .name("Electronics")
                .description("Electronic devices")
                .build();
        setId(existingCategory, categoryId);

        Category updatedCategory = Category.builder()
                .name("Updated Electronics")
                .description("Updated electronic devices")
                .build();
        setId(updatedCategory, categoryId);

        when(categoryService.getCategory(categoryId)).thenReturn(existingCategory);
        when(categoryService.updateCategory(any(Category.class), eq(categoryId))).thenReturn(updatedCategory);
        when(categoryMapper.toDetailedResponse(updatedCategory)).thenReturn(
                com.dungpham.asm1.response.CategoryResponse.builder()
                        .id(categoryId)
                        .name("Updated Electronics")
                        .description("Updated electronic devices")
                        .build()
        );

        // Act & Assert
        mockMvc.perform(put("/api/v1/categories/{id}", categoryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("200")))
                .andExpect(jsonPath("$.message", is("Success")))
                .andExpect(jsonPath("$.data.id", is(1)))
                .andExpect(jsonPath("$.data.name", is("Updated Electronics")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteCategory_WithValidId_ReturnsSuccessMessage() throws Exception {
        // Arrange
        Long categoryId = 1L;

        // Act & Assert
        mockMvc.perform(delete("/api/v1/categories/{id}", categoryId)
                        .with(csrf()))
                .andExpect(status().isNoContent())
                .andExpect(jsonPath("$.code", is("200")))
                .andExpect(jsonPath("$.message", is("Success")))
                .andExpect(jsonPath("$.data", is("Category deleted successfully")));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void getAllCategories_ReturnsAllCategories() throws Exception {
        // Arrange
        Category category = Category.builder()
                .name("Electronics")
                .description("Electronic devices")
                .build();
        setId(category, 1L);

        List<Category> categories = List.of(category);

        when(categoryService.getAllCategories()).thenReturn(categories);
        when(categoryMapper.toListResponse(any(Category.class))).thenReturn(
                com.dungpham.asm1.response.CategoryResponse.builder()
                        .id(1L)
                        .name("Electronics")
                        .build()
        );

        // Act & Assert
        mockMvc.perform(get("/api/v1/categories")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("200")))
                .andExpect(jsonPath("$.message", is("Success")))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].name", is("Electronics")));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void getCategoryById_WithValidId_ReturnsCategory() throws Exception {
        // Arrange
        Long categoryId = 1L;
        Category category = Category.builder()
                .name("Electronics")
                .description("Electronic devices")
                .build();
        setId(category, categoryId);

        when(categoryService.getCategory(categoryId)).thenReturn(category);
        when(categoryMapper.toDetailedResponse(category)).thenReturn(
                com.dungpham.asm1.response.CategoryResponse.builder()
                        .id(categoryId)
                        .name("Electronics")
                        .description("Electronic devices")
                        .build()
        );

        // Act & Assert
        mockMvc.perform(get("/api/v1/categories/{id}", categoryId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("200")))
                .andExpect(jsonPath("$.message", is("Success")))
                .andExpect(jsonPath("$.data.id", is(1)))
                .andExpect(jsonPath("$.data.name", is("Electronics")));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void createCategory_WithCustomerRole_ReturnsForbidden() throws Exception {
        // Arrange
        CategoryRequest request = CategoryRequest.builder()
                .name("Electronics")
                .description("Electronic devices")
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void updateCategory_WithCustomerRole_ReturnsForbidden() throws Exception {
        // Arrange
        Long categoryId = 1L;
        CategoryRequest request = CategoryRequest.builder()
                .name("Updated Electronics")
                .description("Updated electronic devices")
                .build();

        // Act & Assert
        mockMvc.perform(put("/api/v1/categories/{id}", categoryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void deleteCategory_WithCustomerRole_ReturnsForbidden() throws Exception {
        // Arrange
        Long categoryId = 1L;

        // Act & Assert
        mockMvc.perform(delete("/api/v1/categories/{id}", categoryId)
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    private void setId(Category category, Long id) throws Exception {
        Field idField = category.getClass().getSuperclass().getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(category, id);
    }
}