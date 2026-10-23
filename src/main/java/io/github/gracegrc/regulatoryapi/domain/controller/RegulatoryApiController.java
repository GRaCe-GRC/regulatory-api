package io.github.gracegrc.regulatoryapi.domain.controller;

import io.github.gracegrc.regulatoryapi.domain.model.dto.RegulatoryAuthorityRequestDto;
import io.github.gracegrc.regulatoryapi.domain.model.dto.RegulatoryAuthorityResponseDto;
import io.github.gracegrc.regulatoryapi.domain.model.dto.RegulatoryAuthorityUpdateRequestDto;
import io.github.gracegrc.regulatoryapi.domain.service.AuthorityService;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/authorities")
public class RegulatoryApiController {

	private final AuthorityService authorityService;

	public RegulatoryApiController(AuthorityService authorityService) {
		this.authorityService = authorityService;
	}

	@PostMapping
	public ResponseEntity<Void> create(@Valid @RequestBody RegulatoryAuthorityRequestDto request) {
		UUID id = authorityService.create(request);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(id).toUri();
		return ResponseEntity.created(location).build();
	}

	@GetMapping
	public List<RegulatoryAuthorityResponseDto> findAll() {
		return authorityService.findAll();
	}

	@GetMapping("/{id}")
	public RegulatoryAuthorityResponseDto findById(@PathVariable UUID id) {
		return authorityService.findById(id);
	}

	@PutMapping("/{id}")
	public RegulatoryAuthorityResponseDto update(@PathVariable UUID id,
			@Valid @RequestBody RegulatoryAuthorityUpdateRequestDto request) {
		return authorityService.update(id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable UUID id) {
		authorityService.delete(id);
	}

}
