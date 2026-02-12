package com.example.ordermanagement.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderRequest {

    @NotBlank(message = "Order number is required")
    private String orderNumber;

    @NotBlank(message = "Customer name is required")
    private String customerName;

    private String shippingAddress;
    private String billingAddress;

    @NotEmpty(message = "Order must have at least one item")
    @Valid
    @Builder.Default
    private List<ItemRequest> items = new ArrayList<>();
}