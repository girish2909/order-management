package com.example.ordermanagement.mapper;

import com.example.ordermanagement.dto.ItemRequest;
import com.example.ordermanagement.dto.ItemResponse;
import com.example.ordermanagement.dto.OrderRequest;
import com.example.ordermanagement.dto.OrderResponse;
import com.example.ordermanagement.entity.Item;
import com.example.ordermanagement.entity.Order;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderMapper {

    @Mapping(target = "items", source = "items")
    OrderResponse toResponse(Order order);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "totalAmount", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "paymentStatus", ignore = true)
    @Mapping(target = "trackingNumber", ignore = true)
    @Mapping(target = "items", source = "items")
    Order toEntity(OrderRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "totalAmount", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "paymentStatus", ignore = true)
    @Mapping(target = "trackingNumber", ignore = true)
    @Mapping(target = "items", ignore = true) // Handled manually in service or via specific update logic usually
    void updateEntity(@MappingTarget Order order, OrderRequest request);

    // Item Mappings
    ItemResponse itemToResponse(Item item);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", ignore = true)
    Item itemRequestToEntity(ItemRequest request);

    @AfterMapping
    default void linkItems(@MappingTarget Order order) {
        if (order.getItems() != null) {
            order.getItems().forEach(item -> item.setOrder(order));
        }
    }
}