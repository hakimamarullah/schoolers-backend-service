package com.schoolers.models;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.aot.hint.annotation.RegisterReflection;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "information",
        indexes = {
                @Index(name = "idx_info_created_at", columnList = "created_at DESC"),
                @Index(name = "idx_info_author", columnList = "author_id")
        })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@RegisterReflection
public class Information extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @Column(name = "banner_uri", length = 1000)
    private String bannerUri;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @OneToMany(mappedBy = "information", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<InformationUserTarget> userTargets = new ArrayList<>();

    @OneToMany(mappedBy = "information", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<InformationClassroomTarget> classroomTargets = new ArrayList<>();

    @OneToMany(mappedBy = "information", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<InformationRoleTarget> roleTargets = new ArrayList<>();

    @OneToMany(mappedBy = "information", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<InformationRead> reads = new ArrayList<>();
}