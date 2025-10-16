package com.schoolers.service.impl;

import com.schoolers.dto.ApiResponse;
import com.schoolers.dto.event.SessionStartEvent;
import com.schoolers.dto.projection.SimpleAttendanceSessionInfo;
import com.schoolers.dto.request.GetAttendanceSessionInfo;
import com.schoolers.dto.request.StartSessionRequest;
import com.schoolers.dto.response.AttendanceSessionInfo;
import com.schoolers.enums.SessionStatus;
import com.schoolers.exceptions.DataNotFoundException;
import com.schoolers.models.AttendanceSession;
import com.schoolers.repository.AttendanceSessionRepository;
import com.schoolers.repository.TeacherRepository;
import com.schoolers.service.IAttendanceSessionService;
import com.schoolers.service.ILocalizationService;
import com.schoolers.utils.CommonUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@RegisterReflectionForBinding({
        AttendanceSessionInfo.class,
        SimpleAttendanceSessionInfo.class,
        GetAttendanceSessionInfo.class,
        StartSessionRequest.class
})
public class AttendanceSessionService implements IAttendanceSessionService {

    private final AttendanceSessionRepository attendanceSessionRepository;
    private final TeacherRepository teacherRepository;
    private final ILocalizationService localizationService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    @Override
    public ApiResponse<AttendanceSessionInfo> getSessionByClassroomIdAndStatus(GetAttendanceSessionInfo payload) {
        SimpleAttendanceSessionInfo sessionInfo = attendanceSessionRepository.getAllByStatusAndClassroomIdAndSessionDate(payload.getStatus(), payload.getClassroomId(), payload.getSessionDate())
                .stream()
                .findFirst()
                .orElseThrow(() -> new DataNotFoundException(localizationService.getMessage("attendance.session-not-found")));


        return ApiResponse.setSuccess(AttendanceSessionInfo.builder()
                .sessionId(sessionInfo.getId())
                .topic(sessionInfo.getTopic())
                .displayDate(CommonUtils.formatSessionDate(sessionInfo.getSessionDate()))
                .displayTime(CommonUtils.formatSessionTime(sessionInfo.getStartTime(), sessionInfo.getEndTime()))
                .sessionDate(sessionInfo.getSessionDate())
                .subjectName(sessionInfo.getSubjectName())
                .room(sessionInfo.getRoom())
                .teacherName(teacherRepository.getTeacherNameById(sessionInfo.getTeacherId()).orElse(""))
                .build());
    }

    @Transactional
    @Override
    public ApiResponse<String> startSession(StartSessionRequest payload) {
        AttendanceSession attendanceSession = attendanceSessionRepository
                .findByScheduleIdAndTeacherEmployeeNumberAndSessionDateEquals(payload.getScheduleId(),
                        payload.getEmployeeNumber(), LocalDate.now())
                .orElseThrow(() -> new DataNotFoundException("You got no session"));
        if (attendanceSession.getStatus().equals(SessionStatus.ONGOING)) {
            return ApiResponse.setResponse("Session is already started", 200);
        }
        attendanceSession.setStatus(SessionStatus.ONGOING);
        attendanceSession.setTopic(payload.getTopic());
        attendanceSessionRepository.save(attendanceSession);

        eventPublisher.publishEvent(SessionStartEvent.builder()
                .sessionId(attendanceSession.getId())
                .build());
        return ApiResponse.setSuccess("Session started successfully");
    }
}
