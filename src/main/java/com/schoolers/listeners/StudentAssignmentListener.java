package com.schoolers.listeners;

import com.schoolers.dto.event.NewAssignmentEvent;
import com.schoolers.enums.SubmissionStatus;
import com.schoolers.models.Assignment;
import com.schoolers.models.Student;
import com.schoolers.models.StudentAssignment;
import com.schoolers.repository.StudentAssignmentRepository;
import com.schoolers.repository.StudentRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class StudentAssignmentListener {

    private final StudentAssignmentRepository studentAssignmentRepository;

    private final StudentRepository studentRepository;

    private final EntityManager entityManager;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(value = NewAssignmentEvent.class, phase = TransactionPhase.AFTER_COMPLETION)
    public void onNewAssignment(NewAssignmentEvent event) {
      log.info("[NEW ASSIGNMENT] Classroom: {} Assignment: {}", event.getClassroomId(), event.getAssignmentId());
      var assignment = entityManager.getReference(Assignment.class, event.getAssignmentId());
      var studentAssignments = studentRepository.findAllIdByClassroomId(event.getClassroomId())
              .stream().map(it -> {
                  StudentAssignment studentAssignment = new StudentAssignment();
                  studentAssignment.setAssignment(assignment);
                  studentAssignment.setStudent(entityManager.getReference(Student.class, it));
                  studentAssignment.setStatus(SubmissionStatus.NOT_SUBMITTED);
                  return studentAssignment;
              }).toList();
      if (studentAssignments.isEmpty()) {
          return;
      }
      studentAssignmentRepository.saveAll(studentAssignments);
      log.info("[NEW ASSIGNMENT] Student assignments created successfully. Count: {}", studentAssignments.size());
    }
}
