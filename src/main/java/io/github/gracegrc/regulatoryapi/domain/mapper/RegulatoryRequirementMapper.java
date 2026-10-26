package io.github.gracegrc.regulatoryapi.domain.mapper;

import io.github.gracegrc.regulatoryapi.domain.model.dto.RegulatoryRequirementRequestDto;
import io.github.gracegrc.regulatoryapi.domain.model.dto.RegulatoryRequirementResponseDto;
import io.github.gracegrc.regulatoryapi.domain.model.dto.RegulatoryRequirementUpdateRequestDto;
import io.github.gracegrc.regulatoryapi.domain.model.entity.RequirementEntity;
import java.time.Instant;

public final class RegulatoryRequirementMapper {

	private RegulatoryRequirementMapper() {
	}

	public static RequirementEntity toEntity(RegulatoryRequirementRequestDto request, Instant createdAt) {
		return new RequirementEntity(null, request.fileId(), request.parentRequirementId(), request.provisionRef(),
				request.heading(), request.text(), createdAt, request.createdBy());
	}

	public static RequirementEntity toEntity(RequirementEntity existing, RegulatoryRequirementUpdateRequestDto request) {
		return new RequirementEntity(existing.id(), existing.fileId(), existing.parentRequirementId(),
				request.provisionRef(), request.heading(), request.text(), existing.createdAt(), existing.createdBy());
	}

	public static RegulatoryRequirementResponseDto toResponseDto(RequirementEntity entity) {
		return new RegulatoryRequirementResponseDto(entity.id(), entity.fileId(), entity.parentRequirementId(),
				entity.provisionRef(), entity.heading(), entity.text(), entity.createdAt(), entity.createdBy());
	}

}
