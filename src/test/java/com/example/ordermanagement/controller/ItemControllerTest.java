package com.example.ordermanagement.controller;

import com.example.ordermanagement.dto.ItemRequest;
import com.example.ordermanagement.dto.ItemResponse;
import com.example.ordermanagement.exception.ResourceNotFoundException;
import com.example.ordermanagement.service.CustomUserDetailsService;
import com.example.ordermanagement.service.ItemService;
import com.example.ordermanagement.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
@AutoConfigureMockMvc(addFilters = false) // Keeping filters disabled for unit testing controller logic in isolation
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ItemService itemService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @WithMockUser
    void testCreateItem() throws Exception {
        ItemRequest request = ItemRequest.builder()
                .sku("SKU-NEW")
                .name("New Item")
                .quantity(5)
                .unitPrice(BigDecimal.valueOf(20.00))
                .build();

        ItemResponse response = ItemResponse.builder()
                .id(10L)
                .sku("SKU-NEW")
                .name("New Item")
                .quantity(5)
                .unitPrice(BigDecimal.valueOf(20.00))
                .build();

        when(itemService.create(eq(1L), any(ItemRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/items/order/1")
                .header("Authorization", "Bearer mock-jwt-token")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.sku").value("SKU-NEW"));
    }

    @Test
    @WithMockUser
    void testCreateItemOrderNotFound() throws Exception {
        ItemRequest request = ItemRequest.builder()
                .sku("SKU-NEW")
                .name("New Item")
                .quantity(5)
                .unitPrice(BigDecimal.valueOf(20.00))
                .build();

        when(itemService.create(eq(999L), any(ItemRequest.class)))
                .thenThrow(new ResourceNotFoundException("Order not found with id: 999"));

        mockMvc.perform(post("/api/items/order/999")
                .header("Authorization", "Bearer mock-jwt-token")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void testUpdateItem() throws Exception {
        ItemRequest request = ItemRequest.builder()
                .sku("SKU-UPD")
                .name("Updated Item")
                .quantity(10)
                .unitPrice(BigDecimal.valueOf(25.00))
                .build();

        ItemResponse response = ItemResponse.builder()
                .id(10L)
                .sku("SKU-UPD")
                .name("Updated Item")
                .quantity(10)
                .unitPrice(BigDecimal.valueOf(25.00))
                .build();

        when(itemService.update(eq(10L), any(ItemRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/items/10")
                .header("Authorization", "Bearer mock-jwt-token")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Item"));
    }

    @Test
    @WithMockUser
    void testDeleteItem() throws Exception {
        doNothing().when(itemService).delete(10L);

        mockMvc.perform(delete("/api/items/10")
                .header("Authorization", "Bearer mock-jwt-token")
                .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    void testDeleteItemNotFound() throws Exception {
        org.mockito.Mockito.doThrow(new ResourceNotFoundException("Item not found with id: 999"))
                .when(itemService).delete(999L);

        mockMvc.perform(delete("/api/items/999")
                .header("Authorization", "Bearer mock-jwt-token")
                .with(csrf()))
                .andExpect(status().isNotFound());
    }
}