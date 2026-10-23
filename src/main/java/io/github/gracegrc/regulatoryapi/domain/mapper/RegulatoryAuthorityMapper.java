package io.github.gracegrc.regulatoryapi.domain.mapper;

import io.github.gracegrc.regulatoryapi.domain.model.dto.RegulatoryAuthorityRequestDto;
import io.github.gracegrc.regulatoryapi.domain.model.dto.RegulatoryAuthorityResponseDto;
import io.github.gracegrc.regulatoryapi.domain.model.dto.RegulatoryAuthorityUpdateRequestDto;
import io.github.gracegrc.regulatoryapi.domain.model.entity.AuthorityEntity;
import java.time.Instant;

public final class RegulatoryAuthorityMapper {

	private RegulatoryAuthorityMapper() {
	}

	public static AuthorityEntity toEntity(RegulatoryAuthorityRequestDto request, Instant createdAt, boolean enabled) {
		return new AuthorityEntity(null, request.name(), request.jurisdiction(), createdAt, enabled, request.createdBy());
	}

	public static AuthorityEntity toEntity(AuthorityEntity existing, RegulatoryAuthorityUpdateRequestDto request) {
		return new AuthorityEntity(existing.id(), request.name(), request.jurisdiction(), existing.createdAt(),
				request.enabled(), existing.createdBy());
	}

	public static RegulatoryAuthorityResponseDto toResponseDto(AuthorityEntity entity) {
		return new RegulatoryAuthorityResponseDto(entity.id(), entity.name(), entity.jurisdiction(), entity.createdAt(),
				entity.enabled(), entity.createdBy());
	}

}
