package io.github.gracegrc.regulatoryapi.domain.controller;

import io.github.gracegrc.regulatoryapi.domain.model.dto.RegulatoryAuthorityRequestDto;
import io.github.gracegrc.regulatoryapi.domain.model.dto.RegulatoryAuthorityResponseDto;
import io.github.gracegrc.regulatoryapi.domain.model.dto.RegulatoryAuthorityUpdateRequestDto;
import io.github.gracegrc.regulatoryapi.domain.model.dto.RegulatoryFileRequestDto;
import io.github.gracegrc.regulatoryapi.domain.model.dto.RegulatoryFileResponseDto;
import io.github.gracegrc.regulatoryapi.domain.model.dto.RegulatoryFileUpdateRequestDto;
import io.github.gracegrc.regulatoryapi.domain.model.dto.RegulatoryFrameworkRequestDto;
import io.github.gracegrc.regulatoryapi.domain.model.dto.RegulatoryFrameworkResponseDto;
import io.github.gracegrc.regulatoryapi.domain.model.dto.RegulatoryFrameworkUpdateRequestDto;
import io.github.gracegrc.regulatoryapi.domain.model.dto.RegulatoryRequirementRequestDto;
import io.github.gracegrc.regulatoryapi.domain.model.dto.RegulatoryRequirementResponseDto;
import io.github.gracegrc.regulatoryapi.domain.model.dto.RegulatoryRequirementUpdateRequestDto;
import io.github.gracegrc.regulatoryapi.domain.service.AuthorityService;
import io.github.gracegrc.regulatoryapi.domain.service.FileService;
import io.github.gracegrc.regulatoryapi.domain.service.FrameworkService;
import io.github.gracegrc.regulatoryapi.domain.service.RequirementService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/v1")
public class RegulatoryApiController {

	private final AuthorityService authorityService;

	private final FrameworkService frameworkService;

	private final FileService fileService;

	private final RequirementService requirementService;

	public RegulatoryApiController(AuthorityService authorityService, FrameworkService frameworkService,
			FileService fileService, RequirementService requirementService) {
		this.authorityService = authorityService;
		this.frameworkService = frameworkService;
		this.fileService = fileService;
		this.requirementService = requirementService;
	}

	@PostMapping("/authorities")
	public ResponseEntity<Void> createAuthority(@Valid @RequestBody RegulatoryAuthorityRequestDto request) {
		return created(authorityService.create(request));
	}

	@GetMapping("/authorities")
	public List<RegulatoryAuthorityResponseDto> findAllAuthorities() {
		return authorityService.findAll();
	}

	@GetMapping("/authorities/{id}")
	public RegulatoryAuthorityResponseDto findAuthorityById(@PathVariable UUID id) {
		return authorityService.findById(id);
	}

	@PutMapping("/authorities/{id}")
	public RegulatoryAuthorityResponseDto updateAuthority(@PathVariable UUID id,
			@Valid @RequestBody RegulatoryAuthorityUpdateRequestDto request) {
		return authorityService.update(id, request);
	}

	@DeleteMapping("/authorities/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteAuthority(@PathVariable UUID id) {
		authorityService.delete(id);
	}

	@PostMapping("/frameworks")
	public ResponseEntity<Void> createFramework(@Valid @RequestBody RegulatoryFrameworkRequestDto request) {
		return created(frameworkService.create(request));
	}

	@GetMapping("/frameworks")
	public List<RegulatoryFrameworkResponseDto> findAllFrameworks(@RequestParam(required = false) UUID authorityId) {
		return frameworkService.findAll(authorityId);
	}

	@GetMapping("/frameworks/{id}")
	public RegulatoryFrameworkResponseDto findFrameworkById(@PathVariable UUID id) {
		return frameworkService.findById(id);
	}

	@PutMapping("/frameworks/{id}")
	public RegulatoryFrameworkResponseDto updateFramework(@PathVariable UUID id,
			@Valid @RequestBody RegulatoryFrameworkUpdateRequestDto request) {
		return frameworkService.update(id, request);
	}

	@DeleteMapping("/frameworks/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteFramework(@PathVariable UUID id) {
		frameworkService.delete(id);
	}

	@PostMapping("/files")
	public ResponseEntity<Void> createFile(@Valid @RequestBody RegulatoryFileRequestDto request) {
		return created(fileService.create(request));
	}

	@GetMapping("/files")
	public List<RegulatoryFileResponseDto> findAllFiles(@RequestParam(required = false) UUID frameworkId) {
		return fileService.findAll(frameworkId);
	}

	@GetMapping("/files/{id}")
	public RegulatoryFileResponseDto findFileById(@PathVariable UUID id) {
		return fileService.findById(id);
	}

	@PutMapping("/files/{id}")
	public RegulatoryFileResponseDto updateFile(@PathVariable UUID id,
			@Valid @RequestBody RegulatoryFileUpdateRequestDto request) {
		return fileService.update(id, request);
	}

	@DeleteMapping("/files/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteFile(@PathVariable UUID id) {
		fileService.delete(id);
	}

	@PostMapping("/requirements")
	public ResponseEntity<Void> createRequirement(@Valid @RequestBody RegulatoryRequirementRequestDto request) {
		return created(requirementService.create(request));
	}

	@GetMapping("/requirements")
	public List<RegulatoryRequirementResponseDto> findAllRequirements(@RequestParam(required = false) UUID fileId,
			@RequestParam(required = false) UUID parentId) {
		return requirementService.findAll(fileId, parentId);
	}

	@GetMapping("/requirements/{id}")
	public RegulatoryRequirementResponseDto findRequirementById(@PathVariable UUID id) {
		return requirementService.findById(id);
	}

	@PutMapping("/requirements/{id}")
	public RegulatoryRequirementResponseDto updateRequirement(@PathVariable UUID id,
			@Valid @RequestBody RegulatoryRequirementUpdateRequestDto request) {
		return requirementService.update(id, request);
	}

	@DeleteMapping("/requirements/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteRequirement(@PathVariable UUID id) {
		requirementService.delete(id);
	}

	private static ResponseEntity<Void> created(UUID id) {
		URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(id).toUri();
		return ResponseEntity.created(location).build();
	}

}
