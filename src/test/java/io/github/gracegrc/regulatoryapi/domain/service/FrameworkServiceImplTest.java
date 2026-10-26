package io.github.gracegrc.regulatoryapi.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

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
class FrameworkServiceImplTest {

	private static final UUID ID = UUID.fromString("8b1e4c2a-5f3d-4a7e-b9c6-1d2e3f4a5b60");

	private static final UUID AUTHORITY_ID = UUID.fromString("3f2c1a9e-7d4b-4e8a-9c1f-2b6d5e8a7c40");

	private static final Instant CREATED_AT = Instant.parse("2026-10-01T10:15:30Z");

	private static final RegulatoryFrameworkRequestDto REQUEST = new RegulatoryFrameworkRequestDto(AUTHORITY_ID,
			"Capital Requirements Regulation", "CRR", "Regulation (EU) No 575/2013", "admin-console");

	private static final FrameworkEntity EXISTING = new FrameworkEntity(ID, AUTHORITY_ID,
			"Capital Requirements Regulation", "CRR", "Regulation (EU) No 575/2013", true, CREATED_AT,
			"admin-console");

	@Mock
	private FrameworkRepository frameworkRepository;

	@Mock
	private AuthorityRepository authorityRepository;

	@InjectMocks
	private FrameworkServiceImpl frameworkService;

	@Captor
	private ArgumentCaptor<FrameworkEntity> saved;

	@Test
	void createSavesAnEnabledFrameworkWithTheCurrentTime() {
		given(authorityRepository.existsById(AUTHORITY_ID)).willReturn(true);
		given(frameworkRepository.save(any())).willAnswer(invocation -> {
			FrameworkEntity entity = invocation.getArgument(0);
			return new FrameworkEntity(ID, entity.authorityId(), entity.name(), entity.shortName(), entity.citation(),
					entity.enabled(), entity.createdAt(), entity.createdBy());
		});
		Instant before = Instant.now();

		UUID id = frameworkService.create(REQUEST);

		then(frameworkRepository).should().save(saved.capture());
		assertThat(saved.getValue().id()).isNull();
		assertThat(saved.getValue().authorityId()).isEqualTo(AUTHORITY_ID);
		assertThat(saved.getValue().enabled()).isTrue();
		assertThat(saved.getValue().createdAt()).isBetween(before, Instant.now());
		assertThat(saved.getValue().createdBy()).isEqualTo("admin-console");
		assertThat(id).isEqualTo(ID);
	}

	@Test
	void createWithAnUnknownAuthorityThrowsNotFoundAndSavesNothing() {
		given(authorityRepository.existsById(AUTHORITY_ID)).willReturn(false);

		assertThatThrownBy(() -> frameworkService.create(REQUEST)).isInstanceOf(AuthorityNotFoundException.class)
			.hasMessage("Authority " + AUTHORITY_ID + " not found");
		then(frameworkRepository).should(never()).save(any());
	}

	@Test
	void createWithADuplicateNameThrowsAlreadyExists() {
		given(authorityRepository.existsById(AUTHORITY_ID)).willReturn(true);
		given(frameworkRepository.save(any()))
			.willThrow(new DuplicateKeyException("frameworks_authority_id_name_key"));

		assertThatThrownBy(() -> frameworkService.create(REQUEST))
			.isInstanceOf(FrameworkAlreadyExistsException.class)
			.hasMessage("A framework named 'Capital Requirements Regulation' already exists for authority "
					+ AUTHORITY_ID);
	}

	@Test
	void findAllWithoutAnAuthorityReturnsEveryFramework() {
		given(frameworkRepository.findAll()).willReturn(List.of(EXISTING));

		assertThat(frameworkService.findAll(null)).extracting(RegulatoryFrameworkResponseDto::id).containsExactly(ID);
		then(frameworkRepository).should(never()).findByAuthorityId(any());
	}

	@Test
	void findAllWithAnAuthorityReturnsItsFrameworks() {
		given(frameworkRepository.findByAuthorityId(AUTHORITY_ID)).willReturn(List.of(EXISTING));

		assertThat(frameworkService.findAll(AUTHORITY_ID)).extracting(RegulatoryFrameworkResponseDto::id)
			.containsExactly(ID);
		then(frameworkRepository).should(never()).findAll();
	}

	@Test
	void findByIdReturnsTheFramework() {
		given(frameworkRepository.findById(ID)).willReturn(Optional.of(EXISTING));

		assertThat(frameworkService.findById(ID)).isEqualTo(new RegulatoryFrameworkResponseDto(ID, AUTHORITY_ID,
				"Capital Requirements Regulation", "CRR", "Regulation (EU) No 575/2013", true, CREATED_AT,
				"admin-console"));
	}

	@Test
	void findByIdWithAnUnknownIdThrowsNotFound() {
		given(frameworkRepository.findById(ID)).willReturn(Optional.empty());

		assertThatThrownBy(() -> frameworkService.findById(ID)).isInstanceOf(FrameworkNotFoundException.class)
			.hasMessage("Framework " + ID + " not found");
	}

	@Test
	void updateChangesNameShortNameCitationAndEnabledOnly() {
		given(frameworkRepository.findById(ID)).willReturn(Optional.of(EXISTING));
		given(frameworkRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0));

		RegulatoryFrameworkResponseDto response = frameworkService.update(ID,
				new RegulatoryFrameworkUpdateRequestDto("CRR III", null, "Regulation (EU) 2024/1623", false));

		assertThat(response).isEqualTo(new RegulatoryFrameworkResponseDto(ID, AUTHORITY_ID, "CRR III", null,
				"Regulation (EU) 2024/1623", false, CREATED_AT, "admin-console"));
	}

	@Test
	void updateWithAnUnknownIdThrowsNotFoundAndSavesNothing() {
		given(frameworkRepository.findById(ID)).willReturn(Optional.empty());

		assertThatThrownBy(
				() -> frameworkService.update(ID, new RegulatoryFrameworkUpdateRequestDto("CRR", null, null, true)))
			.isInstanceOf(FrameworkNotFoundException.class);
		then(frameworkRepository).should(never()).save(any());
	}

	@Test
	void updateToADuplicateNameThrowsAlreadyExists() {
		given(frameworkRepository.findById(ID)).willReturn(Optional.of(EXISTING));
		given(frameworkRepository.save(any()))
			.willThrow(new DuplicateKeyException("frameworks_authority_id_name_key"));

		assertThatThrownBy(
				() -> frameworkService.update(ID, new RegulatoryFrameworkUpdateRequestDto("CRD", null, null, true)))
			.isInstanceOf(FrameworkAlreadyExistsException.class)
			.hasMessage("A framework named 'CRD' already exists for authority " + AUTHORITY_ID);
	}

	@Test
	void deleteRemovesAnExistingFramework() {
		given(frameworkRepository.existsById(ID)).willReturn(true);

		frameworkService.delete(ID);

		then(frameworkRepository).should().deleteById(ID);
	}

	@Test
	void deleteWithAnUnknownIdThrowsNotFoundAndDeletesNothing() {
		given(frameworkRepository.existsById(ID)).willReturn(false);

		assertThatThrownBy(() -> frameworkService.delete(ID)).isInstanceOf(FrameworkNotFoundException.class);
		then(frameworkRepository).should(never()).deleteById(any());
	}

}
