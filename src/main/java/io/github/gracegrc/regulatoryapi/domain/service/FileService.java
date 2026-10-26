package io.github.gracegrc.regulatoryapi.domain.service;

import io.github.gracegrc.regulatoryapi.domain.model.dto.RegulatoryFileRequestDto;
import io.github.gracegrc.regulatoryapi.domain.model.dto.RegulatoryFileResponseDto;
import io.github.gracegrc.regulatoryapi.domain.model.dto.RegulatoryFileUpdateRequestDto;
import java.util.List;
import java.util.UUID;

public interface FileService {

	UUID create(RegulatoryFileRequestDto request);

	List<RegulatoryFileResponseDto> findAll(UUID frameworkId);

	RegulatoryFileResponseDto findById(UUID id);

	RegulatoryFileResponseDto update(UUID id, RegulatoryFileUpdateRequestDto request);

	void delete(UUID id);

}
