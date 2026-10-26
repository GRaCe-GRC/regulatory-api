package io.github.gracegrc.regulatoryapi.domain.model.dto;

import java.time.Instant;
import java.util.UUID;

public record RegulatoryRequirementResponseDto(
		UUID id,
		UUID fileId,
		UUID parentRequirementId,
		String provisionRef,
		String heading,
		String text,
		Instant createdAt,
		String createdBy) {
}
