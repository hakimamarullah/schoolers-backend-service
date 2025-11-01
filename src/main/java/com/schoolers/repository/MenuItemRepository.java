package com.schoolers.repository;

import com.schoolers.models.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {

    List<MenuItem> findAllByMenuCategoryNameIgnoreCaseOrderByOrdinalDesc(String menuCategory);
}
