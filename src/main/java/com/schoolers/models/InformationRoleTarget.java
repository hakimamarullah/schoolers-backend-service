package com.schoolers.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.aot.hint.annotation.RegisterReflection;

@Entity
@Table(name = "information_role_targets",
        indexes = @Index(name = "idx_role_targets", columnList = "information_id, role"))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@RegisterReflection
public class InformationRoleTarget {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "information_id", nullable = false)
    private Information information;

    @Column(name = "role", nullable = false, length = 50)
    private String role;
}
