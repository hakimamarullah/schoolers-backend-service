package com.schoolers.service.impl;

import com.schoolers.dto.ApiResponse;
import com.schoolers.dto.request.RegisterStaffRequest;
import com.schoolers.dto.request.RegisterStudentRequest;
import com.schoolers.dto.response.UserRegistrationResponse;
import com.schoolers.enums.UserRole;
import com.schoolers.exceptions.DataNotFoundException;
import com.schoolers.exceptions.DuplicateDataException;
import com.schoolers.models.Classroom;
import com.schoolers.models.Student;
import com.schoolers.models.Teacher;
import com.schoolers.models.User;
import com.schoolers.repository.ClassroomRepository;
import com.schoolers.repository.StudentRepository;
import com.schoolers.repository.TeacherRepository;
import com.schoolers.repository.UserRepository;
import com.schoolers.service.IFileStorageService;
import com.schoolers.service.IUserRegistrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
@RequiredArgsConstructor
@RegisterReflectionForBinding({
        UserRegistrationResponse.class,
        RegisterStudentRequest.class,
        RegisterStaffRequest.class,
})
public class UserRegistrationService implements IUserRegistrationService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final ClassroomRepository classroomRepository;
    private final PasswordEncoder passwordEncoder;
    private final IFileStorageService fileStorageService;

    @Transactional
    @Override
    public ApiResponse<UserRegistrationResponse> registerStudent(RegisterStudentRequest request, MultipartFile profilePicture) {
        if (studentRepository.existsByStudentNumber(request.getStudentNumber())) {
            throw new DuplicateDataException("Student number already exists");
        }

        // Validate login ID uniqueness
        if (userRepository.existsByLoginId(request.getStudentNumber())) {
            throw new DuplicateDataException("Login ID already exists");
        }

        // Validate email uniqueness
        if (request.getEmail() != null && !request.getEmail().isEmpty()
                && userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateDataException("Email already exists");
        }

        // Validate classroom exists
        Classroom classroom = classroomRepository.findById(request.getClassroomId())
                .orElseThrow(() -> new DataNotFoundException("Classroom not found"));

        // Store profile picture if provided
        String profilePictureUrl = null;
        if (profilePicture != null && !profilePicture.isEmpty()) {
            profilePictureUrl = fileStorageService.storeProfilePicture(
                    profilePicture, "students", request.getStudentNumber()
            );
        }

        // Create user account
        User user = new User();
        user.setLoginId(request.getStudentNumber());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setProfilePictUri(profilePictureUrl);
        user.setRole(UserRole.STUDENT);
        user.setActive(true);
        user.setGender(request.getGender());

        user = userRepository.save(user);

        // Create student profile
        Student student = new Student();
        student.setUser(user);
        student.setStudentNumber(request.getStudentNumber());
        student.setClassroom(classroom);


        student = studentRepository.save(student);

        log.info("Student registered successfully: {} ({})", user.getFullName(), user.getLoginId());

        return ApiResponse.setResponse(new UserRegistrationResponse(
                user.getId(),
                student.getId(),
                user.getLoginId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                user.getProfilePictUri(),
                user.getCreatedDate()
        ), 201);
    }

    @Transactional
    @Override
    public ApiResponse<UserRegistrationResponse> registerTeacher(RegisterStaffRequest request, MultipartFile profilePicture) {
        // Validate employee number uniqueness
        if (teacherRepository.existsByEmployeeNumber(request.getEmployeeNumber())) {
            throw new DuplicateDataException("Employee number already exists");
        }

        // Validate login ID uniqueness
        if (userRepository.existsByLoginId(request.getEmployeeNumber())) {
            throw new DuplicateDataException("Login ID already exists");
        }

        // Validate email uniqueness
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateDataException("Email already exists");
        }

        // Store profile picture if provided
        String profilePictureUrl = null;
        if (profilePicture != null && !profilePicture.isEmpty()) {
            profilePictureUrl = fileStorageService.storeProfilePicture(
                    profilePicture, "teachers", request.getEmployeeNumber()
            );
        }

        // Create user account
        User user = new User();
        user.setLoginId(request.getEmployeeNumber());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setProfilePictUri(profilePictureUrl);
        user.setRole(UserRole.TEACHER);
        user.setActive(true);
        user.setGender(request.getGender());

        user = userRepository.save(user);

        // Create teacher profile
        Teacher teacher = new Teacher();
        teacher.setUser(user);
        teacher.setEmployeeNumber(request.getEmployeeNumber());


        teacher = teacherRepository.save(teacher);

        log.info("Teacher registered successfully: {} ({})", user.getFullName(), user.getLoginId());

        return ApiResponse.setResponse(new UserRegistrationResponse(
                user.getId(),
                teacher.getId(),
                user.getLoginId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                user.getProfilePictUri(),
                user.getCreatedDate()
        ), 201);
    }

    @Transactional
    @Override
    public ApiResponse<UserRegistrationResponse> registerAdmin(RegisterStaffRequest request, MultipartFile profilePicture) {
        // Validate login ID uniqueness
        if (userRepository.existsByLoginId(request.getEmployeeNumber())) {
            throw new DuplicateDataException("Login ID already exists");
        }

        // Validate email uniqueness
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateDataException("Email already exists");
        }

        // Store profile picture if provided
        String profilePictureUrl = null;
        if (profilePicture != null && !profilePicture.isEmpty()) {
            profilePictureUrl = fileStorageService.storeProfilePicture(
                    profilePicture, "admins", request.getEmployeeNumber()
            );
        }

        // Create user account
        User user = new User();
        user.setLoginId(request.getEmployeeNumber());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setProfilePictUri(profilePictureUrl);
        user.setRole(UserRole.OFFICE_ADMIN);
        user.setActive(true);
        user.setGender(request.getGender());

        user = userRepository.save(user);

        log.info("Office admin registered successfully: {} ({})", user.getFullName(), user.getLoginId());

        return ApiResponse.setResponse(new UserRegistrationResponse(
                user.getId(),
                null, // Office admin doesn't have separate profile
                user.getLoginId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                user.getProfilePictUri(),
                user.getCreatedDate()
        ), 201);
    }

    @Transactional
    public ApiResponse<String> updateProfilePicture(Long userId, MultipartFile profilePicture) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Delete old profile picture if exists
        if (user.getProfilePictUri() != null) {
            fileStorageService.deleteProfilePicture(user.getProfilePictUri());
        }

        // Store new profile picture
        String userType = switch (user.getRole()) {
            case STUDENT -> "students";
            case TEACHER -> "teachers";
            case OFFICE_ADMIN -> "admins";
        };

        String profilePictureUrl = fileStorageService.storeProfilePicture(
                profilePicture, userType, user.getLoginId()
        );

        user.setProfilePictUri(profilePictureUrl);
        userRepository.save(user);

        log.info("Profile picture updated for user: {}", user.getLoginId());

        return ApiResponse.setSuccess(profilePictureUrl);
    }
}
