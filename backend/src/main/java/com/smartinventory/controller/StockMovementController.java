package com.smartinventory.controller;

import com.smartinventory.dto.StockMovementResponse;
import com.smartinventory.exception.ProductNotFoundException;
import com.smartinventory.repository.ProductRepository;
import com.smartinventory.repository.StockMovementRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "http://localhost:5173")
public class StockMovementController {

    private final StockMovementRepository stockMovementRepository;
    private final ProductRepository productRepository;

    public StockMovementController(
            StockMovementRepository stockMovementRepository,
            ProductRepository productRepository) {

        this.stockMovementRepository = stockMovementRepository;
        this.productRepository = productRepository;
    }

    @GetMapping("/{productId}/movements")
    public List<StockMovementResponse> getStockMovements(
            @PathVariable Long productId) {

        if (!productRepository.existsById(productId)) {
            throw new ProductNotFoundException(productId);
        }

        return stockMovementRepository
                .findByProductIdOrderByCreatedAtDesc(productId)
                .stream()
                .map(movement -> new StockMovementResponse(
                        movement.getId(),
                        movement.getType(),
                        movement.getQuantity(),
                        movement.getCreatedAt()
                ))
                .toList();
    }
}