package io.github.gracegrc.regulatoryapi.domain.model.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record RegulatoryFileResponseDto(
		UUID id,
		UUID frameworkId,
		String title,
		String sourceUrl,
		String versionLabel,
		LocalDate publicationDate,
		Instant createdAt,
		String createdBy) {
}
