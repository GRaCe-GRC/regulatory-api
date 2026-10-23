package io.github.gracegrc.regulatoryapi.domain.service;

import io.github.gracegrc.regulatoryapi.domain.mapper.RegulatoryAuthorityMapper;
import io.github.gracegrc.regulatoryapi.domain.model.dto.RegulatoryAuthorityRequestDto;
import io.github.gracegrc.regulatoryapi.domain.model.dto.RegulatoryAuthorityResponseDto;
import io.github.gracegrc.regulatoryapi.domain.model.dto.RegulatoryAuthorityUpdateRequestDto;
import io.github.gracegrc.regulatoryapi.domain.model.entity.AuthorityEntity;
import io.github.gracegrc.regulatoryapi.domain.model.exception.AuthorityAlreadyExistsException;
import io.github.gracegrc.regulatoryapi.domain.model.exception.AuthorityNotFoundException;
import io.github.gracegrc.regulatoryapi.domain.repository.AuthorityRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthorityServiceImpl implements AuthorityService {

	private final AuthorityRepository authorityRepository;

	public AuthorityServiceImpl(AuthorityRepository authorityRepository) {
		this.authorityRepository = authorityRepository;
	}

	@Override
	public UUID create(RegulatoryAuthorityRequestDto request) {
		AuthorityEntity authority = RegulatoryAuthorityMapper.toEntity(request, Instant.now(), true);
		return save(authority).id();
	}

	@Override
	@Transactional(readOnly = true)
	public List<RegulatoryAuthorityResponseDto> findAll() {
		return authorityRepository.findAll().stream().map(RegulatoryAuthorityMapper::toResponseDto).toList();
	}

	@Override
	@Transactional(readOnly = true)
	public RegulatoryAuthorityResponseDto findById(UUID id) {
		return RegulatoryAuthorityMapper.toResponseDto(findEntity(id));
	}

	@Override
	public RegulatoryAuthorityResponseDto update(UUID id, RegulatoryAuthorityUpdateRequestDto request) {
		AuthorityEntity authority = RegulatoryAuthorityMapper.toEntity(findEntity(id), request);
		return RegulatoryAuthorityMapper.toResponseDto(save(authority));
	}

	@Override
	public void delete(UUID id) {
		if (!authorityRepository.existsById(id)) {
			throw new AuthorityNotFoundException(id);
		}
		authorityRepository.deleteById(id);
	}

	private AuthorityEntity findEntity(UUID id) {
		return authorityRepository.findById(id).orElseThrow(() -> new AuthorityNotFoundException(id));
	}

	private AuthorityEntity save(AuthorityEntity authority) {
		try {
			return authorityRepository.save(authority);
		}
		catch (DuplicateKeyException ex) {
			throw new AuthorityAlreadyExistsException(authority.name());
		}
	}

}
