package com.schoolers.service.impl;

import com.schoolers.dto.ApiResponse;
import com.schoolers.dto.PagedResponse;
import com.schoolers.dto.event.ActivityLogEvent;
import com.schoolers.dto.projection.TeacherInfo;
import com.schoolers.dto.request.ScheduleRequest;
import com.schoolers.dto.response.ClassroomSchedulesInfo;
import com.schoolers.dto.response.ScheduleResponse;
import com.schoolers.enums.ActivityType;
import com.schoolers.enums.DayOfWeek;
import com.schoolers.exceptions.DataNotFoundException;
import com.schoolers.exceptions.ScheduleConflictException;
import com.schoolers.models.Classroom;
import com.schoolers.models.Schedule;
import com.schoolers.models.Subject;
import com.schoolers.models.Teacher;
import com.schoolers.repository.ClassroomRepository;
import com.schoolers.repository.ScheduleRepository;
import com.schoolers.repository.SubjectRepository;
import com.schoolers.repository.TeacherRepository;
import com.schoolers.service.IScheduleService;
import com.schoolers.utils.ScheduleMapper;
import com.schoolers.validator.ScheduleValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@RegisterReflectionForBinding({
        Schedule.class,
        ScheduleRequest.class,
        ScheduleResponse.class,
        Classroom.class,
        Subject.class,
        Teacher.class,
        ActivityLogEvent.class,
        TeacherInfo.class,
        ClassroomSchedulesInfo.class
})
public class ScheduleService implements IScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final ClassroomRepository classroomRepository;
    private final SubjectRepository subjectRepository;
    private final TeacherRepository teacherRepository;
    private final ScheduleMapper scheduleMapper;
    private final ScheduleValidator scheduleValidator;
    private final ApplicationEventPublisher eventPublisher;


    @Override
    @Transactional
    public ApiResponse<ScheduleResponse> create(ScheduleRequest payload) {
        log.info("Creating new schedule for classroom: {}", payload.getClassroomId());

        scheduleValidator.validateScheduleRequest(payload);

        if (hasScheduleConflict(payload, null)) {
            throw new ScheduleConflictException("Schedule conflict detected");
        }

        Classroom classroom = classroomRepository.findById(payload.getClassroomId())
                .orElseThrow(() -> new DataNotFoundException("Classroom not found"));

        Subject subject = subjectRepository.findById(payload.getSubjectId())
                .orElseThrow(() -> new DataNotFoundException("Subject not found"));

        Teacher teacher = teacherRepository.findById(payload.getTeacherId())
                .orElseThrow(() -> new DataNotFoundException("Teacher not found"));

        Schedule schedule = new Schedule();
        schedule.setClassroom(classroom);
        schedule.setSubject(subject);
        schedule.setTeacher(teacher);
        schedule.setDayOfWeek(payload.getDayOfWeek());
        schedule.setStartTime(payload.getStartTime());
        schedule.setEndTime(payload.getEndTime());
        schedule.setRoom(payload.getRoom());
        schedule.setAcademicYear(payload.getAcademicYear());
        schedule.setActive(true);

        Schedule savedSchedule = scheduleRepository.save(schedule);
        log.info("Schedule created successfully with id: {}", savedSchedule.getId());

        var logActivity = ActivityLogEvent.builder()
                .activityType(ActivityType.CREATE_SCHEDULE)
                .entityId(schedule.getId())
                .entityName(Schedule.class.getName())
                .description("create new schedule")
                .ipAddress(payload.getClientIp())
                .userAgent(payload.getUserAgent())
                .build();
        logActivity(logActivity);

        var response = scheduleMapper.toDto(savedSchedule);
        getTeacherInfo(response.getTeacherId())
                .ifPresent(it -> response.setTeacherName(it.getFullName()));
        return ApiResponse.setResponse(response, "Schedule created successfully", 201);
    }

    private Optional<TeacherInfo> getTeacherInfo(Long teacherId) {
        return teacherRepository.getTeacherInfo(teacherId);
    }

    @Override
    @Transactional
    public ApiResponse<ScheduleResponse> update(ScheduleRequest payload) {
        log.info("Updating schedule with id: {}", payload.getScheduleId());

        Schedule existingSchedule = findScheduleById(payload.getScheduleId());
        scheduleValidator.validateScheduleRequest(payload);

        if (hasScheduleConflict(payload, payload.getScheduleId())) {
            throw new ScheduleConflictException("Schedule conflict detected");
        }

        updateScheduleFields(existingSchedule, payload);

        Schedule updatedSchedule = scheduleRepository.save(existingSchedule);
        log.info("Schedule updated successfully with id: {}", updatedSchedule.getId());


        var response = scheduleMapper.toDto(updatedSchedule);
        getTeacherInfo(response.getTeacherId())
                .ifPresent(it -> response.setTeacherName(it.getFullName()));

        var logActivity = ActivityLogEvent.builder()
                .activityType(ActivityType.UPDATE_SCHEDULE)
                .entityId(updatedSchedule.getId())
                .entityName(Schedule.class.getName())
                .description("update schedule")
                .ipAddress(payload.getClientIp())
                .userAgent(payload.getUserAgent())
                .build();
        logActivity(logActivity);
        return ApiResponse.setSuccess(response);
    }

    @Override
    public ApiResponse<ScheduleResponse> findById(Long id) {
        var response = scheduleMapper.toDto(findScheduleById(id));
        getTeacherInfo(response.getTeacherId())
                .ifPresent(it -> response.setTeacherName(it.getFullName()));
        return ApiResponse.setSuccess(response);
    }

    @Override
    public ApiResponse<PagedResponse<ScheduleResponse>> findAll(Pageable pageable) {
        Page<ScheduleResponse> page = scheduleRepository.findAll(pageable)
                .map(scheduleMapper::toDto);
        List<Long> teacherIds = page.getContent().parallelStream()
                .map(ScheduleResponse::getTeacherId)
                .toList();
        Map<Long, String> teacherName = teacherRepository.findTeacherInfoByIdIn(teacherIds)
                .stream()
                .collect(Collectors.toMap(TeacherInfo::getId, TeacherInfo::getFullName));
        page.getContent().forEach(it -> it.setTeacherName(teacherName.get(it.getTeacherId())));
        return ApiResponse.setSuccess(PagedResponse.from(page));
    }

    @Override
    @Transactional
    @Modifying
    public void delete(Long id) {
        log.info("Hard deleting schedule with id: {}", id);
        Schedule schedule = findScheduleById(id);
        scheduleRepository.delete(schedule);

        var logActivity = ActivityLogEvent.builder()
                .activityType(ActivityType.DELETE_SCHEDULE)
                .entityId(id)
                .entityName(Schedule.class.getName())
                .description("delete schedule")
                .build();
        logActivity(logActivity);
        log.info("Schedule deleted successfully with id: {}", id);
    }

    @Override
    @Transactional
    public void softDelete(Long id) {
        log.info("Soft deleting schedule with id: {}", id);
        Schedule schedule = findScheduleById(id);
        schedule.setActive(false);
        scheduleRepository.save(schedule);

        var logActivity = ActivityLogEvent.builder()
                .activityType(ActivityType.DELETE_SCHEDULE)
                .entityId(id)
                .entityName(Schedule.class.getName())
                .description("soft delete schedule")
                .build();
        logActivity(logActivity);
        log.info("Schedule soft deleted successfully with id: {}", id);
    }

    @Override
    public ApiResponse<ClassroomSchedulesInfo> findByClassroomId(Long classroomId) {
        log.debug("Finding schedules by classroom id: {}", classroomId);
        List<ScheduleResponse> schedules = scheduleRepository.findByClassroomIdAndActiveTrue(classroomId)
                .parallelStream()
                .map(scheduleMapper::toDto)
                .toList();
        setTeacherName(schedules);
        Map<DayOfWeek, List<ScheduleResponse>> grouped = schedules.parallelStream()
                .collect(Collectors.groupingBy(
                        ScheduleResponse::getDayOfWeek,
                        LinkedHashMap::new,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> list.stream()
                                        .sorted(Comparator.comparing(ScheduleResponse::getStartTime))
                                        .toList()
                        )
                ));
        return ApiResponse.setSuccess(ClassroomSchedulesInfo.builder()
                .classroomId(classroomId)
                .classroomName(schedules.stream().findAny().map(ScheduleResponse::getClassroomName).orElse(""))
                .schedules(grouped)
                .build());
    }

    private void setTeacherName(List<ScheduleResponse> scheduleResponses) {
        List<Long> teacherIds = scheduleResponses.parallelStream()
                .map(ScheduleResponse::getTeacherId)
                .toList();
        Map<Long, String> teacherName = teacherRepository.findTeacherInfoByIdIn(teacherIds)
                .stream()
                .collect(Collectors.toMap(TeacherInfo::getId, TeacherInfo::getFullName));
        scheduleResponses.forEach(it -> it.setTeacherName(teacherName.get(it.getTeacherId())));
    }


    @Override
    public ApiResponse<List<ScheduleResponse>> findByTeacherId(Long teacherId) {
        log.debug("Finding schedules by teacher id: {}", teacherId);
        List<ScheduleResponse> schedules = scheduleRepository.findByTeacherIdAndActiveTrue(teacherId)
                .parallelStream()
                .map(scheduleMapper::toDto)
                .toList();
        setTeacherName(schedules);
        return ApiResponse.setSuccess(schedules);
    }

    @Override
    public ApiResponse<List<ScheduleResponse>> findByClassroomAndDay(Long classroomId, DayOfWeek dayOfWeek) {
        log.debug("Finding schedules by classroom: {} and day: {}", classroomId, dayOfWeek);
        List<Schedule> schedules = scheduleRepository.findByClassroomIdAndDayOfWeekAndActiveTrue(
                classroomId, dayOfWeek);
        var scheduleResponses = scheduleMapper.toDtoList(schedules);
        setTeacherName(scheduleResponses);
        return ApiResponse.setSuccess(scheduleResponses);
    }

    @Override
    public ApiResponse<List<ScheduleResponse>> findByTeacherAndDay(Long teacherId, DayOfWeek dayOfWeek) {
        log.debug("Finding schedules by teacher: {} and day: {}", teacherId, dayOfWeek);
        List<Schedule> schedules = scheduleRepository.findByTeacherIdAndDayOfWeekAndActiveTrue(
                teacherId, dayOfWeek);
        var scheduleResponses = scheduleMapper.toDtoList(schedules);
        setTeacherName(scheduleResponses);
        return ApiResponse.setSuccess(scheduleResponses);
    }

    @Override
    public ApiResponse<List<ScheduleResponse>> findByAcademicYear(String academicYear) {
        log.debug("Finding schedules by academic year: {}", academicYear);
        List<Schedule> schedules = scheduleRepository.findByAcademicYearAndActiveTrue(academicYear);
        var scheduleResponses = scheduleMapper.toDtoList(schedules);
        setTeacherName(scheduleResponses);
        return ApiResponse.setSuccess(scheduleResponses);
    }

    @Override
    public boolean hasScheduleConflict(ScheduleRequest requestDto, Long excludeScheduleId) {
        return scheduleValidator.hasTimeConflict(requestDto, excludeScheduleId);
    }

    private Schedule findScheduleById(Long id) {
        return scheduleRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Schedule not found with id: " + id));
    }


    private void updateScheduleFields(Schedule schedule, ScheduleRequest requestDto) {
        if (requestDto.getClassroomId() != null) {
            Classroom classroom = classroomRepository.findById(requestDto.getClassroomId())
                    .orElseThrow(() -> new DataNotFoundException("Classroom not found"));
            schedule.setClassroom(classroom);
        }

        if (requestDto.getSubjectId() != null) {
            Subject subject = subjectRepository.findById(requestDto.getSubjectId())
                    .orElseThrow(() -> new DataNotFoundException("Subject not found"));
            schedule.setSubject(subject);
        }

        if (requestDto.getTeacherId() != null) {
            Teacher teacher = teacherRepository.findById(requestDto.getTeacherId())
                    .orElseThrow(() -> new DataNotFoundException("Teacher not found"));
            schedule.setTeacher(teacher);
        }

        if (requestDto.getDayOfWeek() != null) {
            schedule.setDayOfWeek(requestDto.getDayOfWeek());
        }

        if (requestDto.getStartTime() != null) {
            schedule.setStartTime(requestDto.getStartTime());
        }

        if (requestDto.getEndTime() != null) {
            schedule.setEndTime(requestDto.getEndTime());
        }

        if (requestDto.getRoom() != null) {
            schedule.setRoom(requestDto.getRoom());
        }

        if (requestDto.getAcademicYear() != null) {
            schedule.setAcademicYear(requestDto.getAcademicYear());
        }
    }

    private void logActivity(ActivityLogEvent event) {
        eventPublisher.publishEvent(event);
    }

}