package com.dungpham.asm1.controller;

import com.dungpham.asm1.entity.Product;
import com.dungpham.asm1.facade.ProductFacade;
import com.dungpham.asm1.request.ProductRequest;
import com.dungpham.asm1.response.BaseResponse;
import com.dungpham.asm1.response.ProductResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductFacade productFacade;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getFeaturedProducts_Success_ReturnsCorrectResponse() throws Exception {
        Page<ProductResponse> productPage = new PageImpl<>(
                List.of(createProductResponse(1L, "iPhone", BigDecimal.valueOf(999.99))),
                Pageable.ofSize(10),
                1
        );

        when(productFacade.getAllProducts(any(), any()))
                .thenReturn(BaseResponse.build(productPage, true));

        mockMvc.perform(get("/api/v1/products")
                        .param("productName", "iPhone")
                        .param("minPrice", "800")
                        .param("maxPrice", "1200")
                        .param("categoryId", "1")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "price,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("200")))
                .andExpect(jsonPath("$.message", is("Success")))
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].name", is("iPhone")));

        ArgumentCaptor<Specification<Product>> specCaptor = ArgumentCaptor.forClass(Specification.class);
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(productFacade).getAllProducts(specCaptor.capture(), pageableCaptor.capture());

        Pageable pageable = pageableCaptor.getValue();
        assertEquals(0, pageable.getPageNumber());
        assertEquals(10, pageable.getPageSize());
        assertTrue(pageable.getSort().getOrderFor("price").getDirection().isDescending());
    }

    @Test
    void getProductById_ValidId_ReturnsCorrectProduct() throws Exception {
        ProductResponse product = createProductResponse(1L, "iPhone", BigDecimal.valueOf(999.99));

        when(productFacade.getProductDetails(1L)).thenReturn(BaseResponse.build(product, true));

        mockMvc.perform(get("/api/v1/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("200")))
                .andExpect(jsonPath("$.message", is("Success")))
                .andExpect(jsonPath("$.data.id", is(1)))
                .andExpect(jsonPath("$.data.name", is("iPhone")));
    }

    @Test
    @WithMockUser(roles = "ADMIN") // Assuming ADMIN role is required
    void createProduct_Successfully() throws Exception {
        ProductRequest request = ProductRequest.builder()
                .name("New Product")
                .description("Product Description")
                .price(BigDecimal.valueOf(199.99))
                .categoryId(1L)
                .build();

        ProductResponse response = createProductResponse(1L, "New Product", BigDecimal.valueOf(199.99));

        MockMultipartFile requestPart = new MockMultipartFile(
                "request",
                "",
                "application/json",
                objectMapper.writeValueAsBytes(request));

        MockMultipartFile imagePart = new MockMultipartFile(
                "productImages",
                "image.jpg",
                "image/jpeg",
                "test image content".getBytes());

        when(productFacade.createProduct(any(), anyList()))
                .thenReturn(BaseResponse.build(response, true));

        mockMvc.perform(multipart("/api/v1/products")
                        .file(imagePart)
                        .file(requestPart)
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code", is("200")))
                .andExpect(jsonPath("$.message", is("Success")))
                .andExpect(jsonPath("$.data.name", is("New Product")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateProduct_Successfully() throws Exception {
        // Arrange
        Long productId = 1L;
        ProductRequest request = ProductRequest.builder()
                .name("Updated Product")
                .description("Updated Description")
                .price(BigDecimal.valueOf(299.99))
                .categoryId(1L)
                .build();

        ProductResponse response = createProductResponse(1L, "Updated Product", BigDecimal.valueOf(299.99));

        MockMultipartFile requestPart = new MockMultipartFile(
                "request",
                "",
                "application/json",
                objectMapper.writeValueAsBytes(request));

        MockMultipartFile imagePart = new MockMultipartFile(
                "productImages",
                "image.jpg",
                "image/jpeg",
                "test image content".getBytes());

        when(productFacade.updateProduct(any(), eq(productId), anyList()))
                .thenReturn(BaseResponse.build(response, true));

        // Act & Assert
        mockMvc.perform(multipart(HttpMethod.PUT, "/api/v1/products/{id}", productId)
                        .file(imagePart)
                        .file(requestPart)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("200")))
                .andExpect(jsonPath("$.message", is("Success")))
                .andExpect(jsonPath("$.data.name", is("Updated Product")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateProduct_WithInvalidRequest_ReturnsBadRequest() throws Exception {
        // Arrange
        Long productId = 1L;
        ProductRequest request = ProductRequest.builder()
                .name("")  // Invalid: empty name
                .description("Updated Description")
                .price(BigDecimal.valueOf(-50.00))  // Invalid: negative price
                .categoryId(1L)
                .build();

        MockMultipartFile requestPart = new MockMultipartFile(
                "request",
                "",
                "application/json",
                objectMapper.writeValueAsBytes(request));

        MockMultipartFile imagePart = new MockMultipartFile(
                "productImages",
                "image.jpg",
                "image/jpeg",
                "test image content".getBytes());

        // Act & Assert
        mockMvc.perform(multipart(HttpMethod.PUT, "/api/v1/products/{id}", productId)
                        .file(imagePart)
                        .file(requestPart)
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN") // Assuming ADMIN role is required
    void removeProduct_Successfully() throws Exception {
        when(productFacade.removeProduct(1L))
                .thenReturn(BaseResponse.build("Product deleted successfully", true));

        mockMvc.perform(delete("/api/v1/products/1")
                        .with(csrf()))
                .andExpect(status().isNoContent())
                .andExpect(jsonPath("$.code", is("200")))
                .andExpect(jsonPath("$.message", is("Success")))
                .andExpect(jsonPath("$.data", is("Product deleted successfully")));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void rateProduct_Successfully() throws Exception {
        when(productFacade.rateProduct(eq(1L), eq(5)))
                .thenReturn(BaseResponse.build("Rating successful", true));

        mockMvc.perform(post("/api/v1/products/1/rate")
                        .param("rating", "5")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("200")))
                .andExpect(jsonPath("$.message", is("Success")))
                .andExpect(jsonPath("$.data", is("Rating successful")));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void rateProduct_InvalidRating_ReturnsBadRequest() throws Exception {
        when(productFacade.rateProduct(eq(1L), eq(6)))
                .thenThrow(new IllegalArgumentException("Rating must be between 1 and 5"));

        mockMvc.perform(post("/api/v1/products/1/rate")
                        .param("rating", "6")
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void createProduct_WithCustomerRole_ReturnsForbidden() throws Exception {
        ProductRequest request = ProductRequest.builder()
                .name("New Product")
                .description("Product Description")
                .price(BigDecimal.valueOf(199.99))
                .categoryId(1L)
                .build();

        MockMultipartFile requestPart = new MockMultipartFile(
                "request",
                "",
                "application/json",
                objectMapper.writeValueAsBytes(request));

        MockMultipartFile imagePart = new MockMultipartFile(
                "productImages",
                "image.jpg",
                "image/jpeg",
                "test image content".getBytes());

        mockMvc.perform(multipart("/api/v1/products")
                        .file(imagePart)
                        .file(requestPart)
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void updateProduct_WithCustomerRole_ReturnsForbidden() throws Exception {
        ProductRequest request = ProductRequest.builder()
                .name("Updated Product")
                .description("Updated Description")
                .price(BigDecimal.valueOf(299.99))
                .categoryId(1L)
                .build();

        mockMvc.perform(put("/api/v1/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void removeProduct_WithCustomerRole_ReturnsForbidden() throws Exception {
        mockMvc.perform(delete("/api/v1/products/1")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void createProduct_WithoutLogin_ReturnsUnauthorized() throws Exception {
        ProductRequest request = ProductRequest.builder()
                .name("New Product")
                .description("Product Description")
                .price(BigDecimal.valueOf(199.99))
                .categoryId(1L)
                .build();

        MockMultipartFile requestPart = new MockMultipartFile(
                "request",
                "",
                "application/json",
                objectMapper.writeValueAsBytes(request));

        MockMultipartFile imagePart = new MockMultipartFile(
                "productImages",
                "image.jpg",
                "image/jpeg",
                "test image content".getBytes());

        mockMvc.perform(multipart("/api/v1/products")
                        .file(imagePart)
                        .file(requestPart)
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateProduct_WithoutLogin_ReturnsUnauthorized() throws Exception {
        ProductRequest request = ProductRequest.builder()
                .name("Updated Product")
                .description("Updated Description")
                .price(BigDecimal.valueOf(299.99))
                .categoryId(1L)
                .build();

        mockMvc.perform(put("/api/v1/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deleteProduct_WithoutLogin_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(delete("/api/v1/products/1")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rateProduct_WithoutLogin_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/products/1/rate")
                        .param("rating", "5")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getAllProducts_WithFilterParameters_CallsFacadeWithCorrectSpecification() throws Exception {
        Page<ProductResponse> productPage = new PageImpl<>(
                List.of(createProductResponse(1L, "iPhone", BigDecimal.valueOf(999.99))),
                Pageable.ofSize(10),
                1
        );

        when(productFacade.getAllProducts(any(), any()))
                .thenReturn(BaseResponse.build(productPage, true));

        mockMvc.perform(get("/api/v1/products")
                        .param("productName", "Phone")
                        .param("minPrice", "500")
                        .param("maxPrice", "1500")
                        .param("categoryId", "2"))
                .andExpect(status().isOk());

        ArgumentCaptor<Specification<Product>> specCaptor = ArgumentCaptor.forClass(Specification.class);
        verify(productFacade).getAllProducts(specCaptor.capture(), any());
    }

    @Test
    void getAllProducts_WithDefaultPagination_UsesCorrectDefaults() throws Exception {
        Page<ProductResponse> productPage = new PageImpl<>(
                List.of(createProductResponse(1L, "iPhone", BigDecimal.valueOf(999.99))),
                Pageable.ofSize(10),
                1
        );

        when(productFacade.getAllProducts(any(), any()))
                .thenReturn(BaseResponse.build(productPage, true));

        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(productFacade).getAllProducts(any(), pageableCaptor.capture());

        Pageable pageable = pageableCaptor.getValue();
        assertEquals(0, pageable.getPageNumber());
        assertEquals(10, pageable.getPageSize());
        assertTrue(pageable.getSort().getOrderFor("id").getDirection().isAscending());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createProduct_WithInvalidRequest_ReturnsBadRequest() throws Exception {
        ProductRequest request = ProductRequest.builder()
                .description("Product Description")
                .price(BigDecimal.valueOf(-199.99))  // Invalid price
                .build();

        MockMultipartFile requestPart = new MockMultipartFile(
                "request",
                "",
                "application/json",
                objectMapper.writeValueAsBytes(request));

        MockMultipartFile imagePart = new MockMultipartFile(
                "productImages",
                "image.jpg",
                "image/jpeg",
                "test image content".getBytes());

        mockMvc.perform(multipart("/api/v1/products")
                        .file(imagePart)
                        .file(requestPart)
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    private ProductResponse createProductResponse(Long id, String name, BigDecimal price) {
        ProductResponse response = new ProductResponse();
        response.setId(id);
        response.setName(name);
        response.setPrice(price);
        return response;
    }
}