package com.smartinventory.controller;

import com.smartinventory.entity.MovementType;
import com.smartinventory.entity.Product;
import com.smartinventory.entity.StockMovement;
import com.smartinventory.repository.ProductRepository;
import com.smartinventory.repository.StockMovementRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StockMovementController.class)
class StockMovementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StockMovementRepository stockMovementRepository;

    @MockitoBean
    private ProductRepository productRepository;

    @Test
    void getStockMovements_shouldReturnMovements() throws Exception {

        when(productRepository.existsById(2L))
                .thenReturn(true);

        Product product = new Product();
        product.setId(2L);
        product.setName("Dell Latitude 5440");

        StockMovement movement = new StockMovement();
        movement.setProduct(product);
        movement.setType(MovementType.RECEIVE);
        movement.setQuantity(10);
        movement.setCreatedAt(
                LocalDateTime.of(2026, 8, 26, 5, 52, 14)
        );

        when(stockMovementRepository
                .findByProductIdOrderByCreatedAtDesc(2L))
                .thenReturn(List.of(movement));

        mockMvc.perform(get("/api/products/2/stock/movements"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("RECEIVE"))
                .andExpect(jsonPath("$[0].quantity").value(10))
                .andExpect(jsonPath("$[0].createdAt")
                        .value("2026-08-26T05:52:14"));
    }

    @Test
    void getStockMovements_shouldReturnNotFoundForMissingProduct()
            throws Exception {

        when(productRepository.existsById(999L))
                .thenReturn(false);

        mockMvc.perform(get("/api/products/999/stock/movements"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message")
                        .value("Product not found with id: 999"));
    }
}