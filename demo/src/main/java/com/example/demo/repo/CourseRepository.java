package com.example.demo.repo;

import com.example.demo.entity.course.Course;
import com.example.demo.entity.course.Level;
import com.example.demo.entity.course.Status;
import com.example.demo.entity.user.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Repository

public interface CourseRepository extends JpaRepository<Course, Long> {

    boolean existsByTitleIgnoreCase(String title);

    // Service expects Optional<Course>
    @EntityGraph(attributePaths = {"instructor","category"})
    Optional<Course> findByTitleIgnoreCase(String title);

    // Service calls findByCategoryNameIgnoreCase(String) — add JPQL since the proper
    // derived name would be findAllByCategory_NameIgnoreCase(...)
    @EntityGraph(attributePaths = {"instructor","category"})
    @Query("select c from Course c where lower(c.category.name) = lower(:name)")
    List<Course> findByCategoryNameIgnoreCase(@Param("name") String name);

    // By instructor entity
    @EntityGraph(attributePaths = {"instructor","category"})
    List<Course> findByInstructor(User instructor);

    // By status
    @EntityGraph(attributePaths = {"instructor","category"})
    List<Course> findAllByStatus(Status status);

    // Your service uses findByLevel(...); return a list
    @EntityGraph(attributePaths = {"instructor","category"})
    List<Course> findByLevel(Level level);

    // Free courses: price = 0 OR NULL
    @EntityGraph(attributePaths = {"instructor","category"})
    @Query("select c from Course c where c.price = 0 or c.price is null")
    List<Course> findFreeCourses();

    // Paid courses: price > 0
    @EntityGraph(attributePaths = {"instructor","category"})
    @Query("select c from Course c where c.price > 0")
    List<Course> findPaidCourses();

    // Exact price
    @EntityGraph(attributePaths = {"instructor","category"})
    List<Course> findByPrice(BigDecimal price);

    // Range — keep your current service name; use BETWEEN JPQL
    @EntityGraph(attributePaths = {"instructor","category"})
    @Query("select c from Course c where c.price between :min and :max")
    List<Course> findAllByCoursesBetween(@Param("min") BigDecimal min,
                                         @Param("max") BigDecimal max);

    // Title uniqueness check excluding current id (case-insensitive)
    boolean existsByTitleIgnoreCaseAndIdNot(String title, Long id);}
