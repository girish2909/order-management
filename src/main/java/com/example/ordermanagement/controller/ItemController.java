package com.example.ordermanagement.controller;

import com.example.ordermanagement.dto.ItemRequest;
import com.example.ordermanagement.dto.ItemResponse;
import com.example.ordermanagement.service.ItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/items")
@CrossOrigin(origins = "http://localhost:4200")
@Tag(name = "Item Management", description = "APIs for managing items within orders")
@SecurityRequirement(name = "bearerAuth")
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @Operation(summary = "Add item to order", description = "Creates a new item and adds it to an existing order")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Item successfully created", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ItemResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content),
            @ApiResponse(responseCode = "404", description = "Order not found", content = @Content)
    })
    @PostMapping("/order/{orderId}")
    public ResponseEntity<ItemResponse> createItem(
            @Parameter(description = "ID of the order to add the item to", required = true) @PathVariable Long orderId,
            @Parameter(description = "Item details to be created", required = true) @Valid @RequestBody ItemRequest request) {
        ItemResponse createdItem = itemService.create(orderId, request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdItem.getId())
                .toUri();

        return ResponseEntity.created(location).body(createdItem);
    }

    @Operation(summary = "Update an existing item", description = "Updates the details of an existing item")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Item successfully updated", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ItemResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content),
            @ApiResponse(responseCode = "404", description = "Item not found", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<ItemResponse> updateItem(
            @Parameter(description = "ID of the item to be updated", required = true) @PathVariable Long id,
            @Parameter(description = "Updated item details", required = true) @Valid @RequestBody ItemRequest request) {
        ItemResponse updatedItem = itemService.update(id, request);
        return ResponseEntity.ok(updatedItem);
    }

    @Operation(summary = "Delete an item", description = "Deletes an item from the system by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Item successfully deleted", content = @Content),
            @ApiResponse(responseCode = "404", description = "Item not found", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(
            @Parameter(description = "ID of the item to be deleted", required = true) @PathVariable Long id) {
        itemService.delete(id);
        return ResponseEntity.noContent().build();
    }
}