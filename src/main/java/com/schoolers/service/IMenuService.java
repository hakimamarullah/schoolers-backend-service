package com.schoolers.service;

import com.schoolers.dto.ApiResponse;
import com.schoolers.dto.request.CreateMenuCategoryRequest;
import com.schoolers.dto.request.CreateMenuItemRequest;
import com.schoolers.dto.response.MenuCatResponse;
import com.schoolers.dto.response.MenuItemResponse;

import java.util.List;

public interface IMenuService {

    List<MenuItemResponse> getMenuItemsByCatName(String catName);

    ApiResponse<List<MenuCatResponse>> createMenuCatBatch(List<CreateMenuCategoryRequest> payload);

    ApiResponse<List<MenuItemResponse>> createMenuItemsBatch(List<CreateMenuItemRequest> payload);
}
