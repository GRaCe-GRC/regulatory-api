package io.github.gracegrc.regulatoryapi.domain.service;

import io.github.gracegrc.regulatoryapi.domain.mapper.RegulatoryFileMapper;
import io.github.gracegrc.regulatoryapi.domain.model.dto.RegulatoryFileRequestDto;
import io.github.gracegrc.regulatoryapi.domain.model.dto.RegulatoryFileResponseDto;
import io.github.gracegrc.regulatoryapi.domain.model.dto.RegulatoryFileUpdateRequestDto;
import io.github.gracegrc.regulatoryapi.domain.model.entity.FileEntity;
import io.github.gracegrc.regulatoryapi.domain.model.exception.FileHasRequirementsException;
import io.github.gracegrc.regulatoryapi.domain.model.exception.FileNotFoundException;
import io.github.gracegrc.regulatoryapi.domain.model.exception.FrameworkNotFoundException;
import io.github.gracegrc.regulatoryapi.domain.repository.FileRepository;
import io.github.gracegrc.regulatoryapi.domain.repository.FrameworkRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class FileServiceImpl implements FileService {

	private final FileRepository fileRepository;

	private final FrameworkRepository frameworkRepository;

	public FileServiceImpl(FileRepository fileRepository, FrameworkRepository frameworkRepository) {
		this.fileRepository = fileRepository;
		this.frameworkRepository = frameworkRepository;
	}

	@Override
	public UUID create(RegulatoryFileRequestDto request) {
		if (!frameworkRepository.existsById(request.frameworkId())) {
			throw new FrameworkNotFoundException(request.frameworkId());
		}
		return fileRepository.save(RegulatoryFileMapper.toEntity(request, Instant.now())).id();
	}

	@Override
	@Transactional(readOnly = true)
	public List<RegulatoryFileResponseDto> findAll(UUID frameworkId) {
		List<FileEntity> files = frameworkId == null ? fileRepository.findAll()
				: fileRepository.findByFrameworkId(frameworkId);
		return files.stream().map(RegulatoryFileMapper::toResponseDto).toList();
	}

	@Override
	@Transactional(readOnly = true)
	public RegulatoryFileResponseDto findById(UUID id) {
		return RegulatoryFileMapper.toResponseDto(findEntity(id));
	}

	@Override
	public RegulatoryFileResponseDto update(UUID id, RegulatoryFileUpdateRequestDto request) {
		FileEntity file = RegulatoryFileMapper.toEntity(findEntity(id), request);
		return RegulatoryFileMapper.toResponseDto(fileRepository.save(file));
	}

	@Override
	public void delete(UUID id) {
		if (!fileRepository.existsById(id)) {
			throw new FileNotFoundException(id);
		}
		try {
			fileRepository.deleteById(id);
		}
		catch (DataIntegrityViolationException ex) {
			throw new FileHasRequirementsException(id);
		}
	}

	private FileEntity findEntity(UUID id) {
		return fileRepository.findById(id).orElseThrow(() -> new FileNotFoundException(id));
	}

}
