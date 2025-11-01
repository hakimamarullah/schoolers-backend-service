package com.schoolers.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.aot.hint.annotation.RegisterReflection;

@Entity
@Table(name = "menu_item")
@Setter
@Getter
@EqualsAndHashCode(callSuper = true)
@RegisterReflection
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class MenuItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    private String title;

    private String icon;

    @Column(name = "badge_text")
    private String badgeText;

    private String target;

    private Integer ordinal;

    private boolean enabled = true;

    @ManyToOne
    @JoinColumn(name = "menu_category_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private MenuCategory menuCategory;
}
