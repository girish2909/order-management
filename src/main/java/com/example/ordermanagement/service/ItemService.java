package com.example.ordermanagement.service;

import com.example.ordermanagement.dto.ItemRequest;
import com.example.ordermanagement.dto.ItemResponse;
import com.example.ordermanagement.entity.Item;
import com.example.ordermanagement.entity.Order;
import com.example.ordermanagement.exception.ResourceNotFoundException;
import com.example.ordermanagement.mapper.OrderMapper;
import com.example.ordermanagement.repository.ItemRepository;
import com.example.ordermanagement.repository.OrderRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ItemService {

    private final ItemRepository itemRepository;
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    public ItemService(ItemRepository itemRepository, OrderRepository orderRepository, OrderMapper orderMapper) {
        this.itemRepository = itemRepository;
        this.orderRepository = orderRepository;
        this.orderMapper = orderMapper;
    }

    @CacheEvict(value = {"order", "orders"}, allEntries = true)
    public ItemResponse create(Long orderId, ItemRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        Item item = Item.builder()
                .sku(request.getSku())
                .name(request.getName())
                .quantity(request.getQuantity())
                .unitPrice(request.getUnitPrice())
                .imageUrl(request.getImageUrl())
                .weight(request.getWeight())
                .order(order)
                .build();

        Item savedItem = itemRepository.save(item);
        return orderMapper.itemToResponse(savedItem);
    }

    @CacheEvict(value = {"order", "orders"}, allEntries = true)
    public ItemResponse update(Long id, ItemRequest request) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with id: " + id));

        item.setSku(request.getSku());
        item.setName(request.getName());
        item.setQuantity(request.getQuantity());
        item.setUnitPrice(request.getUnitPrice());
        item.setImageUrl(request.getImageUrl());
        item.setWeight(request.getWeight());

        Item updatedItem = itemRepository.save(item);
        return orderMapper.itemToResponse(updatedItem);
    }

    @CacheEvict(value = {"order", "orders"}, allEntries = true)
    public void delete(Long id) {
        if (!itemRepository.existsById(id)) {
            throw new ResourceNotFoundException("Item not found with id: " + id);
        }
        itemRepository.deleteById(id);
    }
}