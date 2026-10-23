package io.github.gracegrc.regulatoryapi.domain.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.gracegrc.regulatoryapi.domain.model.dto.RegulatoryAuthorityRequestDto;
import io.github.gracegrc.regulatoryapi.domain.model.dto.RegulatoryAuthorityResponseDto;
import io.github.gracegrc.regulatoryapi.domain.model.dto.RegulatoryAuthorityUpdateRequestDto;
import io.github.gracegrc.regulatoryapi.domain.model.entity.AuthorityEntity;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RegulatoryAuthorityMapperTest {

	private static final UUID ID = UUID.fromString("3f2c1a9e-7d4b-4e8a-9c1f-2b6d5e8a7c40");

	private static final Instant CREATED_AT = Instant.parse("2026-10-01T10:15:30Z");

	@Test
	void toEntityFromRequestHasNoIdAndUsesTheGivenCreatedAtAndEnabled() {
		RegulatoryAuthorityRequestDto request = new RegulatoryAuthorityRequestDto("European Banking Authority", "EU",
				"admin-console");

		AuthorityEntity entity = RegulatoryAuthorityMapper.toEntity(request, CREATED_AT, true);

		assertThat(entity).isEqualTo(
				new AuthorityEntity(null, "European Banking Authority", "EU", CREATED_AT, true, "admin-console"));
	}

	@Test
	void toEntityFromUpdateKeepsIdCreatedAtAndCreatedBy() {
		AuthorityEntity existing = new AuthorityEntity(ID, "EBA", "EU", CREATED_AT, true, "admin-console");
		RegulatoryAuthorityUpdateRequestDto request = new RegulatoryAuthorityUpdateRequestDto(
				"European Banking Authority", "European Union", false);

		AuthorityEntity entity = RegulatoryAuthorityMapper.toEntity(existing, request);

		assertThat(entity).isEqualTo(new AuthorityEntity(ID, "European Banking Authority", "European Union",
				CREATED_AT, false, "admin-console"));
	}

	@Test
	void toResponseDtoCopiesEveryField() {
		AuthorityEntity entity = new AuthorityEntity(ID, "EBA", "EU", CREATED_AT, true, "admin-console");

		RegulatoryAuthorityResponseDto response = RegulatoryAuthorityMapper.toResponseDto(entity);

		assertThat(response)
			.isEqualTo(new RegulatoryAuthorityResponseDto(ID, "EBA", "EU", CREATED_AT, true, "admin-console"));
	}

}
