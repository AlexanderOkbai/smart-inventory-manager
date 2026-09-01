package com.smartinventory.controller;
import jakarta.validation.Valid;
import com.smartinventory.dto.StockRequest;
import com.smartinventory.entity.Product;
import com.smartinventory.entity.StockMovement;
import com.smartinventory.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "http://localhost:5173")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }

    @GetMapping("/low-stock")
    public List<Product> getLowStockProducts() {
        return productService.getLowStockProducts();
    }

    @GetMapping("/{id}")
    public Product getProductById(@PathVariable Long id) {
        return productService.getProductById(id);
    }

    @PostMapping
@ResponseStatus(HttpStatus.CREATED)
public Product createProduct(@Valid @RequestBody Product product) {
    return productService.createProduct(product);
}

    @PutMapping("/{id}")
public Product updateProduct(
        @PathVariable Long id,
        @Valid @RequestBody Product product) {
    return productService.updateProduct(id, product);
}

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
    }

    @PostMapping("/{id}/stock/receive")
    public Product receiveStock(
            @PathVariable Long id,
            @Valid @RequestBody StockRequest request) {
        return productService.receiveStock(id, request);
    }

    @PostMapping("/{id}/stock/issue")
    public Product issueStock(
            @PathVariable Long id,
            @Valid @RequestBody StockRequest request) {
        return productService.issueStock(id, request);
    }
    @GetMapping("/{id}/movements")
public List<StockMovement> getStockMovements(@PathVariable Long id) {
    return productService.getStockMovements(id);
}
} 