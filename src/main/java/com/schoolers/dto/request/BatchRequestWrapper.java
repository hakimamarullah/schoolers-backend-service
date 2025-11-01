package com.schoolers.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class BatchRequestWrapper<T> {

    @Size(max = 100)
    @Valid
    private List<T> data;
}
