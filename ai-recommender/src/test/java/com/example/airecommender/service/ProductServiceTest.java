package com.example.airecommender.service;

import com.example.airecommender.domain.Product;
import com.example.airecommender.dto.ProductRequestDto;
import com.example.airecommender.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void createProduct_shouldReturnCreatedProduct() {
        ProductRequestDto requestDto = new ProductRequestDto("Test Product", "Test Description", 10.0);
        Product expectedProduct = new Product("Test Product", "Test Description", 10.0, null);
        when(productRepository.save(any(Product.class))).thenReturn(expectedProduct);

        Product actualProduct = productService.createProduct(requestDto);

        assertThat(actualProduct.getName()).isEqualTo(expectedProduct.getName());
        assertThat(actualProduct.getDescription()).isEqualTo(expectedProduct.getDescription());
        assertThat(actualProduct.getPrice()).isEqualTo(expectedProduct.getPrice());
    }

    @Test
    void getProductById_shouldReturnProduct_whenProductExists() {
        Long productId = 1L;
        Product expectedProduct = new Product("Existing Product", "Description", 20.0, null);
        when(productRepository.findById(productId)).thenReturn(Optional.of(expectedProduct));

        Product actualProduct = productService.getProductById(productId);

        assertEquals(expectedProduct.getName(), actualProduct.getName());
        assertEquals(expectedProduct.getPrice(), actualProduct.getPrice());
    }

    @Test
    void getProductById_shouldThrowException_whenProductNotExists() {
        Long productId = 1L;
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> productService.getProductById(productId));
    }

    @Test
    void getAllProducts_shouldReturnAllProducts() {
        List<Product> expectedProducts = List.of(
                new Product("Product 1", "Desc 1", 10.0, null),
                new Product("Product 2", "Desc 2", 20.0, null)
        );
        when(productRepository.findAll()).thenReturn(expectedProducts);

        List<Product> actualProducts = productService.getAllProducts();

        assertThat(actualProducts).hasSize(2);
        assertThat(actualProducts.get(0).getName()).isEqualTo("Product 1");
        assertThat(actualProducts.get(1).getPrice()).isEqualTo(20.0);
    }
}