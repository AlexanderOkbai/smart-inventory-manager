package com.smartinventory.controller;

import com.smartinventory.entity.Product;
import com.smartinventory.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.smartinventory.exception.ProductNotFoundException;

import com.smartinventory.dto.StockRequest;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;
   
  

    @MockitoBean
    private ProductService productService;

    @Test
    void getAllProducts_shouldReturnProducts() throws Exception {
        Product product = new Product();
        product.setId(2L);
        product.setName("Dell Latitude 5440");
        product.setSku("LAP-10001");
        product.setQuantity(18);

        when(productService.getAllProducts())
                .thenReturn(List.of(product));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[0].name").value("Dell Latitude 5440"))
                .andExpect(jsonPath("$[0].quantity").value(18));
    }
@Test
void searchProducts_shouldReturnMatchingProducts() throws Exception {
    Product product = new Product();
    product.setId(2L);
    product.setName("Dell Latitude 5440");
    product.setSku("LAP-10001");
    product.setQuantity(18);

    when(productService.searchProducts("Dell"))
            .thenReturn(List.of(product));

    mockMvc.perform(
                    get("/api/products/search")
                            .param("keyword", "Dell")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(2))
            .andExpect(jsonPath("$[0].name").value("Dell Latitude 5440"))
            .andExpect(jsonPath("$[0].sku").value("LAP-10001"))
            .andExpect(jsonPath("$[0].quantity").value(18));
}

    @Test
    void getProductById_shouldReturnProduct() throws Exception {
        Product product = new Product();
        product.setId(2L);
        product.setName("Dell Latitude 5440");
        product.setSku("LAP-10001");
        product.setQuantity(18);

        when(productService.getProductById(2L))
                .thenReturn(product);

        mockMvc.perform(get("/api/products/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.name").value("Dell Latitude 5440"))
                .andExpect(jsonPath("$.quantity").value(18));
    }

    @Test
    void getLowStockProducts_shouldReturnLowStockProducts() throws Exception {
        Product product = new Product();
        product.setId(2L);
        product.setName("Dell Latitude 5440");
        product.setSku("LAP-10001");
        product.setQuantity(4);
        product.setReorderLevel(5);

        when(productService.getLowStockProducts())
                .thenReturn(List.of(product));

        mockMvc.perform(get("/api/products/low-stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[0].quantity").value(4))
                .andExpect(jsonPath("$[0].reorderLevel").value(5));
    }

    @Test
    void receiveStock_shouldReturnUpdatedProduct() throws Exception {
        Product product = new Product();
        product.setId(2L);
        product.setName("Dell Latitude 5440");
        product.setSku("LAP-10001");
        product.setQuantity(28);

        when(productService.receiveStock(eq(2L), any()))
                .thenReturn(product);

        mockMvc.perform(
                        post("/api/products/2/stock/receive")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"quantity\":10}")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.quantity").value(28));
    }

    @Test
    void issueStock_shouldReturnUpdatedProduct() throws Exception {
        Product product = new Product();
        product.setId(2L);
        product.setName("Dell Latitude 5440");
        product.setSku("LAP-10001");
        product.setQuantity(15);

        when(productService.issueStock(eq(2L), any()))
                .thenReturn(product);

        mockMvc.perform(
                        post("/api/products/2/stock/issue")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"quantity\":3}")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.quantity").value(15));
    }
    @Test
    void getProductById_shouldReturnNotFoundWhenProductDoesNotExist()
            throws Exception {

        when(productService.getProductById(999L))
                .thenThrow(new ProductNotFoundException(999L));

        mockMvc.perform(get("/api/products/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message")
                        .value("Product not found with id: 999"));
    }

    @Test
    void issueStock_shouldReturnBadRequestWhenInsufficientStock()
            throws Exception {

        StockRequest request = new StockRequest();
        request.setQuantity(999);

        when(productService.issueStock(eq(2L), any(StockRequest.class)))
                .thenThrow(new IllegalArgumentException(
                        "Insufficient stock. Available: 18, requested: 999"
                ));

        mockMvc.perform(
                        post("/api/products/2/stock/issue")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
        {
            "quantity": 999
        }
        """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("Insufficient stock. Available: 18, requested: 999"));
    }
@Test
void issueStock_shouldReturnBadRequestWhenQuantityIsZero()
        throws Exception {

    mockMvc.perform(
                    post("/api/products/2/stock/issue")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                    {
                        "quantity": 0
                    }
                    """)
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Bad Request"))
            .andExpect(jsonPath("$.message")
                    .value("Quantity must be at least 1"));
}

@Test
void receiveStock_shouldReturnBadRequestWhenQuantityIsNegative()
        throws Exception {

    mockMvc.perform(
                    post("/api/products/2/stock/receive")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                    {
                        "quantity": -5
                    }
                    """)
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Bad Request"))
            .andExpect(jsonPath("$.message")
                    .value("Quantity must be at least 1"));
}

@Test
void createProduct_shouldReturnBadRequestWhenNameIsBlank()
        throws Exception {

    mockMvc.perform(
                    post("/api/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                    {
                        "name": "",
                        "sku": "LAP-10002",
                        "quantity": 10,
                        "price": 899.99,
                        "reorderLevel": 5
                    }
                    """)
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Bad Request"))
            .andExpect(jsonPath("$.message")
                    .value("Product name is required"));
}

@Test
void createProduct_shouldReturnBadRequestWhenPriceIsNegative()
        throws Exception {

    mockMvc.perform(
                    post("/api/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                    {
                        "name": "Dell Latitude 5450",
                        "sku": "LAP-10002",
                        "quantity": 10,
                        "price": -100.00,
                        "reorderLevel": 5
                    }
                    """)
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Bad Request"))
            .andExpect(jsonPath("$.message")
                    .value("Price cannot be negative"));
}

@Test
void updateProduct_shouldReturnBadRequestWhenQuantityIsNegative()
        throws Exception {

    mockMvc.perform(
                    put("/api/products/2")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                    {
                        "name": "Dell Latitude 5440",
                        "sku": "LAP-10001",
                        "quantity": -5,
                        "price": 899.99,
                        "reorderLevel": 5
                    }
                    """)
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Bad Request"))
            .andExpect(jsonPath("$.message")
                    .value("Quantity cannot be negative"));
}

@Test
void createProduct_shouldReturnBadRequestWhenSkuAlreadyExists()
        throws Exception {

    when(productService.createProduct(any(Product.class)))
            .thenThrow(new IllegalArgumentException(
                    "Product with SKU already exists: LAP-10001"
            ));

    mockMvc.perform(
                    post("/api/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                    {
                        "name": "Dell Latitude 5440",
                        "sku": "LAP-10001",
                        "quantity": 10,
                        "price": 899.99,
                        "reorderLevel": 5
                    }
                    """)
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Bad Request"))
            .andExpect(jsonPath("$.message")
                    .value("Product with SKU already exists: LAP-10001"));
}
}