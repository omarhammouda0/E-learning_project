package com.example.demo.mapper;

import com.example.demo.entity.category.Category;
import com.example.demo.entity.category.CategoryCreateDto;
import com.example.demo.entity.category.CategoryResponseDto;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component

public class CategoryMapper {

    public CategoryResponseDto toResponseDto(Category category) {


        return new CategoryResponseDto(


                category.getName () ,
                category.getDescription () ,
                category.getSlug () ,
                category.isActive (),
                category.getCreatedDate ()

        );
    }

    public Category toCategory(CategoryCreateDto categoryCreateDto) {

        Objects.requireNonNull ( categoryCreateDto, "Category is required" );

        return new Category(

                categoryCreateDto.name ().trim (),
                categoryCreateDto.description() != null ?
                        categoryCreateDto.description().trim() : null
        );
    }

}
