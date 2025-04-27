package com.example.airecommender.service;

import com.example.airecommender.domain.Product;
import com.example.airecommender.dto.ProductRequestDto;
import com.example.airecommender.repository.ProductRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class ProductService {
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional
    public Product createProduct(ProductRequestDto requestDto) {
        Product product = new Product(requestDto.getName(), requestDto.getDescription(), requestDto.getPrice());
        return productRepository.save(product);
    }

    @Transactional
    public Product updateProduct(Long id, ProductRequestDto requestDto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("해당 상품을 찾을 수 없습니다. id: " + id));
        product.setName(requestDto.getName());
        product.setDescription(requestDto.getDescription());
        product.setPrice(requestDto.getPrice());
        return productRepository.save(product);
    }

    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new NoSuchElementException("해당 상품을 찾을 수 없습니다. id: " + id);
        }
        productRepository.deleteById(id);
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("해당 상품을 찾을 수 없습니다. id: " + id));
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }
}
