package com.smartinventory.service;

import java.util.List;

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
import static org.mockito.ArgumentMatchers.any;
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
@Test
void issueStock_shouldAllowIssuingEntireAvailableStock() {
    Product product = new Product();
    product.setId(2L);
    product.setQuantity(10);

    StockRequest request = new StockRequest();
    request.setQuantity(10);

    when(productRepository.findById(2L))
            .thenReturn(Optional.of(product));

    when(productRepository.save(product))
            .thenReturn(product);

    Product result = productService.issueStock(2L, request);

    assertEquals(0, result.getQuantity());

    verify(productRepository).save(product);
    verify(stockMovementRepository).save(any(StockMovement.class));
}

@Test
void createProduct_shouldRejectDuplicateSku() {
    Product product = new Product();
    product.setName("Dell Latitude 5440");
    product.setSku("LAP-10001");

    when(productRepository.existsBySku("LAP-10001"))
            .thenReturn(true);

    IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> productService.createProduct(product)
    );

    assertEquals(
            "Product with SKU already exists: LAP-10001",
            exception.getMessage()
    );

    verify(productRepository, never()).save(any(Product.class));
}

@Test
void updateProduct_shouldRejectDuplicateSku() {
    Product existingProduct = new Product();
    existingProduct.setId(2L);
    existingProduct.setSku("LAP-10001");

    Product updatedProduct = new Product();
    updatedProduct.setSku("LAP-10002");

    when(productRepository.findById(2L))
            .thenReturn(Optional.of(existingProduct));

    when(productRepository.existsBySkuAndIdNot("LAP-10002", 2L))
            .thenReturn(true);

    IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> productService.updateProduct(2L, updatedProduct)
    );

    assertEquals(
            "Product with SKU already exists: LAP-10002",
            exception.getMessage()
    );

    verify(productRepository, never()).save(any(Product.class));
}
@Test
void createProduct_shouldSaveProductWhenSkuIsUnique() {
    Product product = new Product();
    product.setName("Dell Latitude 5440");
    product.setSku("LAP-10001");
    product.setQuantity(10);

    when(productRepository.existsBySku("LAP-10001"))
            .thenReturn(false);

    when(productRepository.save(product))
            .thenReturn(product);

    Product result = productService.createProduct(product);

    assertSame(product, result);

    verify(productRepository).existsBySku("LAP-10001");
    verify(productRepository).save(product);
}
@Test
void updateProduct_shouldUpdateAndSaveProduct() {
    Product existingProduct = new Product();
    existingProduct.setId(2L);
    existingProduct.setName("Old Product");
    existingProduct.setSku("LAP-10001");
    existingProduct.setQuantity(10);

    Product updatedProduct = new Product();
    updatedProduct.setName("Dell Latitude 5440");
    updatedProduct.setSku("LAP-10002");
    updatedProduct.setQuantity(20);

    when(productRepository.findById(2L))
            .thenReturn(Optional.of(existingProduct));

    when(productRepository.existsBySkuAndIdNot("LAP-10002", 2L))
            .thenReturn(false);

    when(productRepository.save(existingProduct))
            .thenReturn(existingProduct);

    Product result = productService.updateProduct(2L, updatedProduct);

    assertEquals("Dell Latitude 5440", result.getName());
    assertEquals("LAP-10002", result.getSku());
    assertEquals(20, result.getQuantity());

    verify(productRepository).findById(2L);
    verify(productRepository)
            .existsBySkuAndIdNot("LAP-10002", 2L);
    verify(productRepository).save(existingProduct);
}
@Test
void getAllProducts_shouldReturnAllProducts() {
    Product product1 = new Product();
    product1.setId(1L);
    product1.setName("Dell Latitude 5440");

    Product product2 = new Product();
    product2.setId(2L);
    product2.setName("HP EliteBook");

    when(productRepository.findAll())
            .thenReturn(java.util.List.of(product1, product2));

    var result = productService.getAllProducts();

    assertEquals(2, result.size());
    assertEquals("Dell Latitude 5440", result.get(0).getName());
    assertEquals("HP EliteBook", result.get(1).getName());

    verify(productRepository).findAll();
}
@Test
void searchProducts_shouldReturnMatchingProducts() {

    Product product = new Product();
    product.setId(2L);
    product.setName("Dell Latitude 5440");
    product.setSku("LAP-10001");
    product.setQuantity(18);

    when(productRepository
            .findByNameContainingIgnoreCaseOrSkuContainingIgnoreCase(
                    "Dell",
                    "Dell"
            ))
            .thenReturn(List.of(product));

    List<Product> result = productService.searchProducts("Dell");

    assertEquals(1, result.size());
    assertEquals("Dell Latitude 5440", result.get(0).getName());
    assertEquals("LAP-10001", result.get(0).getSku());
    assertEquals(18, result.get(0).getQuantity());

    verify(productRepository)
            .findByNameContainingIgnoreCaseOrSkuContainingIgnoreCase(
                    "Dell",
                    "Dell"
            );
}
@Test
void getLowStockProducts_shouldReturnLowStockProducts() {
    Product product = new Product();
    product.setId(1L);
    product.setName("Dell Latitude 5440");
    product.setQuantity(3);
    product.setReorderLevel(5);

    when(productRepository.findLowStockProducts())
            .thenReturn(java.util.List.of(product));

    var result = productService.getLowStockProducts();

    assertEquals(1, result.size());
    assertEquals("Dell Latitude 5440", result.get(0).getName());
    assertEquals(3, result.get(0).getQuantity());

    verify(productRepository).findLowStockProducts();
}
@Test
void deleteProduct_shouldDeleteExistingProduct() {
    Product product = new Product();
    product.setId(2L);
    product.setName("Dell Latitude 5440");

    when(productRepository.findById(2L))
            .thenReturn(Optional.of(product));

    productService.deleteProduct(2L);

    verify(productRepository).findById(2L);
    verify(productRepository).delete(product);
}
@Test
void getStockMovements_shouldReturnProductMovements() {
    Product product = new Product();
    product.setId(2L);

    StockMovement movement = new StockMovement();
    movement.setProduct(product);
    movement.setType(MovementType.RECEIVE);
    movement.setQuantity(5);

    when(productRepository.findById(2L))
            .thenReturn(Optional.of(product));

    when(stockMovementRepository
            .findByProductIdOrderByCreatedAtDesc(2L))
            .thenReturn(java.util.List.of(movement));

    var result = productService.getStockMovements(2L);

    assertEquals(1, result.size());
    assertEquals(MovementType.RECEIVE, result.get(0).getType());
    assertEquals(5, result.get(0).getQuantity());

    verify(productRepository).findById(2L);
    verify(stockMovementRepository)
            .findByProductIdOrderByCreatedAtDesc(2L);
}
}