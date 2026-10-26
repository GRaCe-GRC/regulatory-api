package io.github.gracegrc.regulatoryapi.domain.service;

import io.github.gracegrc.regulatoryapi.domain.mapper.RegulatoryRequirementMapper;
import io.github.gracegrc.regulatoryapi.domain.model.dto.RegulatoryRequirementRequestDto;
import io.github.gracegrc.regulatoryapi.domain.model.dto.RegulatoryRequirementResponseDto;
import io.github.gracegrc.regulatoryapi.domain.model.dto.RegulatoryRequirementUpdateRequestDto;
import io.github.gracegrc.regulatoryapi.domain.model.entity.RequirementEntity;
import io.github.gracegrc.regulatoryapi.domain.model.exception.FileNotFoundException;
import io.github.gracegrc.regulatoryapi.domain.model.exception.ParentRequirementInAnotherFileException;
import io.github.gracegrc.regulatoryapi.domain.model.exception.RequirementAlreadyExistsException;
import io.github.gracegrc.regulatoryapi.domain.model.exception.RequirementHasChildrenException;
import io.github.gracegrc.regulatoryapi.domain.model.exception.RequirementNotFoundException;
import io.github.gracegrc.regulatoryapi.domain.repository.FileRepository;
import io.github.gracegrc.regulatoryapi.domain.repository.RequirementRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RequirementServiceImpl implements RequirementService {

	private final RequirementRepository requirementRepository;

	private final FileRepository fileRepository;

	public RequirementServiceImpl(RequirementRepository requirementRepository, FileRepository fileRepository) {
		this.requirementRepository = requirementRepository;
		this.fileRepository = fileRepository;
	}

	@Override
	public UUID create(RegulatoryRequirementRequestDto request) {
		if (!fileRepository.existsById(request.fileId())) {
			throw new FileNotFoundException(request.fileId());
		}
		if (request.parentRequirementId() != null
				&& !findEntity(request.parentRequirementId()).fileId().equals(request.fileId())) {
			throw new ParentRequirementInAnotherFileException(request.parentRequirementId(), request.fileId());
		}
		return save(RegulatoryRequirementMapper.toEntity(request, Instant.now())).id();
	}

	@Override
	@Transactional(readOnly = true)
	public List<RegulatoryRequirementResponseDto> findAll(UUID fileId, UUID parentId) {
		List<RequirementEntity> requirements;
		if (fileId != null && parentId != null) {
			requirements = requirementRepository.findByFileIdAndParentRequirementId(fileId, parentId);
		}
		else if (fileId != null) {
			requirements = requirementRepository.findByFileId(fileId);
		}
		else if (parentId != null) {
			requirements = requirementRepository.findByParentRequirementId(parentId);
		}
		else {
			requirements = requirementRepository.findAll();
		}
		return requirements.stream().map(RegulatoryRequirementMapper::toResponseDto).toList();
	}

	@Override
	@Transactional(readOnly = true)
	public RegulatoryRequirementResponseDto findById(UUID id) {
		return RegulatoryRequirementMapper.toResponseDto(findEntity(id));
	}

	@Override
	public RegulatoryRequirementResponseDto update(UUID id, RegulatoryRequirementUpdateRequestDto request) {
		RequirementEntity requirement = RegulatoryRequirementMapper.toEntity(findEntity(id), request);
		return RegulatoryRequirementMapper.toResponseDto(save(requirement));
	}

	@Override
	public void delete(UUID id) {
		if (!requirementRepository.existsById(id)) {
			throw new RequirementNotFoundException(id);
		}
		try {
			requirementRepository.deleteById(id);
		}
		catch (DataIntegrityViolationException ex) {
			throw new RequirementHasChildrenException(id);
		}
	}

	private RequirementEntity findEntity(UUID id) {
		return requirementRepository.findById(id).orElseThrow(() -> new RequirementNotFoundException(id));
	}

	private RequirementEntity save(RequirementEntity requirement) {
		try {
			return requirementRepository.save(requirement);
		}
		catch (DuplicateKeyException ex) {
			throw new RequirementAlreadyExistsException(requirement.fileId(), requirement.provisionRef());
		}
	}

}
