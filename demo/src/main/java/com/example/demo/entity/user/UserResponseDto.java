package com.example.demo.entity.user;

public record UserResponseDto(


        String username ,
        String email ,
        String firstName ,
        String lastName ,
        Role role
) {
}
