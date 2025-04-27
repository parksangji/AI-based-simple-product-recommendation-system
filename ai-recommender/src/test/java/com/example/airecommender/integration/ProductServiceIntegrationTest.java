package com.example.airecommender.integration;

import com.example.airecommender.domain.Product;
import com.example.airecommender.dto.ProductRequestDto;
import com.example.airecommender.repository.ProductRepository;
import com.example.airecommender.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
public class ProductServiceIntegrationTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Test
    void createProduct_shouldSaveProductToDatabase() {
        ProductRequestDto requestDto = new ProductRequestDto("New Product", "New Description", 25.0);

        Product createdProduct = productService.createProduct(requestDto);

        assertThat(createdProduct.getId()).isNotNull();
        Product foundProduct = productRepository.findById(createdProduct.getId()).orElse(null);
        assertThat(foundProduct).isNotNull();
        assertThat(foundProduct.getName()).isEqualTo("New Product");
        assertThat(foundProduct.getPrice()).isEqualTo(25.0);
    }

    @Test
    void getProductById_shouldRetrieveProductFromDatabase() {
        Product product = new Product("Existing Product", "Description", 30.0, null);
        Product savedProduct = productRepository.save(product);

        Product retrievedProduct = productService.getProductById(savedProduct.getId());

        assertThat(retrievedProduct).isNotNull();
        assertThat(retrievedProduct.getName()).isEqualTo("Existing Product");
        assertThat(retrievedProduct.getPrice()).isEqualTo(30.0);
    }

    @Test
    void deleteProduct_shouldRemoveProductFromDatabase() {
        Product product = new Product("ToDelete", "Description", 15.0, null);
        Product savedProduct = productRepository.save(product);

        productService.deleteProduct(savedProduct.getId());

        assertThrows(NoSuchElementException.class, () -> productService.getProductById(savedProduct.getId()));
        assertThat(productRepository.findById(savedProduct.getId())).isEmpty();
    }

    @Test
    void getAllProducts_shouldReturnAllProductsFromDatabase() {
        productRepository.save(new Product("Product A", "Desc A", 10.0, null));
        productRepository.save(new Product("Product B", "Desc B", 20.0, null));

        List<Product> allProducts = productService.getAllProducts();

        assertThat(allProducts).hasSizeGreaterThanOrEqualTo(2);
        assertThat(allProducts).extracting("name").contains("Product A", "Product B");
    }
}