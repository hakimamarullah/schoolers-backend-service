package com.schoolers.repository;

import com.schoolers.dto.projection.SimpleScheduleInfo;
import com.schoolers.enums.DayOfWeek;
import com.schoolers.models.Schedule;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalTime;
import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    List<Schedule> findByClassroomIdAndActiveTrue(Long classroomId);

    List<Schedule> findByTeacherIdAndActiveTrue(Long teacherId);

    List<Schedule> findByClassroomIdAndDayOfWeekAndActiveTrue(Long classroomId, DayOfWeek dayOfWeek);

    List<Schedule> findByTeacherIdAndDayOfWeekAndActiveTrue(Long teacherId, DayOfWeek dayOfWeek);

    List<Schedule> findByAcademicYearAndActiveTrue(String academicYear);

    @Query("""
            SELECT s FROM Schedule s 
            WHERE s.classroom.id = :classroomId 
            AND s.dayOfWeek = :dayOfWeek 
            AND s.active = true
            AND (:excludeId IS NULL OR s.id != :excludeId)
            AND ((s.startTime < :endTime AND s.endTime > :startTime))
            """)
    List<Schedule> findClassroomConflicts(
            @Param("classroomId") Long classroomId,
            @Param("dayOfWeek") DayOfWeek dayOfWeek,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("excludeId") Long excludeId
    );

    @Query("""
            SELECT s FROM Schedule s 
            WHERE s.teacher.id = :teacherId 
            AND s.dayOfWeek = :dayOfWeek 
            AND s.active = true
            AND (:excludeId IS NULL OR s.id != :excludeId)
            AND ((s.startTime < :endTime AND s.endTime > :startTime))
            """)
    List<Schedule> findTeacherConflicts(
            @Param("teacherId") Long teacherId,
            @Param("dayOfWeek") DayOfWeek dayOfWeek,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("excludeId") Long excludeId
    );


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
    SELECT s.id as id, s.classroom.id as classroomId,
    s.teacher.id as teacherId,
    s.subject.id as subjectId,
    s.startTime as startTime,
    s.endTime as endTime, s.active as IsActive FROM Schedule s
    WHERE s.dayOfWeek = :day AND s.active = true
    """)
    Page<SimpleScheduleInfo> getAllByDayOfWeekAndActiveIsTrue(DayOfWeek day, Pageable pageable);

    boolean existsByClassroomIdAndSubjectIdAndTeacherEmployeeNumber(Long classroomId, Long subjectId, String teacherId);
}
