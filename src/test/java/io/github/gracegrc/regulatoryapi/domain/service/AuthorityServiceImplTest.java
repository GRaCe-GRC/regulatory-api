package io.github.gracegrc.regulatoryapi.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import io.github.gracegrc.regulatoryapi.domain.model.dto.RegulatoryAuthorityRequestDto;
import io.github.gracegrc.regulatoryapi.domain.model.dto.RegulatoryAuthorityResponseDto;
import io.github.gracegrc.regulatoryapi.domain.model.dto.RegulatoryAuthorityUpdateRequestDto;
import io.github.gracegrc.regulatoryapi.domain.model.entity.AuthorityEntity;
import io.github.gracegrc.regulatoryapi.domain.model.exception.AuthorityAlreadyExistsException;
import io.github.gracegrc.regulatoryapi.domain.model.exception.AuthorityNotFoundException;
import io.github.gracegrc.regulatoryapi.domain.repository.AuthorityRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

@ExtendWith(MockitoExtension.class)
class AuthorityServiceImplTest {

	private static final UUID ID = UUID.fromString("3f2c1a9e-7d4b-4e8a-9c1f-2b6d5e8a7c40");

	private static final Instant CREATED_AT = Instant.parse("2026-10-01T10:15:30Z");

	private static final AuthorityEntity EXISTING = new AuthorityEntity(ID, "EBA", "EU", CREATED_AT, true,
			"admin-console");

	@Mock
	private AuthorityRepository authorityRepository;

	@InjectMocks
	private AuthorityServiceImpl authorityService;

	@Captor
	private ArgumentCaptor<AuthorityEntity> saved;

	@Test
	void createSavesAnEnabledAuthorityWithTheCurrentTime() {
		given(authorityRepository.save(any())).willAnswer(invocation -> {
			AuthorityEntity entity = invocation.getArgument(0);
			return new AuthorityEntity(ID, entity.name(), entity.jurisdiction(), entity.createdAt(), entity.enabled(),
					entity.createdBy());
		});
		Instant before = Instant.now();

		UUID id = authorityService.create(new RegulatoryAuthorityRequestDto("EBA", "EU", "admin-console"));

		then(authorityRepository).should().save(saved.capture());
		assertThat(saved.getValue().id()).isNull();
		assertThat(saved.getValue().name()).isEqualTo("EBA");
		assertThat(saved.getValue().enabled()).isTrue();
		assertThat(saved.getValue().createdAt()).isBetween(before, Instant.now());
		assertThat(saved.getValue().createdBy()).isEqualTo("admin-console");
		assertThat(id).isEqualTo(ID);
	}

	@Test
	void createWithADuplicateNameThrowsAlreadyExists() {
		given(authorityRepository.save(any())).willThrow(new DuplicateKeyException("authorities_name_key"));

		assertThatThrownBy(() -> authorityService.create(new RegulatoryAuthorityRequestDto("EBA", "EU", "admin-console")))
			.isInstanceOf(AuthorityAlreadyExistsException.class)
			.hasMessage("An authority named 'EBA' already exists");
	}

	@Test
	void findAllReturnsEveryAuthority() {
		AuthorityEntity other = new AuthorityEntity(UUID.randomUUID(), "FCA", "UK", CREATED_AT, false, "admin-console");
		given(authorityRepository.findAll()).willReturn(List.of(EXISTING, other));

		List<RegulatoryAuthorityResponseDto> response = authorityService.findAll();

		assertThat(response).extracting(RegulatoryAuthorityResponseDto::name).containsExactly("EBA", "FCA");
	}

	@Test
	void findByIdReturnsTheAuthority() {
		given(authorityRepository.findById(ID)).willReturn(Optional.of(EXISTING));

		assertThat(authorityService.findById(ID))
			.isEqualTo(new RegulatoryAuthorityResponseDto(ID, "EBA", "EU", CREATED_AT, true, "admin-console"));
	}

	@Test
	void findByIdWithAnUnknownIdThrowsNotFound() {
		given(authorityRepository.findById(ID)).willReturn(Optional.empty());

		assertThatThrownBy(() -> authorityService.findById(ID)).isInstanceOf(AuthorityNotFoundException.class)
			.hasMessage("Authority " + ID + " not found");
	}

	@Test
	void updateChangesNameJurisdictionAndEnabledOnly() {
		given(authorityRepository.findById(ID)).willReturn(Optional.of(EXISTING));
		given(authorityRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0));

		RegulatoryAuthorityResponseDto response = authorityService.update(ID,
				new RegulatoryAuthorityUpdateRequestDto("European Banking Authority", "European Union", false));

		assertThat(response).isEqualTo(new RegulatoryAuthorityResponseDto(ID, "European Banking Authority",
				"European Union", CREATED_AT, false, "admin-console"));
	}

	@Test
	void updateWithAnUnknownIdThrowsNotFoundAndSavesNothing() {
		given(authorityRepository.findById(ID)).willReturn(Optional.empty());

		assertThatThrownBy(
				() -> authorityService.update(ID, new RegulatoryAuthorityUpdateRequestDto("EBA", "EU", true)))
			.isInstanceOf(AuthorityNotFoundException.class);
		then(authorityRepository).should(never()).save(any());
	}

	@Test
	void updateToADuplicateNameThrowsAlreadyExists() {
		given(authorityRepository.findById(ID)).willReturn(Optional.of(EXISTING));
		given(authorityRepository.save(any())).willThrow(new DuplicateKeyException("authorities_name_key"));

		assertThatThrownBy(
				() -> authorityService.update(ID, new RegulatoryAuthorityUpdateRequestDto("FCA", "EU", true)))
			.isInstanceOf(AuthorityAlreadyExistsException.class)
			.hasMessage("An authority named 'FCA' already exists");
	}

	@Test
	void deleteRemovesAnExistingAuthority() {
		given(authorityRepository.existsById(ID)).willReturn(true);

		authorityService.delete(ID);

		then(authorityRepository).should().deleteById(ID);
	}

	@Test
	void deleteWithAnUnknownIdThrowsNotFoundAndDeletesNothing() {
		given(authorityRepository.existsById(ID)).willReturn(false);

		assertThatThrownBy(() -> authorityService.delete(ID)).isInstanceOf(AuthorityNotFoundException.class);
		then(authorityRepository).should(never()).deleteById(any());
	}

}
