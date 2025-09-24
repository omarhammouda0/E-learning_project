package com.example.demo.repo;

import com.example.demo.entity.course.Course;
import com.example.demo.entity.course.CourseResponseDto;
import com.example.demo.entity.course.Level;
import com.example.demo.entity.course.Status;
import com.example.demo.entity.user.User;
import org.hibernate.query.Page;
import org.springframework.data.domain.Pageable;
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



    Optional<Course> findByTitleIgnoreCase(String title);

    List<Course> findByInstructor(User user);

    List<Course> findByCategoryNameIgnoreCase(String categoryName);

    List<Course> findByStatus(Status status);

    List<Course> findAllByStatus(Status status);

    List<Course> findByLevel(Level level);

    @Query ("select c from Course c where c.price =0 or c.price is null")
    List <Course> findFreeCourses();

    @Query("SELECT c FROM Course c WHERE c.price >0")
    List<Course> findPaidCourses();

    List<Course> findByPrice(BigDecimal price);

    @Query ("select c from  Course c where c.price >=:minPrice and c.price <=:maxPrice")
    List<Course> findAllByCoursesBetween(
            @Param ("minPrice") BigDecimal minPrice , @Param ("maxPrice") BigDecimal maxPrice );

    boolean existsByTitle(String title);

    boolean existsByTitleIgnoreCase(String title);

    boolean existsByTitleIgnoreCaseAndIdNot(String trimmedTitle , Long courseId);
}


