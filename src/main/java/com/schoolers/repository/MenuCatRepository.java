package com.schoolers.repository;

import com.schoolers.models.MenuCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuCatRepository extends JpaRepository<MenuCategory, Long> {
}
