package com.schoolers.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.aot.hint.annotation.RegisterReflection;

import java.util.Set;

@Entity
@Table(name = "menu_category", indexes = {
        @Index(name = "mc_name_idx", columnList = "cat_name")

})
@Setter
@Getter
@RegisterReflection
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class MenuCategory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cat_name", unique = true, nullable = false)
    private String name;


    @OneToMany(fetch = FetchType.LAZY, orphanRemoval = true, mappedBy = "menuCategory")
    private Set<MenuItem> menuItems;
}
