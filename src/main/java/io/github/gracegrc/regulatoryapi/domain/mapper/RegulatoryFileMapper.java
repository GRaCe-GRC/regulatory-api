package io.github.gracegrc.regulatoryapi.domain.mapper;

import io.github.gracegrc.regulatoryapi.domain.model.dto.RegulatoryFileRequestDto;
import io.github.gracegrc.regulatoryapi.domain.model.dto.RegulatoryFileResponseDto;
import io.github.gracegrc.regulatoryapi.domain.model.dto.RegulatoryFileUpdateRequestDto;
import io.github.gracegrc.regulatoryapi.domain.model.entity.FileEntity;
import java.time.Instant;

public final class RegulatoryFileMapper {

	private RegulatoryFileMapper() {
	}

	public static FileEntity toEntity(RegulatoryFileRequestDto request, Instant createdAt) {
		return new FileEntity(null, request.frameworkId(), request.title(), request.sourceUrl(),
				request.versionLabel(), request.publicationDate(), createdAt, request.createdBy());
	}

	public static FileEntity toEntity(FileEntity existing, RegulatoryFileUpdateRequestDto request) {
		return new FileEntity(existing.id(), existing.frameworkId(), request.title(), request.sourceUrl(),
				request.versionLabel(), request.publicationDate(), existing.createdAt(), existing.createdBy());
	}

	public static RegulatoryFileResponseDto toResponseDto(FileEntity entity) {
		return new RegulatoryFileResponseDto(entity.id(), entity.frameworkId(), entity.title(), entity.sourceUrl(),
				entity.versionLabel(), entity.publicationDate(), entity.createdAt(), entity.createdBy());
	}

}
