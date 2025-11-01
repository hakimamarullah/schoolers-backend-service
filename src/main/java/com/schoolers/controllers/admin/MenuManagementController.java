package com.schoolers.controllers.admin;

import com.schoolers.annotations.LogRequestResponse;
import com.schoolers.dto.ApiResponse;
import com.schoolers.dto.request.BatchRequestWrapper;
import com.schoolers.dto.request.CreateMenuCategoryRequest;
import com.schoolers.dto.request.CreateMenuItemRequest;
import com.schoolers.dto.response.MenuCatResponse;
import com.schoolers.dto.response.MenuItemResponse;
import com.schoolers.service.IMenuService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/menu")
@LogRequestResponse
@RequiredArgsConstructor
@RolesAllowed({"OFFICE_ADMIN"})
@SecurityRequirement(name = "bearerJWT")
public class MenuManagementController {

    private final IMenuService menuService;


    @PostMapping(value = "/categories", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<List<MenuCatResponse>>> createMenuCatBatch(@RequestBody
                                                                                 @Valid BatchRequestWrapper<CreateMenuCategoryRequest> payload) {
        return menuService.createMenuCatBatch(payload.getData()).toResponseEntity();
    }

    @PostMapping(value = "/items", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<List<MenuItemResponse>>> createMenuItemsBatch(@RequestBody
                                                                                 @Valid BatchRequestWrapper<CreateMenuItemRequest> payload) {
        return menuService.createMenuItemsBatch(payload.getData()).toResponseEntity();
    }
}
