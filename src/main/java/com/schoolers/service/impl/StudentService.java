package com.schoolers.service.impl;

import com.schoolers.exceptions.DataNotFoundException;
import com.schoolers.repository.StudentRepository;
import com.schoolers.service.IStudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StudentService implements IStudentService {

    private final StudentRepository studentRepository;

    @Transactional
    @Override
    public void changeClassroom(Long newClassroomId, Long studentId) {
        long updatedRows = studentRepository.updateStudentById(studentId, newClassroomId);
        if (updatedRows == 0) {
            throw new DataNotFoundException("Student or classroom not found");
        }
    }
}
