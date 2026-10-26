package io.github.gracegrc.regulatoryapi.domain.mapper;

import io.github.gracegrc.regulatoryapi.domain.model.dto.RegulatoryFrameworkRequestDto;
import io.github.gracegrc.regulatoryapi.domain.model.dto.RegulatoryFrameworkResponseDto;
import io.github.gracegrc.regulatoryapi.domain.model.dto.RegulatoryFrameworkUpdateRequestDto;
import io.github.gracegrc.regulatoryapi.domain.model.entity.FrameworkEntity;
import java.time.Instant;

public final class RegulatoryFrameworkMapper {

	private RegulatoryFrameworkMapper() {
	}

	public static FrameworkEntity toEntity(RegulatoryFrameworkRequestDto request, Instant createdAt, boolean enabled) {
		return new FrameworkEntity(null, request.authorityId(), request.name(), request.shortName(), request.citation(),
				enabled, createdAt, request.createdBy());
	}

	public static FrameworkEntity toEntity(FrameworkEntity existing, RegulatoryFrameworkUpdateRequestDto request) {
		return new FrameworkEntity(existing.id(), existing.authorityId(), request.name(), request.shortName(),
				request.citation(), request.enabled(), existing.createdAt(), existing.createdBy());
	}

	public static RegulatoryFrameworkResponseDto toResponseDto(FrameworkEntity entity) {
		return new RegulatoryFrameworkResponseDto(entity.id(), entity.authorityId(), entity.name(), entity.shortName(),
				entity.citation(), entity.enabled(), entity.createdAt(), entity.createdBy());
	}

}
