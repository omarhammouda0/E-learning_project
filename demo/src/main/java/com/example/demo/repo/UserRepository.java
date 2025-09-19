package com.example.demo.repo;

import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);


    boolean existsByEmailAndIdNot(String email , Long id);

    boolean existsByUserName(String userName);

    boolean existsByUserNameAndIdNot(String userName , Long id);
}
