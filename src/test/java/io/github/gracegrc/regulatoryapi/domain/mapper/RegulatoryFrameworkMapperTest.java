package io.github.gracegrc.regulatoryapi.domain.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.gracegrc.regulatoryapi.domain.model.dto.RegulatoryFrameworkRequestDto;
import io.github.gracegrc.regulatoryapi.domain.model.dto.RegulatoryFrameworkResponseDto;
import io.github.gracegrc.regulatoryapi.domain.model.dto.RegulatoryFrameworkUpdateRequestDto;
import io.github.gracegrc.regulatoryapi.domain.model.entity.FrameworkEntity;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RegulatoryFrameworkMapperTest {

	private static final UUID ID = UUID.fromString("8b1e4c2a-5f3d-4a7e-b9c6-1d2e3f4a5b60");

	private static final UUID AUTHORITY_ID = UUID.fromString("3f2c1a9e-7d4b-4e8a-9c1f-2b6d5e8a7c40");

	private static final Instant CREATED_AT = Instant.parse("2026-10-01T10:15:30Z");

	@Test
	void toEntityFromRequestHasNoIdAndUsesTheGivenCreatedAtAndEnabled() {
		RegulatoryFrameworkRequestDto request = new RegulatoryFrameworkRequestDto(AUTHORITY_ID,
				"Capital Requirements Regulation", "CRR", "Regulation (EU) No 575/2013", "admin-console");

		FrameworkEntity entity = RegulatoryFrameworkMapper.toEntity(request, CREATED_AT, true);

		assertThat(entity).isEqualTo(new FrameworkEntity(null, AUTHORITY_ID, "Capital Requirements Regulation", "CRR",
				"Regulation (EU) No 575/2013", true, CREATED_AT, "admin-console"));
	}

	@Test
	void toEntityFromUpdateKeepsIdAuthorityCreatedAtAndCreatedBy() {
		FrameworkEntity existing = new FrameworkEntity(ID, AUTHORITY_ID, "CRR", null, null, true, CREATED_AT,
				"admin-console");
		RegulatoryFrameworkUpdateRequestDto request = new RegulatoryFrameworkUpdateRequestDto(
				"Capital Requirements Regulation", "CRR", "Regulation (EU) No 575/2013", false);

		FrameworkEntity entity = RegulatoryFrameworkMapper.toEntity(existing, request);

		assertThat(entity).isEqualTo(new FrameworkEntity(ID, AUTHORITY_ID, "Capital Requirements Regulation", "CRR",
				"Regulation (EU) No 575/2013", false, CREATED_AT, "admin-console"));
	}

	@Test
	void toResponseDtoCopiesEveryField() {
		FrameworkEntity entity = new FrameworkEntity(ID, AUTHORITY_ID, "Capital Requirements Regulation", "CRR",
				"Regulation (EU) No 575/2013", true, CREATED_AT, "admin-console");

		RegulatoryFrameworkResponseDto response = RegulatoryFrameworkMapper.toResponseDto(entity);

		assertThat(response).isEqualTo(new RegulatoryFrameworkResponseDto(ID, AUTHORITY_ID,
				"Capital Requirements Regulation", "CRR", "Regulation (EU) No 575/2013", true, CREATED_AT,
				"admin-console"));
	}

}
