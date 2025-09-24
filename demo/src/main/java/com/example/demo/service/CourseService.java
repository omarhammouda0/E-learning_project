package com.example.demo.service;
import com.example.demo.entity.course.Course;
import com.example.demo.entity.course.CourseCreateDto;
import com.example.demo.entity.course.CourseResponseDto;
import com.example.demo.exception.model.ErrorCode;
import com.example.demo.exception.types.NotFoundException;
import com.example.demo.mapper.CourseMapper;
import com.example.demo.repo.CourseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service

public class CourseService {

    private final CourseRepository courseRepository;
    private final CourseMapper courseMapper;


    public CourseService(CourseRepository courseRepository , CourseMapper courseMapper) {
        this.courseRepository = courseRepository;
        this.courseMapper = courseMapper;
    }

    @Transactional (readOnly = true)
    public List < CourseResponseDto > getAllCourses() {
        return courseRepository.findAll ()
                .stream ()
                .map ( courseMapper::toCourseDto )
                .collect( Collectors.toList () );
    }

    @Transactional(readOnly = true)
    public CourseResponseDto getCourseById(Long id)
    {
        Objects.requireNonNull(id, "id is required");
        return courseRepository.findById ( id )
                .map ( courseMapper::toCourseDto )
                .orElseThrow (  () ->
                        new NotFoundException ( ErrorCode.COURSE_NOT_FOUND.toString () ,
                                "Course with id " + id + " not found") );
    }

    @Transactional (readOnly = true)
    public CourseResponseDto getCourseByTitle(String title)
    {

        if (title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be empty");
        }

        return courseRepository.findByTitle ( title )
                .map ( courseMapper::toCourseDto )
                .orElseThrow (  () ->
                        new NotFoundException ( ErrorCode.COURSE_NOT_FOUND.toString () ,
                                "Course with title " + title + " not found") );

    }

    @Transactional
    public CourseResponseDto createCourse (CourseCreateDto dto) {

        Objects.requireNonNull ( dto, "Course is required" );

        Course toSave = courseMapper.toCourse ( dto );
        courseRepository.save( toSave );
        return courseMapper.toCourseDto (  toSave );





    }
}
