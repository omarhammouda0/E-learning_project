package com.example.demo.service;
import com.example.demo.entity.category.Category;
import com.example.demo.entity.category.CategoryCreateDto;
import com.example.demo.entity.category.CategoryResponseDto;
import com.example.demo.exception.model.ErrorCode;
import com.example.demo.exception.types.DuplicateResourceException;
import com.example.demo.exception.types.NotFoundException;
import com.example.demo.mapper.CategoryMapper;
import com.example.demo.repo.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service

public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public CategoryService(CategoryRepository categoryRepository , CategoryMapper categoryMapper) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
    }

    private String generateSlug(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        return name.toLowerCase()
                .trim()
                .replaceAll("[^a-z0-9\\s]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("^-+|-+$", "");

    }

    @Transactional (readOnly = true)
    public List<CategoryResponseDto> getCategories()
    {
        return categoryRepository.findAll().stream ()
                .map ( categoryMapper::toResponseDto )
                .collect ( Collectors.toList () );
    }

    @Transactional (readOnly = true)
    public CategoryResponseDto getCategoryById(Long id)
    {
        Objects.requireNonNull(id, "id is required");
        return categoryRepository.findById(id)
                .map ( categoryMapper::toResponseDto )
                .orElseThrow (  () ->
                new NotFoundException ( ErrorCode.CATEGORY_NOT_FOUND.toString () ,
                        "Category with id " + id + " not found") );

    }

    @Transactional
    public CategoryResponseDto createCategory(CategoryCreateDto categoryCreateDto) {

        Objects.requireNonNull ( categoryCreateDto , "Category is required" );
        Category category = categoryMapper.toCategory ( categoryCreateDto );

        String trimmedName = category.getName().trim();
        category.setName( trimmedName );

        String slug = generateSlug(trimmedName);
        category.setSlug(slug);

        if (categoryRepository.existsByNameIgnoreCase ( trimmedName )) {
            throw new DuplicateResourceException
                    ( ErrorCode.CATEGORY_ALREADY_EXISTS.toString ( ) ,
                            "Category with name " + trimmedName + " already exists" );
        }

        if (categoryRepository.existsBySlug ( category.getSlug ( ) )) {
            throw new DuplicateResourceException (
                    ErrorCode.SLUG_ALREADY_EXISTS.toString ( ) ,
                    "Category with slug " + category.getSlug ( ) + " already exists" );
        }

        Category savedCategory = categoryRepository.save ( category );
        return categoryMapper.toResponseDto ( savedCategory );
    }

    @Transactional
    public CategoryResponseDto updateCategory(Long id , CategoryCreateDto categoryCreateDto) {

        Objects.requireNonNull ( categoryCreateDto , "Category is required" );
        Objects.requireNonNull ( id , "id is required" );

        Category categoryToUpdate = categoryRepository.findById ( id )
                .orElseThrow ( () -> new NotFoundException (
                        ErrorCode.CATEGORY_NOT_FOUND.toString ( ) , "Category with id " + id + " not found"
                ) );

        if (categoryCreateDto.name ( ) != null && !categoryCreateDto.name().trim().isEmpty() ) {

            String newName = categoryCreateDto.name ( ).trim ();

            if (categoryRepository.existsByNameIgnoreCaseAndIdNot ( newName , categoryToUpdate.getId ( ) ))
                throw new DuplicateResourceException (
                        ErrorCode.CATEGORY_ALREADY_EXISTS.toString ( ) ,
                        "Category with name " + newName + " already exists"
                );

            String newSlug = generateSlug (  newName );

            if (categoryRepository.existsBySlugAndIdNot(newSlug, id)) {
                throw new DuplicateResourceException(
                        ErrorCode.SLUG_ALREADY_EXISTS.toString(),
                        "Category with slug '" + newSlug + "' already exists"
                );
            }

            categoryToUpdate.setName ( newName );
            categoryToUpdate.setSlug ( newSlug );
        }

        if (categoryCreateDto.description ( ) != null) {
            categoryToUpdate.setDescription ( categoryCreateDto.description ( ).trim () );
        }
        
        if (categoryCreateDto.isActive()!= null) {
            categoryToUpdate.setActive ( categoryCreateDto.isActive ( ) );
        }

        Category updatedCategory = categoryRepository.save(categoryToUpdate);
        return categoryMapper.toResponseDto ( updatedCategory );
    }

    @Transactional
        public CategoryResponseDto deleteCategory(Long id)
    {
        Objects.requireNonNull (id, "id is required");

        Category categoryToDelete = categoryRepository.findById ( id ).orElseThrow ( () -> new NotFoundException (
                ErrorCode.CATEGORY_NOT_FOUND.toString () , "Category with id " + id + " not found"
        ));

        categoryRepository.deleteById ( id );
        return categoryMapper.toResponseDto ( categoryToDelete );
    }


}
