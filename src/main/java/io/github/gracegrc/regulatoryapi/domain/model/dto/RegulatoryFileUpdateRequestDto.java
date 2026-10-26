package io.github.gracegrc.regulatoryapi.domain.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record RegulatoryFileUpdateRequestDto(
		@NotBlank @Size(max = 255) String title,
		String sourceUrl,
		@Size(max = 255) String versionLabel,
		LocalDate publicationDate) {
}
