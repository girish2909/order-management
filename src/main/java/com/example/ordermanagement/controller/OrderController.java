package com.example.ordermanagement.controller;

import com.example.ordermanagement.dto.OrderRequest;
import com.example.ordermanagement.dto.OrderResponse;
import com.example.ordermanagement.dto.PagedResponse;
import com.example.ordermanagement.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "http://localhost:4200")
@Tag(name = "Order Management", description = "APIs for managing orders")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @Operation(summary = "Get all orders", description = "Retrieves a paginated list of all orders in the system")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list of orders", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PagedResponse.class)))
    @GetMapping
    public ResponseEntity<PagedResponse<OrderResponse>> getAllOrders(
            @Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field", example = "createdAt") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)", example = "desc") @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        PagedResponse<OrderResponse> orders = orderService.findAll(pageable);
        return ResponseEntity.ok(orders);
    }

    @Operation(summary = "Get order by ID", description = "Retrieves a specific order by its unique identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the order", content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrderResponse.class))),
            @ApiResponse(responseCode = "404", description = "Order not found", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(
            @Parameter(description = "ID of the order to be retrieved", required = true) @PathVariable Long id) {
        OrderResponse order = orderService.findById(id);
        return ResponseEntity.ok(order);
    }

    @Operation(summary = "Create a new order", description = "Creates a new order with the provided details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Order successfully created", content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrderResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content)
    })
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @Parameter(description = "Order details to be created", required = true) @Valid @RequestBody OrderRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {

        OrderResponse createdOrder = orderService.create(request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdOrder.getId())
                .toUri();

        return ResponseEntity.created(location).body(createdOrder);
    }

    @Operation(summary = "Update an existing order", description = "Updates the details of an existing order")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order successfully updated", content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrderResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content),
            @ApiResponse(responseCode = "404", description = "Order not found", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<OrderResponse> updateOrder(
            @Parameter(description = "ID of the order to be updated", required = true) @PathVariable Long id,
            @Parameter(description = "Updated order details", required = true) @Valid @RequestBody OrderRequest request) {
        OrderResponse updatedOrder = orderService.update(id, request);
        return ResponseEntity.ok(updatedOrder);
    }

    @Operation(summary = "Delete an order", description = "Deletes an order from the system by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Order successfully deleted", content = @Content),
            @ApiResponse(responseCode = "404", description = "Order not found", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(
            @Parameter(description = "ID of the order to be deleted", required = true) @PathVariable Long id) {
        orderService.delete(id);
        return ResponseEntity.noContent().build();
    }
}