package com.example.ordermanagement.repository;

import com.example.ordermanagement.entity.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @BeforeEach
    void setUp() {
        Order order = new Order();
        order.setOrderNumber("ORD-2025-0001");
        order.setCustomerName("Test Customer");
        orderRepository.save(order);
    }

    @Test
    void testFindByOrderNumber() {
        Optional<Order> order = orderRepository.findByOrderNumber("ORD-2025-0001");

        assertTrue(order.isPresent());
        assertEquals("ORD-2025-0001", order.get().getOrderNumber());
    }

    @Test
    void testExistsByOrderNumber() {
        boolean exists = orderRepository.existsByOrderNumber("ORD-2025-0001");

        assertTrue(exists);
    }

    @Test
    void testExistsByOrderNumberNotFound() {
        boolean exists = orderRepository.existsByOrderNumber("NON-EXISTENT");

        assertFalse(exists);
    }

    @Test
    void testSaveOrder() {
        Order order = new Order();
        order.setOrderNumber("ORD-TEST-999");
        order.setCustomerName("Test Customer");

        Order savedOrder = orderRepository.save(order);

        assertNotNull(savedOrder.getId());
        assertEquals("ORD-TEST-999", savedOrder.getOrderNumber());
        assertNotNull(savedOrder.getCreatedAt());
    }
}