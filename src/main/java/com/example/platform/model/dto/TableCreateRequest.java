package com.example.platform.model.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record TableCreateRequest(@NotBlank String name, List<ColumnDef> columns) {
    public record ColumnDef(@NotBlank String name, @NotBlank String type) {}
}
