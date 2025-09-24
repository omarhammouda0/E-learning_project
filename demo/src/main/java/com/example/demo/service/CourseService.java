package com.example.demo.service;
import com.example.demo.entity.category.Category;
import com.example.demo.entity.course.*;
import com.example.demo.entity.user.Role;
import com.example.demo.entity.user.User;
import com.example.demo.exception.model.ErrorCode;
import com.example.demo.exception.types.DuplicateResourceException;
import com.example.demo.exception.types.NotFoundException;
import com.example.demo.mapper.CourseMapper;
import com.example.demo.repo.CategoryRepository;
import com.example.demo.repo.CourseRepository;
import com.example.demo.repo.UserRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service

public class CourseService {

    private final CourseRepository courseRepository;
    private final CourseMapper courseMapper;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;


    public CourseService(CourseRepository courseRepository , CourseMapper courseMapper , CategoryRepository categoryRepository , UserRepository userRepository) {
        this.courseRepository = courseRepository;
        this.courseMapper = courseMapper;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }


    // ________________________Create__________________________

    @Transactional
    public CourseResponseDto createCourse (@Valid CourseCreateDto dto) {

        Objects.requireNonNull ( dto, "Course is required" );

        String trimmedTitle = dto.title ( ).replaceAll ( "\\s+" , " " ).trim ( );
        if (trimmedTitle.isEmpty () )
            throw new IllegalArgumentException ( "Title is required" );

        Long instructorId = dto.instructorId ();
        Long categoryId = dto.categoryId ();

        User instructor = userRepository.findById ( instructorId ).orElseThrow (
                () -> new NotFoundException ( ErrorCode.INSTRUCTOR_NOT_FOUND.toString ( ) ,
                        "Instructor not found" )
        );

        if (!instructor.getRole ().equals ( Role.INSTRUCTOR )) {
            throw new IllegalArgumentException ( "The user role must be an instructor" );
        }

        Category category = categoryRepository.findById ( categoryId ).orElseThrow (
                () -> new NotFoundException ( ErrorCode.CATEGORY_NOT_FOUND.toString ( ) ,
                        "category not found" )
        );

        if (courseRepository.existsByTitleIgnoreCase(trimmedTitle))
            throw new DuplicateResourceException ( ErrorCode.TITLE_ALREADY_EXISTS.toString () ,
                    "A course with this title already exists" );


        Course toSave = courseMapper.toCourse ( dto );
        toSave.setId ( null );
        toSave.setTitle (  trimmedTitle );
        toSave.setInstructor ( instructor );
        toSave.setCategory ( category );

        Course savedCourse = courseRepository.save(toSave);
        return courseMapper.toCourseDto(savedCourse);




    }


    // ________________________Read__________________________


    @Transactional (readOnly = true)
    public Page<CourseResponseDto> getAllCourses(Pageable pageable) {
        return courseRepository.findAll(pageable)
                .map(courseMapper::toCourseDto);

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
        Objects.requireNonNull (title, "title is required");

        if (title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be empty");
        }

        String trimmedTitle = title.trim ();

        return courseRepository.findByTitleIgnoreCase ( trimmedTitle )
                .map ( courseMapper::toCourseDto )
                .orElseThrow (  () ->
                        new NotFoundException ( ErrorCode.COURSE_NOT_FOUND.toString () ,
                                "Course with title " + trimmedTitle + " not found") );

    }

    @Transactional (readOnly = true)
    public List <CourseResponseDto> getCoursesByCategory (String category) {

        Objects.requireNonNull ( category , "category is required" );

        if (category.trim ( ).isEmpty ( )) {
            throw new IllegalArgumentException ( "Category cannot be empty" );
        }

        String trimmedCategory = category.trim ( );

        List<Course> courses = courseRepository.findByCategoryNameIgnoreCase ( trimmedCategory );

        if (courses.isEmpty ( )) {
            boolean categoryExists = categoryRepository.existsByNameIgnoreCase ( trimmedCategory );
            if (!categoryExists)
                throw new NotFoundException ( ErrorCode.CATEGORY_NOT_FOUND.toString ( ) ,
                        "Category " + trimmedCategory + " not found" );
        }

        return courses.stream ( )
                .map ( courseMapper::toCourseDto )
                .collect ( Collectors.toList ( ) );
    }

    @Transactional (readOnly = true)
    public List <CourseResponseDto> getCoursesByInstructor (String instructorName){

        Objects.requireNonNull(instructorName, "instructor is required");

        if (instructorName.trim().isEmpty()) {
            throw new IllegalArgumentException("Instructor cannot be empty");
        }

        String[] parts = instructorName.split("\\s+", 2);
        String firstName = parts[0].trim ();
        String lastName  = (parts.length > 1) ? parts[1].trim () : "";

        User u = userRepository.findByFirstNameAndLastNameIgnoreCase ( firstName , lastName ).orElseThrow (
                (() ->
                        new NotFoundException ( ErrorCode.INSTRUCTOR_NOT_FOUND.toString ( ) ,
                                "Instructor with name " + firstName + " " + lastName + " not found" ))
        );

        if (! u.getRole ().equals ( Role.INSTRUCTOR ))
            throw new IllegalArgumentException (  "The given user must be an instructor" );


        return courseRepository.findByInstructor(u)
                .stream ()
                .map ( courseMapper::toCourseDto )
                .toList ( );


    }

    @Transactional (readOnly = true)
    public List<CourseResponseDto> getCoursesByStatus(String status) {

        Objects.requireNonNull ( status , "status is required" );
        String trimmedStatus = status.trim ( );
        if (trimmedStatus.isEmpty ( )) {
            throw new IllegalArgumentException ( "Status cannot be empty" );
        }

        Status courseStatus = Arrays.stream(Status.values())
                .filter(s -> s.name ().equalsIgnoreCase(trimmedStatus))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Invalid status '" + status + "'. Must be one of: " +
                                Arrays.stream(Status.values()).map(Enum::name).collect(Collectors.joining(", "))
                ));

        return courseRepository.findAllByStatus(courseStatus)
                .stream ()
                .map(courseMapper::toCourseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CourseResponseDto> getCoursesByLevel (String level) {
        Objects.requireNonNull(level, "level is required");
        String trimmedLevel = level.trim ( );
        if (trimmedLevel.isEmpty ( )) {
            throw new IllegalArgumentException("Level cannot be empty");
        }

        Level courseLevel = Arrays.stream( Level.values())
                .filter(l -> l.name ().equalsIgnoreCase(trimmedLevel))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Invalid level '" + trimmedLevel + "'. Must be one of: " +
                                Arrays.stream(Level.values()).map(Enum::name).collect(Collectors.joining(", "))
                ));

        return courseRepository.findByLevel(courseLevel)
                .stream ()
                .map ( courseMapper::toCourseDto )
                .toList ();

    }

    @Transactional(readOnly = true)
    public List <CourseResponseDto> getFreeCourses () {
        return courseRepository.findFreeCourses ()
                .stream( )
                .map ( courseMapper::toCourseDto )
                .toList ();
    }

    @Transactional (readOnly = true)
    public List <CourseResponseDto> getPaidCourses ( ) {
        return courseRepository.findPaidCourses (  )
                .stream ()
                .map ( courseMapper::toCourseDto )
                .toList ();
    }

    @Transactional(readOnly = true)
    public List <CourseResponseDto> getCoursesByPrice (BigDecimal price) {

        Objects.requireNonNull(price, "price is required");

        if (price.compareTo ( BigDecimal.ZERO ) < 0 )
            throw new IllegalArgumentException ("Price cannot be negative");

        return courseRepository.findByPrice ( price )
                .stream ()
                .map ( courseMapper::toCourseDto )
                .toList ();
    }

    @Transactional (readOnly = true)
    public List<CourseResponseDto>  findAllByCoursesBetween (BigDecimal from , BigDecimal to) {

        Objects.requireNonNull(from, "from is required");
        Objects.requireNonNull( to, "to is required");


       if (from.compareTo ( BigDecimal.ZERO ) < 0 || to.compareTo ( BigDecimal.ZERO ) < 0 )
           throw new IllegalArgumentException ("Prices cannot be negative");

       if (from.compareTo ( to) > 0 )
           throw new IllegalArgumentException (
                   "Minimum price (" + from + ") must be less than maximum price (" + to + ")");

        BigDecimal min = from.setScale(2, RoundingMode.DOWN);
        BigDecimal max = to.setScale(2, RoundingMode.UP);


        return courseRepository.findAllByCoursesBetween(min, max)
                .stream ()
                .map ( courseMapper::toCourseDto )
                .toList ();
    }

    // ________________________Update__________________________

    @Transactional
    public CourseResponseDto updateCourse (Long courseId , @Valid CourseUpdateDto dto) {
        Objects.requireNonNull ( dto, "dto is required");
        Objects.requireNonNull ( courseId, "course Id is required");

        Course course = courseRepository.findById ( courseId )
                .orElseThrow (  () ->
                        new NotFoundException ( ErrorCode.COURSE_NOT_FOUND.toString () ,
                                "Course with id " + courseId + " not found"));

        if (dto.instructorId () != null){
            User instructor = userRepository.findById ( dto.instructorId () )
                    .orElseThrow (  () ->
                    new NotFoundException ( ErrorCode.INSTRUCTOR_NOT_FOUND.toString () ,
                            "Instructor with id " + dto.instructorId () + " not found"));

            if (!Role.INSTRUCTOR.equals(instructor.getRole())) {
                throw new IllegalArgumentException("User is not an instructor");
            }

            course.setInstructor ( instructor );
        }

        if (dto.categoryId () != null) {
            Category category = categoryRepository.findById ( dto.categoryId () )
                    .orElseThrow ( () ->
                            new NotFoundException ( ErrorCode.CATEGORY_NOT_FOUND.toString ( ) ,
                                    "Category with id " + dto.categoryId ( ) + " not found" ) );

            course.setCategory ( category );
        }

        if (dto.title () != null) {
            String trimmedTitle = dto.title ().replaceAll ( "\\s+" , " " ).trim ( );

            if (trimmedTitle.isEmpty ())
                throw new IllegalArgumentException ( "Title cannot be empty");

            if (courseRepository.existsByTitleIgnoreCaseAndIdNot(trimmedTitle, courseId))
                throw new DuplicateResourceException (
                        ( ErrorCode.COURSE_ALREADY_EXISTS.toString ( )) ,
                        "A course with this title " + trimmedTitle + " already exists" );

            course.setTitle ( trimmedTitle );

            }

        if (dto.description () != null) {
            String trimmedDescription = dto.description ().replaceAll ( "\\s+" , " " ).trim ();
            course.setDescription ( trimmedDescription );

        }

        if (dto.shortDescription () != null) {
            String trimmedShortDescription = dto.shortDescription ( ).replaceAll ( "\\s+" , " " ).trim ( );
            course.setShortDescription ( trimmedShortDescription );
        }

        if (dto.duration() != null)
            course.setDuration(dto.duration());


        if (dto.price() != null)
            course.setPrice(dto.price());


        if (dto.level() != null) {
            course.setLevel(dto.level());
        }

        if (dto.status() != null) {
            course.setStatus(dto.status());
        }


        Course updatedCourse = courseRepository.save( course );
        return courseMapper.toCourseDto ( updatedCourse );


    }

    // ________________________Delete__________________________

    @Transactional
    public CourseResponseDto archiveCourse (Long courseId) {

        Objects.requireNonNull(courseId, "course Id is required");

        Course toDelete = courseRepository.findById ( courseId ).orElseThrow (
                () ->  new NotFoundException ( ErrorCode.COURSE_NOT_FOUND.toString () ,
                        "Course with id " + courseId + " not found"));

        if (toDelete.getStatus ().equals ( Status.ARCHIVED ))
            throw new IllegalStateException ( "Course is already archived");

        toDelete.setStatus ( Status.ARCHIVED );

        Course archivedCourse = courseRepository.save(toDelete);
        return courseMapper.toCourseDto(archivedCourse);
    }

}
