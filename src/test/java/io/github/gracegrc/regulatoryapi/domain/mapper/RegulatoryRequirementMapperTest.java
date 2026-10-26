package io.github.gracegrc.regulatoryapi.domain.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.gracegrc.regulatoryapi.domain.model.dto.RegulatoryRequirementRequestDto;
import io.github.gracegrc.regulatoryapi.domain.model.dto.RegulatoryRequirementResponseDto;
import io.github.gracegrc.regulatoryapi.domain.model.dto.RegulatoryRequirementUpdateRequestDto;
import io.github.gracegrc.regulatoryapi.domain.model.entity.RequirementEntity;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RegulatoryRequirementMapperTest {

	private static final UUID ID = UUID.fromString("e1f2a3b4-c5d6-4e7f-8a9b-0c1d2e3f4a50");

	private static final UUID FILE_ID = UUID.fromString("c4d5e6f7-1a2b-4c3d-8e9f-0a1b2c3d4e50");

	private static final UUID PARENT_ID = UUID.fromString("a9b8c7d6-e5f4-4a3b-9c2d-1e0f9a8b7c60");

	private static final Instant CREATED_AT = Instant.parse("2026-10-01T10:15:30Z");

	@Test
	void toEntityFromRequestHasNoIdAndUsesTheGivenCreatedAt() {
		RegulatoryRequirementRequestDto request = new RegulatoryRequirementRequestDto(FILE_ID, PARENT_ID,
				"164.308(a)(4)(i)", "Heading", "Text", "admin-console");

		RequirementEntity entity = RegulatoryRequirementMapper.toEntity(request, CREATED_AT);

		assertThat(entity).isEqualTo(new RequirementEntity(null, FILE_ID, PARENT_ID, "164.308(a)(4)(i)", "Heading",
				"Text", CREATED_AT, "admin-console"));
	}

	@Test
	void toEntityFromUpdateKeepsIdFileParentCreatedAtAndCreatedBy() {
		RequirementEntity existing = new RequirementEntity(ID, FILE_ID, PARENT_ID, "164.308(a)(4)(i)", "Heading",
				"Text", CREATED_AT, "admin-console");
		RegulatoryRequirementUpdateRequestDto request = new RegulatoryRequirementUpdateRequestDto("164.308(a)(4)(ii)",
				"New heading", null);

		RequirementEntity entity = RegulatoryRequirementMapper.toEntity(existing, request);

		assertThat(entity).isEqualTo(new RequirementEntity(ID, FILE_ID, PARENT_ID, "164.308(a)(4)(ii)", "New heading",
				null, CREATED_AT, "admin-console"));
	}

	@Test
	void toResponseDtoCopiesEveryField() {
		RequirementEntity entity = new RequirementEntity(ID, FILE_ID, PARENT_ID, "164.308(a)(4)(i)", "Heading", "Text",
				CREATED_AT, "admin-console");

		RegulatoryRequirementResponseDto response = RegulatoryRequirementMapper.toResponseDto(entity);

		assertThat(response).isEqualTo(new RegulatoryRequirementResponseDto(ID, FILE_ID, PARENT_ID, "164.308(a)(4)(i)",
				"Heading", "Text", CREATED_AT, "admin-console"));
	}

}
