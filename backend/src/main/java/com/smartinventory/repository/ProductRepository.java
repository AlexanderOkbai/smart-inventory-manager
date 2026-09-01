package com.smartinventory.repository;

import java.util.List;
import com.smartinventory.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

Optional<Product> findBySku(String sku);

boolean existsBySku(String sku);

boolean existsBySkuAndIdNot(String sku, Long id);
List<Product> findByQuantityLessThanEqual(Integer reorderLevel);
@Query("SELECT p FROM Product p WHERE p.quantity <= p.reorderLevel")
List<Product> findLowStockProducts();
}
