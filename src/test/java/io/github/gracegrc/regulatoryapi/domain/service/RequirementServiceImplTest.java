package io.github.gracegrc.regulatoryapi.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;

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
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;

@ExtendWith(MockitoExtension.class)
class RequirementServiceImplTest {

	private static final UUID ID = UUID.fromString("e1f2a3b4-c5d6-4e7f-8a9b-0c1d2e3f4a50");

	private static final UUID FILE_ID = UUID.fromString("c4d5e6f7-1a2b-4c3d-8e9f-0a1b2c3d4e50");

	private static final UUID PARENT_ID = UUID.fromString("a9b8c7d6-e5f4-4a3b-9c2d-1e0f9a8b7c60");

	private static final Instant CREATED_AT = Instant.parse("2026-10-01T10:15:30Z");

	private static final RequirementEntity PARENT = new RequirementEntity(PARENT_ID, FILE_ID, null, "164.308(a)(4)",
			"Information access management", null, CREATED_AT, "admin-console");

	private static final RequirementEntity EXISTING = new RequirementEntity(ID, FILE_ID, PARENT_ID,
			"164.308(a)(4)(i)", "Isolating health care clearinghouse functions", "Implement policies and procedures.",
			CREATED_AT, "admin-console");

	@Mock
	private RequirementRepository requirementRepository;

	@Mock
	private FileRepository fileRepository;

	@InjectMocks
	private RequirementServiceImpl requirementService;

	@Captor
	private ArgumentCaptor<RequirementEntity> saved;

	private static RegulatoryRequirementRequestDto request(UUID parentRequirementId) {
		return new RegulatoryRequirementRequestDto(FILE_ID, parentRequirementId, "164.308(a)(4)(i)",
				"Isolating health care clearinghouse functions", "Implement policies and procedures.",
				"admin-console");
	}

	@Test
	void createSavesATopLevelRequirementWithTheCurrentTime() {
		given(fileRepository.existsById(FILE_ID)).willReturn(true);
		given(requirementRepository.save(any())).willAnswer(invocation -> {
			RequirementEntity entity = invocation.getArgument(0);
			return new RequirementEntity(ID, entity.fileId(), entity.parentRequirementId(), entity.provisionRef(),
					entity.heading(), entity.text(), entity.createdAt(), entity.createdBy());
		});
		Instant before = Instant.now();

		UUID id = requirementService.create(request(null));

		then(requirementRepository).should().save(saved.capture());
		then(requirementRepository).should(never()).findById(any());
		assertThat(saved.getValue().id()).isNull();
		assertThat(saved.getValue().fileId()).isEqualTo(FILE_ID);
		assertThat(saved.getValue().parentRequirementId()).isNull();
		assertThat(saved.getValue().createdAt()).isBetween(before, Instant.now());
		assertThat(saved.getValue().createdBy()).isEqualTo("admin-console");
		assertThat(id).isEqualTo(ID);
	}

	@Test
	void createSavesAChildOfARequirementInTheSameFile() {
		given(fileRepository.existsById(FILE_ID)).willReturn(true);
		given(requirementRepository.findById(PARENT_ID)).willReturn(Optional.of(PARENT));
		given(requirementRepository.save(any())).willReturn(EXISTING);

		assertThat(requirementService.create(request(PARENT_ID))).isEqualTo(ID);

		then(requirementRepository).should().save(saved.capture());
		assertThat(saved.getValue().parentRequirementId()).isEqualTo(PARENT_ID);
	}

	@Test
	void createWithAnUnknownFileThrowsNotFoundAndSavesNothing() {
		given(fileRepository.existsById(FILE_ID)).willReturn(false);

		assertThatThrownBy(() -> requirementService.create(request(null))).isInstanceOf(FileNotFoundException.class)
			.hasMessage("File " + FILE_ID + " not found");
		then(requirementRepository).should(never()).save(any());
	}

	@Test
	void createWithAnUnknownParentThrowsNotFoundAndSavesNothing() {
		given(fileRepository.existsById(FILE_ID)).willReturn(true);
		given(requirementRepository.findById(PARENT_ID)).willReturn(Optional.empty());

		assertThatThrownBy(() -> requirementService.create(request(PARENT_ID)))
			.isInstanceOf(RequirementNotFoundException.class)
			.hasMessage("Requirement " + PARENT_ID + " not found");
		then(requirementRepository).should(never()).save(any());
	}

	@Test
	void createWithAParentInAnotherFileThrowsAndSavesNothing() {
		RequirementEntity parentElsewhere = new RequirementEntity(PARENT_ID, UUID.randomUUID(), null, "1", null, null,
				CREATED_AT, "admin-console");
		given(fileRepository.existsById(FILE_ID)).willReturn(true);
		given(requirementRepository.findById(PARENT_ID)).willReturn(Optional.of(parentElsewhere));

		assertThatThrownBy(() -> requirementService.create(request(PARENT_ID)))
			.isInstanceOf(ParentRequirementInAnotherFileException.class)
			.hasMessage("Parent requirement " + PARENT_ID + " is not in file " + FILE_ID);
		then(requirementRepository).should(never()).save(any());
	}

	@Test
	void createWithADuplicateProvisionRefThrowsAlreadyExists() {
		given(fileRepository.existsById(FILE_ID)).willReturn(true);
		given(requirementRepository.save(any()))
			.willThrow(new DuplicateKeyException("requirements_file_id_provision_ref_key"));

		assertThatThrownBy(() -> requirementService.create(request(null)))
			.isInstanceOf(RequirementAlreadyExistsException.class)
			.hasMessage("A requirement with provision reference '164.308(a)(4)(i)' already exists in file " + FILE_ID);
	}

	@Test
	void findAllWithoutFiltersReturnsEveryRequirement() {
		given(requirementRepository.findAll()).willReturn(List.of(PARENT, EXISTING));

		assertThat(requirementService.findAll(null, null)).extracting(RegulatoryRequirementResponseDto::id)
			.containsExactly(PARENT_ID, ID);
	}

	@Test
	void findAllWithAFileReturnsItsRequirements() {
		given(requirementRepository.findByFileId(FILE_ID)).willReturn(List.of(PARENT, EXISTING));

		assertThat(requirementService.findAll(FILE_ID, null)).extracting(RegulatoryRequirementResponseDto::id)
			.containsExactly(PARENT_ID, ID);
	}

	@Test
	void findAllWithAParentReturnsItsChildren() {
		given(requirementRepository.findByParentRequirementId(PARENT_ID)).willReturn(List.of(EXISTING));

		assertThat(requirementService.findAll(null, PARENT_ID)).extracting(RegulatoryRequirementResponseDto::id)
			.containsExactly(ID);
	}

	@Test
	void findAllWithAFileAndAParentUsesBothFilters() {
		given(requirementRepository.findByFileIdAndParentRequirementId(FILE_ID, PARENT_ID))
			.willReturn(List.of(EXISTING));

		assertThat(requirementService.findAll(FILE_ID, PARENT_ID)).extracting(RegulatoryRequirementResponseDto::id)
			.containsExactly(ID);
	}

	@Test
	void findByIdReturnsTheRequirement() {
		given(requirementRepository.findById(ID)).willReturn(Optional.of(EXISTING));

		assertThat(requirementService.findById(ID)).isEqualTo(new RegulatoryRequirementResponseDto(ID, FILE_ID,
				PARENT_ID, "164.308(a)(4)(i)", "Isolating health care clearinghouse functions",
				"Implement policies and procedures.", CREATED_AT, "admin-console"));
	}

	@Test
	void findByIdWithAnUnknownIdThrowsNotFound() {
		given(requirementRepository.findById(ID)).willReturn(Optional.empty());

		assertThatThrownBy(() -> requirementService.findById(ID)).isInstanceOf(RequirementNotFoundException.class)
			.hasMessage("Requirement " + ID + " not found");
	}

	@Test
	void updateChangesProvisionRefHeadingAndTextOnly() {
		given(requirementRepository.findById(ID)).willReturn(Optional.of(EXISTING));
		given(requirementRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0));

		RegulatoryRequirementResponseDto response = requirementService.update(ID,
				new RegulatoryRequirementUpdateRequestDto("164.308(a)(4)(ii)", null, "Updated text."));

		assertThat(response).isEqualTo(new RegulatoryRequirementResponseDto(ID, FILE_ID, PARENT_ID,
				"164.308(a)(4)(ii)", null, "Updated text.", CREATED_AT, "admin-console"));
	}

	@Test
	void updateWithAnUnknownIdThrowsNotFoundAndSavesNothing() {
		given(requirementRepository.findById(ID)).willReturn(Optional.empty());

		assertThatThrownBy(
				() -> requirementService.update(ID, new RegulatoryRequirementUpdateRequestDto("1", null, null)))
			.isInstanceOf(RequirementNotFoundException.class);
		then(requirementRepository).should(never()).save(any());
	}

	@Test
	void updateToADuplicateProvisionRefThrowsAlreadyExists() {
		given(requirementRepository.findById(ID)).willReturn(Optional.of(EXISTING));
		given(requirementRepository.save(any()))
			.willThrow(new DuplicateKeyException("requirements_file_id_provision_ref_key"));

		assertThatThrownBy(() -> requirementService.update(ID,
				new RegulatoryRequirementUpdateRequestDto("164.308(a)(4)", null, null)))
			.isInstanceOf(RequirementAlreadyExistsException.class)
			.hasMessage("A requirement with provision reference '164.308(a)(4)' already exists in file " + FILE_ID);
	}

	@Test
	void deleteRemovesAnExistingRequirement() {
		given(requirementRepository.existsById(ID)).willReturn(true);

		requirementService.delete(ID);

		then(requirementRepository).should().deleteById(ID);
	}

	@Test
	void deleteWithAnUnknownIdThrowsNotFoundAndDeletesNothing() {
		given(requirementRepository.existsById(ID)).willReturn(false);

		assertThatThrownBy(() -> requirementService.delete(ID)).isInstanceOf(RequirementNotFoundException.class);
		then(requirementRepository).should(never()).deleteById(any());
	}

	@Test
	void deleteOfARequirementWithChildrenThrowsHasChildren() {
		given(requirementRepository.existsById(PARENT_ID)).willReturn(true);
		willThrow(new DataIntegrityViolationException("requirements_parent_requirement_id_fkey"))
			.given(requirementRepository)
			.deleteById(PARENT_ID);

		assertThatThrownBy(() -> requirementService.delete(PARENT_ID))
			.isInstanceOf(RequirementHasChildrenException.class)
			.hasMessage("Requirement " + PARENT_ID + " has child requirements. Delete them first.");
	}

}
