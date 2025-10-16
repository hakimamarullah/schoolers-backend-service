package com.schoolers.repository;

import com.schoolers.dto.projection.AttendanceSessionInfo;
import com.schoolers.dto.projection.SimpleAttendanceSessionInfo;
import com.schoolers.enums.SessionStatus;
import com.schoolers.models.AttendanceSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceSessionRepository extends JpaRepository<AttendanceSession, Long> {

    /**
     * Find all sessions for a classroom on a specific date
     */
    @Query("SELECT a FROM AttendanceSession a " +
            "WHERE a.classroom.id = :classroomId " +
            "AND a.sessionDate = :date " +
            "ORDER BY a.startTime ASC")
    List<AttendanceSession> findByClassroomAndDate(@Param("classroomId") Long classroomId, @Param("date") LocalDate date);

    @Query("""
                select a.schedule.id
                from AttendanceSession a
                where a.sessionDate = :date
                  and a.schedule.id in :scheduleIds
            """)
    List<Long> findAllScheduleIdsBySessionDate(LocalDate date, List<Long> scheduleIds);

    @Query("""
                select a.id as id,
                a.classroom.id as classroomId,
                a.classroom.name as room,
                a.subject.name as subjectName,
                a.teacher.id as teacherId,
                a.sessionDate as sessionDate,
                a.startTime as startTime,
                a.endTime as endTime,
                a.topic as topic
                from AttendanceSession a
                where a.status = :status
                and a.classroom.id = :classroomId
                and a.sessionDate = :date
            """)
    List<SimpleAttendanceSessionInfo> getAllByStatusAndClassroomIdAndSessionDate(@Param("status") SessionStatus status,
                                                                                 @Param("classroomId") Long classroomId,
                                                                                 @Param("date") LocalDate date);

    Optional<AttendanceSession> findByScheduleIdAndTeacherEmployeeNumberAndSessionDateEquals(Long scheduleId, String teacherEmployeeNumber,
                                                                                             LocalDate sessionDate);

    @Query("SELECT a.classroom.id as classroomId, a.subject.name as subjectName from AttendanceSession a where a.id = :id")
    Optional<AttendanceSessionInfo> getSessionInfoById(Long id);

}
