package io.github.gracegrc.regulatoryapi.domain.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.gracegrc.regulatoryapi.domain.model.dto.RegulatoryFileRequestDto;
import io.github.gracegrc.regulatoryapi.domain.model.dto.RegulatoryFileResponseDto;
import io.github.gracegrc.regulatoryapi.domain.model.dto.RegulatoryFileUpdateRequestDto;
import io.github.gracegrc.regulatoryapi.domain.model.entity.FileEntity;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RegulatoryFileMapperTest {

	private static final UUID ID = UUID.fromString("c4d5e6f7-1a2b-4c3d-8e9f-0a1b2c3d4e50");

	private static final UUID FRAMEWORK_ID = UUID.fromString("8b1e4c2a-5f3d-4a7e-b9c6-1d2e3f4a5b60");

	private static final Instant CREATED_AT = Instant.parse("2026-10-01T10:15:30Z");

	private static final LocalDate PUBLISHED = LocalDate.parse("2024-07-09");

	@Test
	void toEntityFromRequestHasNoIdAndUsesTheGivenCreatedAt() {
		RegulatoryFileRequestDto request = new RegulatoryFileRequestDto(FRAMEWORK_ID, "CRR consolidated text",
				"https://example.org/crr.pdf", "2024-07", PUBLISHED, "admin-console");

		FileEntity entity = RegulatoryFileMapper.toEntity(request, CREATED_AT);

		assertThat(entity).isEqualTo(new FileEntity(null, FRAMEWORK_ID, "CRR consolidated text",
				"https://example.org/crr.pdf", "2024-07", PUBLISHED, CREATED_AT, "admin-console"));
	}

	@Test
	void toEntityFromUpdateKeepsIdFrameworkCreatedAtAndCreatedBy() {
		FileEntity existing = new FileEntity(ID, FRAMEWORK_ID, "CRR", null, null, null, CREATED_AT, "admin-console");
		RegulatoryFileUpdateRequestDto request = new RegulatoryFileUpdateRequestDto("CRR consolidated text",
				"https://example.org/crr.pdf", "2024-07", PUBLISHED);

		FileEntity entity = RegulatoryFileMapper.toEntity(existing, request);

		assertThat(entity).isEqualTo(new FileEntity(ID, FRAMEWORK_ID, "CRR consolidated text",
				"https://example.org/crr.pdf", "2024-07", PUBLISHED, CREATED_AT, "admin-console"));
	}

	@Test
	void toResponseDtoCopiesEveryField() {
		FileEntity entity = new FileEntity(ID, FRAMEWORK_ID, "CRR consolidated text", "https://example.org/crr.pdf",
				"2024-07", PUBLISHED, CREATED_AT, "admin-console");

		RegulatoryFileResponseDto response = RegulatoryFileMapper.toResponseDto(entity);

		assertThat(response).isEqualTo(new RegulatoryFileResponseDto(ID, FRAMEWORK_ID, "CRR consolidated text",
				"https://example.org/crr.pdf", "2024-07", PUBLISHED, CREATED_AT, "admin-console"));
	}

}
