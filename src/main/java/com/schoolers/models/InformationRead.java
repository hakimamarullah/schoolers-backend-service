package com.schoolers.models;



import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.aot.hint.annotation.RegisterReflection;

import java.time.Instant;

@Entity
@Table(name = "information_reads",
        uniqueConstraints = @UniqueConstraint(columnNames = {"information_id", "user_id"}),
        indexes = {
                @Index(name = "idx_info_reads_user", columnList = "user_id"),
                @Index(name = "idx_info_reads_info", columnList = "information_id")
        })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@RegisterReflection
public class InformationRead {

    @EmbeddedId
    private InformationReadId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("informationId")
    @JoinColumn(name = "information_id")
    private Information information;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @CreationTimestamp
    @Column(name = "read_at", nullable = false, updatable = false)
    private Instant readAt;
}

