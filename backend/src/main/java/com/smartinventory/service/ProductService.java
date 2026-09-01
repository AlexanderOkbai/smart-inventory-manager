package com.smartinventory.service;

import com.smartinventory.dto.StockRequest;
import com.smartinventory.entity.MovementType;
import com.smartinventory.entity.Product;
import com.smartinventory.entity.StockMovement;
import com.smartinventory.exception.ProductNotFoundException;
import com.smartinventory.repository.ProductRepository;
import com.smartinventory.repository.StockMovementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final StockMovementRepository stockMovementRepository;

    public ProductService(
            ProductRepository productRepository,
            StockMovementRepository stockMovementRepository) {

        this.productRepository = productRepository;
        this.stockMovementRepository = stockMovementRepository;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public List<Product> getLowStockProducts() {
        return productRepository.findLowStockProducts();
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    public Product createProduct(Product product) {
        if (productRepository.existsBySku(product.getSku())) {
            throw new RuntimeException(
                    "Product with SKU already exists: " + product.getSku()
            );
        }

        return productRepository.save(product);
    }

    public Product updateProduct(Long id, Product updatedProduct) {
        Product existingProduct = getProductById(id);

        existingProduct.setName(updatedProduct.getName());
        existingProduct.setSku(updatedProduct.getSku());
        existingProduct.setDescription(updatedProduct.getDescription());
        existingProduct.setQuantity(updatedProduct.getQuantity());
        existingProduct.setPrice(updatedProduct.getPrice());
        existingProduct.setReorderLevel(updatedProduct.getReorderLevel());

        existingProduct.setWarehouse(updatedProduct.getWarehouse());
        existingProduct.setZone(updatedProduct.getZone());
        existingProduct.setAisle(updatedProduct.getAisle());
        existingProduct.setRack(updatedProduct.getRack());
        existingProduct.setShelf(updatedProduct.getShelf());
        existingProduct.setBin(updatedProduct.getBin());

        return productRepository.save(existingProduct);
    }

    public void deleteProduct(Long id) {
        Product existingProduct = getProductById(id);
        productRepository.delete(existingProduct);
    }

   @Transactional
public Product receiveStock(Long id, StockRequest request) {
        Product product = getProductById(id);

        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new IllegalArgumentException(
                    "Stock quantity must be greater than zero"
            );
        }

        product.setQuantity(
                product.getQuantity() + request.getQuantity()
        );

        Product savedProduct = productRepository.save(product);

        StockMovement movement = new StockMovement(
                savedProduct,
                MovementType.RECEIVE,
                request.getQuantity()
        );

        stockMovementRepository.save(movement);

        return savedProduct;
    }
    @Transactional
    public Product issueStock(Long id, StockRequest request) {
        Product product = getProductById(id);

        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new IllegalArgumentException(
                    "Issue quantity must be greater than zero"
            );
        }

        if (request.getQuantity() > product.getQuantity()) {
            throw new IllegalArgumentException(
                    "Insufficient stock. Available: "
                            + product.getQuantity()
                            + ", requested: "
                            + request.getQuantity()
            );
        }

        product.setQuantity(
                product.getQuantity() - request.getQuantity()
        );

        Product savedProduct = productRepository.save(product);

        StockMovement movement = new StockMovement(
                savedProduct,
                MovementType.ISSUE,
                request.getQuantity()
        );

        stockMovementRepository.save(movement);

        return savedProduct;
    }
    public List<StockMovement> getStockMovements(Long productId) {
    getProductById(productId);

    return stockMovementRepository
            .findByProductIdOrderByCreatedAtDesc(productId);
}
}