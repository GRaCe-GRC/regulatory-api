package io.github.gracegrc.regulatoryapi.domain.service;

import io.github.gracegrc.regulatoryapi.domain.model.dto.RegulatoryFrameworkRequestDto;
import io.github.gracegrc.regulatoryapi.domain.model.dto.RegulatoryFrameworkResponseDto;
import io.github.gracegrc.regulatoryapi.domain.model.dto.RegulatoryFrameworkUpdateRequestDto;
import java.util.List;
import java.util.UUID;

public interface FrameworkService {

	UUID create(RegulatoryFrameworkRequestDto request);

	List<RegulatoryFrameworkResponseDto> findAll(UUID authorityId);

	RegulatoryFrameworkResponseDto findById(UUID id);

	RegulatoryFrameworkResponseDto update(UUID id, RegulatoryFrameworkUpdateRequestDto request);

	void delete(UUID id);

}
