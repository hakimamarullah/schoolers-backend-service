package com.schoolers.service.impl;

import com.schoolers.dto.ApiResponse;
import com.schoolers.dto.request.CreateMenuCategoryRequest;
import com.schoolers.dto.request.CreateMenuItemRequest;
import com.schoolers.dto.response.MenuCatResponse;
import com.schoolers.dto.response.MenuItemResponse;
import com.schoolers.models.MenuCategory;
import com.schoolers.models.MenuItem;
import com.schoolers.repository.MenuCatRepository;
import com.schoolers.repository.MenuItemRepository;
import com.schoolers.service.IMenuService;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@RegisterReflectionForBinding({
        MenuItem.class,
        MenuItemResponse.class,
        MenuCatResponse.class,
        CreateMenuItemRequest.class,
        CreateMenuCategoryRequest.class,
})
public class MenuService implements IMenuService {

    private final MenuItemRepository menuItemRepository;
    private final MenuCatRepository menuCatRepository;
    private final EntityManager entityManager;

    @Override
    public List<MenuItemResponse> getMenuItemsByCatName(String catName) {
        return menuItemRepository.findAllByMenuCategoryNameIgnoreCaseOrderByOrdinalDesc(catName)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    @Override
    public ApiResponse<List<MenuCatResponse>> createMenuCatBatch(List<CreateMenuCategoryRequest> payload) {
        var menuCategories = payload.stream().map(it -> MenuCategory.builder()
                .name(it.getName().toUpperCase().trim()).build())
                .toList();
        var response = menuCatRepository.saveAllAndFlush(menuCategories)
                .stream()
                .map(it -> MenuCatResponse.builder()
                        .createdBy(it.getCreatedBy())
                        .createdDate(it.getCreatedDate())
                        .name(it.getName())
                        .id(it.getId())
                        .build())
                .toList();
        return ApiResponse.setResponse(response, 201);
    }

    @Transactional
    @Override
    public ApiResponse<List<MenuItemResponse>> createMenuItemsBatch(List<CreateMenuItemRequest> payload) {
        var menuItems = payload.stream()
                .map(it -> MenuItem.builder()
                        .menuCategory(entityManager.getReference(MenuCategory.class, it.getCategoryId()))
                        .title(it.getTitle().trim())
                        .badgeText(Optional.ofNullable(it.getBadgeText()).map(String::trim).orElse(null))
                        .icon(Optional.ofNullable(it.getIcon()).map(String::trim).orElse(null))
                        .enabled(it.getEnabled())
                        .ordinal(it.getOrdinal())
                        .target(Optional.ofNullable(it.getTarget()).map(String::trim).orElse(null))
                        .build()
                )
                .toList();
        var response = menuItemRepository.saveAllAndFlush(menuItems)
                .stream()
                .map(it -> MenuItemResponse.builder()
                        .route(it.getTarget())
                        .isEnabled(it.isEnabled())
                        .title(it.getTitle())
                        .id(it.getId())
                        .iconName(it.getIcon())
                        .badgeText(it.getBadgeText())
                        .build()

                )
                .toList();
        return ApiResponse.setResponse(response, 201);
    }


    private MenuItemResponse toDto(MenuItem menuItem) {
        return MenuItemResponse.builder()
                .id(menuItem.getId())
                .title(menuItem.getTitle())
                .route(menuItem.getTarget())
                .iconName(menuItem.getIcon())
                .isEnabled(menuItem.isEnabled())
                .build();
    }
}
