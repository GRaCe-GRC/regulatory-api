package io.github.gracegrc.regulatoryapi.domain.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegulatoryRequirementUpdateRequestDto(
		@NotBlank @Size(max = 255) String provisionRef,
		@Size(max = 255) String heading,
		String text) {
}
