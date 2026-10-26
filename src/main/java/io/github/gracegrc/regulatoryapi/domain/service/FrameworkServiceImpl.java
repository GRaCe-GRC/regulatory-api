package io.github.gracegrc.regulatoryapi.domain.service;

import io.github.gracegrc.regulatoryapi.domain.mapper.RegulatoryFrameworkMapper;
import io.github.gracegrc.regulatoryapi.domain.model.dto.RegulatoryFrameworkRequestDto;
import io.github.gracegrc.regulatoryapi.domain.model.dto.RegulatoryFrameworkResponseDto;
import io.github.gracegrc.regulatoryapi.domain.model.dto.RegulatoryFrameworkUpdateRequestDto;
import io.github.gracegrc.regulatoryapi.domain.model.entity.FrameworkEntity;
import io.github.gracegrc.regulatoryapi.domain.model.exception.AuthorityNotFoundException;
import io.github.gracegrc.regulatoryapi.domain.model.exception.FrameworkAlreadyExistsException;
import io.github.gracegrc.regulatoryapi.domain.model.exception.FrameworkNotFoundException;
import io.github.gracegrc.regulatoryapi.domain.repository.AuthorityRepository;
import io.github.gracegrc.regulatoryapi.domain.repository.FrameworkRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class FrameworkServiceImpl implements FrameworkService {

	private final FrameworkRepository frameworkRepository;

	private final AuthorityRepository authorityRepository;

	public FrameworkServiceImpl(FrameworkRepository frameworkRepository, AuthorityRepository authorityRepository) {
		this.frameworkRepository = frameworkRepository;
		this.authorityRepository = authorityRepository;
	}

	@Override
	public UUID create(RegulatoryFrameworkRequestDto request) {
		if (!authorityRepository.existsById(request.authorityId())) {
			throw new AuthorityNotFoundException(request.authorityId());
		}
		FrameworkEntity framework = RegulatoryFrameworkMapper.toEntity(request, Instant.now(), true);
		return save(framework).id();
	}

	@Override
	@Transactional(readOnly = true)
	public List<RegulatoryFrameworkResponseDto> findAll(UUID authorityId) {
		List<FrameworkEntity> frameworks = authorityId == null ? frameworkRepository.findAll()
				: frameworkRepository.findByAuthorityId(authorityId);
		return frameworks.stream().map(RegulatoryFrameworkMapper::toResponseDto).toList();
	}

	@Override
	@Transactional(readOnly = true)
	public RegulatoryFrameworkResponseDto findById(UUID id) {
		return RegulatoryFrameworkMapper.toResponseDto(findEntity(id));
	}

	@Override
	public RegulatoryFrameworkResponseDto update(UUID id, RegulatoryFrameworkUpdateRequestDto request) {
		FrameworkEntity framework = RegulatoryFrameworkMapper.toEntity(findEntity(id), request);
		return RegulatoryFrameworkMapper.toResponseDto(save(framework));
	}

	@Override
	public void delete(UUID id) {
		if (!frameworkRepository.existsById(id)) {
			throw new FrameworkNotFoundException(id);
		}
		frameworkRepository.deleteById(id);
	}

	private FrameworkEntity findEntity(UUID id) {
		return frameworkRepository.findById(id).orElseThrow(() -> new FrameworkNotFoundException(id));
	}

	private FrameworkEntity save(FrameworkEntity framework) {
		try {
			return frameworkRepository.save(framework);
		}
		catch (DuplicateKeyException ex) {
			throw new FrameworkAlreadyExistsException(framework.authorityId(), framework.name());
		}
	}

}
