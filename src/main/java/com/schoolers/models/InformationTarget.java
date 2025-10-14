package com.schoolers.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.aot.hint.annotation.RegisterReflection;

@Entity
@Table(name = "information_targets")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@RegisterReflection
public class InformationTarget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "information_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Information information;

    @Column(name = "classroom_id")
    private String classroomId;

    @Column(name = "role")
    private String role;

    @Column(name = "user_id")
    private String userId;
}
