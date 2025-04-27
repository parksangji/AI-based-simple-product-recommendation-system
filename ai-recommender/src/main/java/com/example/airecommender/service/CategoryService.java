package com.example.airecommender.service;

import com.example.airecommender.domain.Category;
import com.example.airecommender.dto.CategoryRequestDto;
import com.example.airecommender.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public Category createCategory(CategoryRequestDto requestDto) {
        Category category = new Category(requestDto.getName());
        return categoryRepository.save(category);
    }

    @Transactional
    public Category updateCategory(Long id, CategoryRequestDto requestDto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("해당 카테고리를 찾을 수 없습니다. id: " + id));
        category.setName(requestDto.getName());
        return categoryRepository.save(category);
    }

    @Transactional
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new NoSuchElementException("해당 카테고리를 찾을 수 없습니다. id: " + id);
        }
        categoryRepository.deleteById(id);
    }

    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("해당 카테고리를 찾을 수 없습니다. id: " + id));
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }
}
