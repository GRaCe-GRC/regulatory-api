package io.github.gracegrc.regulatoryapi.domain.model.dto;

import java.time.Instant;
import java.util.UUID;

public record RegulatoryFrameworkResponseDto(
		UUID id,
		UUID authorityId,
		String name,
		String shortName,
		String citation,
		boolean enabled,
		Instant createdAt,
		String createdBy) {
}
