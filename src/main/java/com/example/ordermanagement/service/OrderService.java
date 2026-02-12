package com.example.ordermanagement.service;

import com.example.ordermanagement.dto.OrderRequest;
import com.example.ordermanagement.dto.OrderResponse;
import com.example.ordermanagement.entity.Item;
import com.example.ordermanagement.entity.Order;
import com.example.ordermanagement.exception.DuplicateOrderNumberException;
import com.example.ordermanagement.exception.ResourceNotFoundException;
import com.example.ordermanagement.mapper.OrderMapper;
import com.example.ordermanagement.repository.OrderRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    public OrderService(OrderRepository orderRepository, OrderMapper orderMapper) {
        this.orderRepository = orderRepository;
        this.orderMapper = orderMapper;
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "orders")
    public List<OrderResponse> findAll() {
        return orderRepository.findAll().stream()
                .map(orderMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "order", key = "#id")
    public OrderResponse findById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
        return orderMapper.toResponse(order);
    }

    @CacheEvict(value = "orders", allEntries = true)
    public OrderResponse create(OrderRequest request) {
        if (orderRepository.existsByOrderNumber(request.getOrderNumber())) {
            throw new DuplicateOrderNumberException(
                    "Order number already exists: " + request.getOrderNumber());
        }

        // Using Builder pattern to construct the Order entity
        Order order = Order.builder()
                .orderNumber(request.getOrderNumber())
                .customerName(request.getCustomerName())
                .shippingAddress(request.getShippingAddress())
                .billingAddress(request.getBillingAddress())
                .build();

        // Using Builder pattern to construct Items and link them
        if (request.getItems() != null) {
            request.getItems().forEach(itemReq -> {
                Item item = Item.builder()
                        .sku(itemReq.getSku())
                        .name(itemReq.getName())
                        .quantity(itemReq.getQuantity())
                        .unitPrice(itemReq.getUnitPrice())
                        .imageUrl(itemReq.getImageUrl())
                        .weight(itemReq.getWeight())
                        .order(order) // Set relationship
                        .build();
                order.addItem(item);
            });
        }

        Order savedOrder = orderRepository.save(order);
        return orderMapper.toResponse(savedOrder);
    }

    @CacheEvict(value = {"order", "orders"}, key = "#id", allEntries = true)
    public OrderResponse update(Long id, OrderRequest request) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

        // Check if order number is being changed to one that already exists
        if (!order.getOrderNumber().equals(request.getOrderNumber()) &&
                orderRepository.existsByOrderNumber(request.getOrderNumber())) {
            throw new DuplicateOrderNumberException(
                    "Order number already exists: " + request.getOrderNumber());
        }

        // Manually handle items update to ensure proper JPA relationship management
        // Clear existing items
        order.getItems().clear();

        // Map new items using Builder pattern and add them
        if (request.getItems() != null) {
            request.getItems().forEach(itemReq -> {
                Item item = Item.builder()
                        .sku(itemReq.getSku())
                        .name(itemReq.getName())
                        .quantity(itemReq.getQuantity())
                        .unitPrice(itemReq.getUnitPrice())
                        .imageUrl(itemReq.getImageUrl())
                        .weight(itemReq.getWeight())
                        .order(order) // Set relationship
                        .build();
                order.addItem(item);
            });
        }

        // Update other fields using mapper (or could use setters manually)
        orderMapper.updateEntity(order, request);

        Order updatedOrder = orderRepository.save(order);
        return orderMapper.toResponse(updatedOrder);
    }

    @CacheEvict(value = {"order", "orders"}, key = "#id", allEntries = true)
    public void delete(Long id) {
        if (!orderRepository.existsById(id)) {
            throw new ResourceNotFoundException("Order not found with id: " + id);
        }
        orderRepository.deleteById(id);
    }
}