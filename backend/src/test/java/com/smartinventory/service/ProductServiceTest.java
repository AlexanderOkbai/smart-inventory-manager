package com.smartinventory.service;

import com.smartinventory.dto.StockRequest;
import com.smartinventory.entity.MovementType;
import com.smartinventory.entity.Product;
import com.smartinventory.entity.StockMovement;
import com.smartinventory.exception.ProductNotFoundException;
import com.smartinventory.repository.ProductRepository;
import com.smartinventory.repository.StockMovementRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private StockMovementRepository stockMovementRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void receiveStock_shouldIncreaseQuantityAndSaveMovement() {
        Product product = new Product();
        product.setId(2L);
        product.setQuantity(10);

        StockRequest request = new StockRequest();
        request.setQuantity(5);

        when(productRepository.findById(2L))
                .thenReturn(Optional.of(product));

        when(productRepository.save(product))
                .thenReturn(product);

        Product result = productService.receiveStock(2L, request);

        assertEquals(15, result.getQuantity());

        verify(productRepository).save(product);
        verify(stockMovementRepository).save(any(StockMovement.class));
    }

    @Test
    void issueStock_shouldDecreaseQuantityAndSaveMovement() {
        Product product = new Product();
        product.setId(2L);
        product.setQuantity(10);

        StockRequest request = new StockRequest();
        request.setQuantity(3);

        when(productRepository.findById(2L))
                .thenReturn(Optional.of(product));

        when(productRepository.save(product))
                .thenReturn(product);

        Product result = productService.issueStock(2L, request);

        assertEquals(7, result.getQuantity());

        verify(productRepository).save(product);
        verify(stockMovementRepository).save(any(StockMovement.class));
    }

    @Test
    void issueStock_shouldRejectInsufficientStock() {
        Product product = new Product();
        product.setId(2L);
        product.setQuantity(4);

        StockRequest request = new StockRequest();
        request.setQuantity(10);

        when(productRepository.findById(2L))
                .thenReturn(Optional.of(product));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> productService.issueStock(2L, request)
        );

        assertEquals(
                "Insufficient stock. Available: 4, requested: 10",
                exception.getMessage()
        );

        verify(productRepository, never()).save(any(Product.class));
        verify(stockMovementRepository, never()).save(any(StockMovement.class));
    }

    @Test
    void receiveStock_shouldRejectZeroQuantity() {
        Product product = new Product();
        product.setId(2L);
        product.setQuantity(10);

        StockRequest request = new StockRequest();
        request.setQuantity(0);

        when(productRepository.findById(2L))
                .thenReturn(Optional.of(product));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> productService.receiveStock(2L, request)
        );

        assertEquals(
                "Stock quantity must be greater than zero",
                exception.getMessage()
        );

        verify(productRepository, never()).save(any(Product.class));
        verify(stockMovementRepository, never()).save(any(StockMovement.class));
    }

    @Test
    void issueStock_shouldRejectZeroQuantity() {
        Product product = new Product();
        product.setId(2L);
        product.setQuantity(10);

        StockRequest request = new StockRequest();
        request.setQuantity(0);

        when(productRepository.findById(2L))
                .thenReturn(Optional.of(product));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> productService.issueStock(2L, request)
        );

        assertEquals(
                "Issue quantity must be greater than zero",
                exception.getMessage()
        );

        verify(productRepository, never()).save(any(Product.class));
        verify(stockMovementRepository, never()).save(any(StockMovement.class));
    }

    @Test
    void getProductById_shouldThrowExceptionWhenProductDoesNotExist() {
        when(productRepository.findById(999L))
                .thenReturn(Optional.empty());

        ProductNotFoundException exception = assertThrows(
                ProductNotFoundException.class,
                () -> productService.getProductById(999L)
        );

        assertEquals(
                "Product not found with id: 999",
                exception.getMessage()
        );
    }
@Test
void receiveStock_shouldRejectNegativeQuantity() {
    Product product = new Product();
    product.setId(2L);
    product.setQuantity(10);

    StockRequest request = new StockRequest();
    request.setQuantity(-5);

    when(productRepository.findById(2L))
            .thenReturn(Optional.of(product));

    IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> productService.receiveStock(2L, request)
    );

    assertEquals(
            "Stock quantity must be greater than zero",
            exception.getMessage()
    );

    verify(productRepository, never()).save(any(Product.class));
    verify(stockMovementRepository, never()).save(any(StockMovement.class));
}

@Test
void issueStock_shouldRejectNegativeQuantity() {
    Product product = new Product();
    product.setId(2L);
    product.setQuantity(10);

    StockRequest request = new StockRequest();
    request.setQuantity(-5);

    when(productRepository.findById(2L))
            .thenReturn(Optional.of(product));

    IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> productService.issueStock(2L, request)
    );

    assertEquals(
            "Issue quantity must be greater than zero",
            exception.getMessage()
    );

    verify(productRepository, never()).save(any(Product.class));
    verify(stockMovementRepository, never()).save(any(StockMovement.class));
}
}