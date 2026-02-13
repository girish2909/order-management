package com.example.ordermanagement.service;

import com.example.ordermanagement.dto.ItemRequest;
import com.example.ordermanagement.dto.OrderRequest;
import com.example.ordermanagement.dto.OrderResponse;
import com.example.ordermanagement.dto.PagedResponse;
import com.example.ordermanagement.entity.Order;
import com.example.ordermanagement.exception.DuplicateOrderNumberException;
import com.example.ordermanagement.exception.ResourceNotFoundException;
import com.example.ordermanagement.mapper.OrderMapper;
import com.example.ordermanagement.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderService orderService;

    private Order order;
    private OrderRequest orderRequest;
    private OrderResponse orderResponse;

    @BeforeEach
    void setUp() {
        order = Order.builder()
                .id(1L)
                .orderNumber("ORD-2025-0001")
                .customerName("Test Customer")
                .build();

        ItemRequest itemRequest = ItemRequest.builder()
                .sku("SKU-001")
                .name("Test Item")
                .quantity(2)
                .unitPrice(BigDecimal.valueOf(100))
                .build();

        orderRequest = OrderRequest.builder()
                .orderNumber("ORD-2025-0001")
                .customerName("Test Customer")
                .items(Collections.singletonList(itemRequest))
                .build();

        orderResponse = OrderResponse.builder()
                .id(1L)
                .orderNumber("ORD-2025-0001")
                .build();
    }

    @Test
    void testFindAll() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> page = new PageImpl<>(Collections.singletonList(order));

        when(orderRepository.findAll(pageable)).thenReturn(page);
        when(orderMapper.toResponse(any(Order.class))).thenReturn(orderResponse);

        PagedResponse<OrderResponse> result = orderService.findAll(pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(1, result.getTotalElements());
        verify(orderRepository, times(1)).findAll(pageable);
    }

    @Test
    void testFindByIdSuccess() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderMapper.toResponse(order)).thenReturn(orderResponse);

        OrderResponse result = orderService.findById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(orderRepository, times(1)).findById(1L);
    }

    @Test
    void testFindByIdNotFound() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> orderService.findById(1L));
    }

    @Test
    void testCreateSuccess() {
        when(orderRepository.existsByOrderNumber(anyString())).thenReturn(false);
        
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderMapper.toResponse(any(Order.class))).thenReturn(orderResponse);

        OrderResponse result = orderService.create(orderRequest);

        assertNotNull(result);
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void testCreateDuplicateOrderNumber() {
        when(orderRepository.existsByOrderNumber(anyString())).thenReturn(true);

        assertThrows(DuplicateOrderNumberException.class, () -> orderService.create(orderRequest));
    }

    @Test
    void testUpdateSuccess() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
      //  when(orderRepository.existsByOrderNumber(anyString())).thenReturn(false);
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderMapper.toResponse(any(Order.class))).thenReturn(orderResponse);

        OrderResponse result = orderService.update(1L, orderRequest);

        assertNotNull(result);
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void testUpdateNotFound() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> orderService.update(1L, orderRequest));
    }

    @Test
    void testDeleteSuccess() {
        when(orderRepository.existsById(1L)).thenReturn(true);

        orderService.delete(1L);

        verify(orderRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteNotFound() {
        when(orderRepository.existsById(1L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> orderService.delete(1L));
    }
}