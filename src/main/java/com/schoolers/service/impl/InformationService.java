package com.schoolers.service.impl;

import com.schoolers.dto.event.NewInformationEvent;
import com.schoolers.dto.projection.InformationDTO;
import com.schoolers.dto.request.CreateInformationRequest;
import com.schoolers.dto.response.InformationDetailResponse;
import com.schoolers.dto.response.InformationSimpleResponse;
import com.schoolers.exceptions.DataNotFoundException;
import com.schoolers.models.Information;
import com.schoolers.models.InformationClassroomTarget;
import com.schoolers.models.InformationRead;
import com.schoolers.models.InformationReadId;
import com.schoolers.models.InformationRoleTarget;
import com.schoolers.models.InformationUserTarget;
import com.schoolers.models.User;
import com.schoolers.repository.InformationReadRepository;
import com.schoolers.repository.InformationRepository;
import com.schoolers.repository.UserRepository;
import com.schoolers.service.ILocalizationService;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@RegisterReflectionForBinding({
        InformationSimpleResponse.class,
        InformationDetailResponse.class,
        InformationDTO.class,
        Information.class,
        InformationRead.class,
        InformationReadId.class,
        InformationUserTarget.class,
        InformationClassroomTarget.class,
        InformationRoleTarget.class,

})
public class InformationService {

    public static final String DD_MMM_YYYY_HH_MM_SS = "dd MMM yyyy HH:mm:ss";
    private final InformationRepository informationRepository;
    private final InformationReadRepository informationReadRepository;
    private final UserRepository userRepository;
    private final ILocalizationService localizationService;
    private final EntityManager entityManager;
    private final ApplicationEventPublisher eventPublisher;
    /**
     * Create new information with targets
     */
    @Transactional
    public InformationSimpleResponse createInformation(CreateInformationRequest request, String authorId) {
        User author = userRepository.findByLoginId(authorId)
                .orElseThrow(() -> new DataNotFoundException(localizationService.getMessage("auth.user-not-found")));

        Information information = Information.builder()
                .title(request.getTitle())
                .body(request.getBody())
                .bannerUri(request.getBannerUri())
                .author(author)
                .build();

        // Add user targets
        if (request.getTargetUserIds() != null) {
            for (Long userId : request.getTargetUserIds()) {
                InformationUserTarget target = InformationUserTarget.builder()
                        .information(information)
                        .userId(userId)
                        .build();
                information.getUserTargets().add(target);
            }
        }

        // Add classroom targets
        if (request.getTargetClassroomIds() != null) {
            for (String classroomId : request.getTargetClassroomIds()) {
                InformationClassroomTarget target = InformationClassroomTarget.builder()
                        .information(information)
                        .classroomId(classroomId)
                        .build();
                information.getClassroomTargets().add(target);
            }
        }

        // Add role targets
        if (request.getTargetRoles() != null) {
            for (String role : request.getTargetRoles()) {
                InformationRoleTarget target = InformationRoleTarget.builder()
                        .information(information)
                        .role(role)
                        .build();
                information.getRoleTargets().add(target);
            }
        }

        var info = informationRepository.save(information);
        var formatter = DateTimeFormatter.ofPattern(DD_MMM_YYYY_HH_MM_SS, LocaleContextHolder.getLocale())
                .withZone(ZoneId.systemDefault());

        eventPublisher.publishEvent(new NewInformationEvent(request, info.getId(), information.getTitle()));
        return InformationSimpleResponse.builder()
                .authorName(info.getAuthor().getFullName())
                .bannerUri(info.getBannerUri())
                .body(info.getBody())
                .title(info.getTitle())
                .createdAt(formatter.format(info.getCreatedDate()))
                .id(info.getId())
                .build();
    }

    /**
     * Get paginated information list for a user
     */
    @Transactional(readOnly = true)
    public Page<InformationSimpleResponse> getInformationList(
            Long userId, String classroomId, String role, Pageable pageable) {

        // Handle null values for query
        String safeClassroomId = classroomId != null ? classroomId : "";
        String safeRole = role != null ? role : "";

        Page<InformationDTO> projections = informationRepository.findAllForUser(
                userId, safeClassroomId, safeRole, pageable
        );

        var formatter = DateTimeFormatter.ofPattern(DD_MMM_YYYY_HH_MM_SS, LocaleContextHolder.getLocale())
                .withZone(ZoneId.systemDefault());
        return projections.map(proj -> InformationSimpleResponse.builder()
                .id(proj.getId())
                .title(proj.getTitle())
                .body(proj.getBody())
                .bannerUri(proj.getBannerUri())
                .createdAt(formatter.format(proj.getCreatedAt()))
                .authorName(proj.getAuthorName())
                .hasRead(proj.getHasRead())
                .build());
    }

    /**
     * Get information detail
     */
    @Transactional(readOnly = true)
    public InformationDetailResponse getInformationDetail(Long informationId, Long userId) {
        Information information = informationRepository.findFirstById(informationId)
                .orElseThrow(() -> new DataNotFoundException(getNotFoundMessage(informationId)));

        // Check if user has read this information
        boolean hasRead = informationReadRepository.existsById(new InformationReadId(informationId, userId));

        User author = information.getAuthor();

        var formatter = DateTimeFormatter.ofPattern(DD_MMM_YYYY_HH_MM_SS, LocaleContextHolder.getLocale())
                .withZone(ZoneId.systemDefault());
        return InformationDetailResponse.builder()
                .id(information.getId())
                .title(information.getTitle())
                .body(information.getBody())
                .bannerUri(information.getBannerUri())
                .createdAt(formatter.format(information.getCreatedDate()))
                .updatedAt(formatter.format(information.getUpdatedDate()))
                .author(InformationDetailResponse.AuthorDto.builder()
                        .id(author.getLoginId())
                        .name(author.getFullName())
                        .email(author.getEmail())
                        .build())
                .hasRead(hasRead)
                .build();
    }

    /**
     * Mark information as read
     */
    @Transactional
    public void markAsRead(Long informationId, Long userId) {
        if (informationReadRepository.existsById(new InformationReadId(informationId, userId))) {
            return;
        }

        InformationRead read = InformationRead.builder()
                .id(new InformationReadId(informationId, userId))
                .information(entityManager.getReference(Information.class, informationId))
                .user(entityManager.getReference(User.class, userId))
                .build();

        informationReadRepository.save(read);
    }

    /**
     * Check if user has access to information
     */
    @Transactional(readOnly = true)
    public boolean notHasAccess(Long informationId, Long userId, String classroomId, String role) {
        boolean hasUserAccess = informationRepository.countByIdAndUserTargetsUserId(informationId, userId) > 0;
        boolean hasClassroomAccess = informationRepository.countByIdAndClassroomTargetsClassroomId(informationId, classroomId) > 0;
        boolean hasRoleAccess = informationRepository.countByIdAndRoleTargetsRole(informationId, role) > 0;

        return !hasUserAccess && !hasClassroomAccess && !hasRoleAccess;
    }


    public Long countUnreadInformation(Long userId, String classroomId, String role) {
        return informationRepository.countUnreadForUser(userId, classroomId, role);
    }

    @Transactional
    @Modifying
    public void deleteAll() {
        informationRepository.deleteAll();
    }

    private String getNotFoundMessage(Long informationId) {
        return localizationService.getMessage("information.not-found", new Object[]{informationId});
    }
}
