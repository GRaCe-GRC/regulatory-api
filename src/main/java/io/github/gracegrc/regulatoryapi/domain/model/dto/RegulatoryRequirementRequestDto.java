package io.github.gracegrc.regulatoryapi.domain.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record RegulatoryRequirementRequestDto(
		@NotNull UUID fileId,
		UUID parentRequirementId,
		@NotBlank @Size(max = 255) String provisionRef,
		@Size(max = 255) String heading,
		String text,
		@NotBlank @Size(max = 255) String createdBy) {
}
