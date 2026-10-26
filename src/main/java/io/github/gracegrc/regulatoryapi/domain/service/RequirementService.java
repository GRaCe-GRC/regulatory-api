package io.github.gracegrc.regulatoryapi.domain.service;

import io.github.gracegrc.regulatoryapi.domain.model.dto.RegulatoryRequirementRequestDto;
import io.github.gracegrc.regulatoryapi.domain.model.dto.RegulatoryRequirementResponseDto;
import io.github.gracegrc.regulatoryapi.domain.model.dto.RegulatoryRequirementUpdateRequestDto;
import java.util.List;
import java.util.UUID;

public interface RequirementService {

	UUID create(RegulatoryRequirementRequestDto request);

	List<RegulatoryRequirementResponseDto> findAll(UUID fileId, UUID parentId);

	RegulatoryRequirementResponseDto findById(UUID id);

	RegulatoryRequirementResponseDto update(UUID id, RegulatoryRequirementUpdateRequestDto request);

	void delete(UUID id);

}
