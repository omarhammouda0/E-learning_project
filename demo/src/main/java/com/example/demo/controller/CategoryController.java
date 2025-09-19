package com.example.demo.controller;
import com.example.demo.entity.category.CategoryCreateDto;
import com.example.demo.entity.category.CategoryResponseDto;
import com.example.demo.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")

public class CategoryController  {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponseDto>> getAllCategories() {
        return ResponseEntity.ok ( categoryService.getCategories () );
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponseDto> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok ( categoryService.getCategoryById ( id ) );
    }

    @PostMapping
    public ResponseEntity<CategoryResponseDto> saveCategory (@Valid @RequestBody CategoryCreateDto categoryCreateDto) {
        return ResponseEntity.status ( HttpStatus.CREATED ).body ( categoryService.createCategory (categoryCreateDto) );
    }

    @PutMapping ("/{id}")
    public ResponseEntity<CategoryResponseDto> updateCategory (@Valid @PathVariable Long id, @RequestBody CategoryCreateDto categoryCreateDto) {
        return ResponseEntity.ok ( categoryService.updateCategory ( id , categoryCreateDto) );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

}
