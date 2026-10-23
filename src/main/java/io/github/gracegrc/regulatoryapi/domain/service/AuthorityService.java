package io.github.gracegrc.regulatoryapi.domain.service;

import io.github.gracegrc.regulatoryapi.domain.model.dto.RegulatoryAuthorityRequestDto;
import io.github.gracegrc.regulatoryapi.domain.model.dto.RegulatoryAuthorityResponseDto;
import io.github.gracegrc.regulatoryapi.domain.model.dto.RegulatoryAuthorityUpdateRequestDto;
import java.util.List;
import java.util.UUID;

public interface AuthorityService {

	UUID create(RegulatoryAuthorityRequestDto request);

	List<RegulatoryAuthorityResponseDto> findAll();

	RegulatoryAuthorityResponseDto findById(UUID id);

	RegulatoryAuthorityResponseDto update(UUID id, RegulatoryAuthorityUpdateRequestDto request);

	void delete(UUID id);

}
