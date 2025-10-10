package com.schoolers.service.impl;

import com.schoolers.dto.ApiResponse;
import com.schoolers.dto.projection.SimpleAttendanceSessionInfo;
import com.schoolers.dto.request.GetAttendanceSessionInfo;
import com.schoolers.dto.response.AttendanceSessionInfo;
import com.schoolers.exceptions.DataNotFoundException;
import com.schoolers.repository.AttendanceSessionRepository;
import com.schoolers.repository.TeacherRepository;
import com.schoolers.service.IAttendanceSessionService;
import com.schoolers.service.ILocalizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@RegisterReflectionForBinding({
        AttendanceSessionInfo.class,
        SimpleAttendanceSessionInfo.class,
        GetAttendanceSessionInfo.class
})
public class AttendanceSessionService implements IAttendanceSessionService {

    private final AttendanceSessionRepository attendanceSessionRepository;
    private final TeacherRepository teacherRepository;
    private final ILocalizationService localizationService;

    @Transactional(readOnly = true)
    @Override
    public ApiResponse<AttendanceSessionInfo> getSessionByClassroomIdAndStatus(GetAttendanceSessionInfo payload) {
        SimpleAttendanceSessionInfo sessionInfo = attendanceSessionRepository.getAllByStatusAndClassroomIdAndSessionDate(payload.getStatus(), payload.getClassroomId(), payload.getSessionDate())
                .stream()
                .findFirst()
                .orElseThrow(() -> new DataNotFoundException(localizationService.getMessage("attendance.session-not-found")));

        return ApiResponse.setSuccess(AttendanceSessionInfo.builder()
                .attendanceSessionId(sessionInfo.getId())
                .topic(sessionInfo.getTopic())
                .startTime(sessionInfo.getStartTime())
                .endTime(sessionInfo.getEndTime())
                .sessionDate(sessionInfo.getSessionDate())
                .subjectName(sessionInfo.getSubjectName())
                .room(sessionInfo.getRoom())
                .teacherName(teacherRepository.getTeacherNameById(sessionInfo.getTeacherId()).orElse(""))
                .build());
    }
}
