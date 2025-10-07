package com.schoolers.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Objects;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PagedResponse<T> {

    private Integer page;
    private Integer size;
    private Long totalElements;
    private Integer totalPages;
    private List<T> content;

    public static <V> PagedResponse<V> from(Page<V> page) {
        Objects.requireNonNull(page, "Page must not be null");
        return new PagedResponse<>(page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages(), page.getContent());
    }
}
