package com.example.highhillsstudio.HighHillsStudio.service.admin;

import com.example.highhillsstudio.HighHillsStudio.entity.Category;
import com.example.highhillsstudio.HighHillsStudio.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public Page<Category> listAll(String keyword, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        if(keyword != null && !keyword.isEmpty()) {
            return categoryRepository.findByIsActiveTrueAndNameContainingIgnoreCase(keyword, pageable);
        }
        return categoryRepository.findByIsActiveTrue(pageable);
    }

    public Category save(Category category) {
        return categoryRepository.save(category);
    }

    public Optional<Category> findById(Long id) {
        return categoryRepository.findById(id);
    }

    public void softDelete(Long id) {
        categoryRepository.findById(id).ifPresent(cat -> {
            cat.setActive(false);
            categoryRepository.save(cat);
        });
    }



    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

}
