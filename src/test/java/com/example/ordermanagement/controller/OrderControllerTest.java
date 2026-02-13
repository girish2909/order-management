package com.example.ordermanagement.controller;

import com.example.ordermanagement.dto.ItemRequest;
import com.example.ordermanagement.dto.OrderRequest;
import com.example.ordermanagement.dto.OrderResponse;
import com.example.ordermanagement.dto.PagedResponse;
import com.example.ordermanagement.exception.ResourceNotFoundException;
import com.example.ordermanagement.service.CustomUserDetailsService;
import com.example.ordermanagement.service.JwtService;
import com.example.ordermanagement.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
@AutoConfigureMockMvc(addFilters = false) // Keeping filters disabled for unit testing controller logic in isolation
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void testGetAllOrders() throws Exception {
        PagedResponse<OrderResponse> pagedResponse = PagedResponse.<OrderResponse>builder()
                .content(Collections.emptyList())
                .pageNumber(0)
                .pageSize(10)
                .totalElements(0)
                .totalPages(0)
                .last(true)
                .build();

        when(orderService.findAll(any(Pageable.class))).thenReturn(pagedResponse);

        mockMvc.perform(get("/api/orders")
                .header("Authorization", "Bearer mock-jwt-token"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void testGetOrderById() throws Exception {
        OrderResponse response = new OrderResponse();
        response.setId(1L);
        response.setOrderNumber("ORD-123");

        when(orderService.findById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/orders/1")
                .header("Authorization", "Bearer mock-jwt-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.orderNumber").value("ORD-123"));
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void testGetOrderByIdNotFound() throws Exception {
        when(orderService.findById(999L)).thenThrow(new ResourceNotFoundException("Order not found"));

        mockMvc.perform(get("/api/orders/999")
                        .header("Authorization", "Bearer mock-jwt-token"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void testCreateOrder() throws Exception {
        ItemRequest item = ItemRequest.builder().sku("SKU-TEST").name("Test Product").quantity(2)
                .unitPrice(BigDecimal.valueOf(50.00)).imageUrl("testUrl").build();
        OrderRequest request = OrderRequest.builder().orderNumber("ORD-TEST-001").customerName("Test Customer")
                .items(Collections.singletonList(item)).build();

        OrderResponse response = new OrderResponse();
        response.setId(1L);
        response.setOrderNumber("ORD-TEST-001");
        response.setCustomerName("Test Customer");
        response.setCreatedAt(LocalDateTime.now());
        response.setTotalAmount(BigDecimal.valueOf(100.00));

        when(orderService.create(any(OrderRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/orders")
                .header("Authorization", "Bearer mock-jwt-token")
                .with(csrf()) // Add CSRF token
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.orderNumber").value("ORD-TEST-001"))
                .andExpect(jsonPath("$.customerName").value("Test Customer"));
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void testCreateOrderValidationFailure() throws Exception {
        OrderRequest request = OrderRequest.builder().orderNumber("").customerName("").items(Collections.emptyList())
                .build();

        mockMvc.perform(post("/api/orders")
                .header("Authorization", "Bearer mock-jwt-token")
                .with(csrf()) // Add CSRF token
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors").exists());
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void testUpdateOrder() throws Exception {
        ItemRequest item = ItemRequest.builder().sku("SKU-TEST").name("Updated Product").quantity(3)
                .unitPrice(BigDecimal.valueOf(75.00)).imageUrl("testUrl").build();
        OrderRequest request = OrderRequest.builder().orderNumber("ORD-2025-0001").customerName("Updated Customer")
                .items(Collections.singletonList(item)).build();

        OrderResponse response = new OrderResponse();
        response.setId(1L);
        response.setCustomerName("Updated Customer");

        when(orderService.update(eq(1L), any(OrderRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/orders/1")
                .header("Authorization", "Bearer mock-jwt-token")
                .with(csrf()) // Add CSRF token
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerName").value("Updated Customer"));
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void testDeleteOrder() throws Exception {
        doNothing().when(orderService).delete(1L);

        mockMvc.perform(delete("/api/orders/1")
                .header("Authorization", "Bearer mock-jwt-token")
                .with(csrf())) // Add CSRF token
                .andExpect(status().isNoContent());
    }
}