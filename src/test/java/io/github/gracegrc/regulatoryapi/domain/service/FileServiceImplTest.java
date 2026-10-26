package io.github.gracegrc.regulatoryapi.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import io.github.gracegrc.regulatoryapi.domain.model.dto.RegulatoryFileRequestDto;
import io.github.gracegrc.regulatoryapi.domain.model.dto.RegulatoryFileResponseDto;
import io.github.gracegrc.regulatoryapi.domain.model.dto.RegulatoryFileUpdateRequestDto;
import io.github.gracegrc.regulatoryapi.domain.model.entity.FileEntity;
import io.github.gracegrc.regulatoryapi.domain.model.exception.FileNotFoundException;
import io.github.gracegrc.regulatoryapi.domain.model.exception.FrameworkNotFoundException;
import io.github.gracegrc.regulatoryapi.domain.repository.FileRepository;
import io.github.gracegrc.regulatoryapi.domain.repository.FrameworkRepository;
import java.time.Instant;
import java.time.LocalDate;
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

@ExtendWith(MockitoExtension.class)
class FileServiceImplTest {

	private static final UUID ID = UUID.fromString("c4d5e6f7-1a2b-4c3d-8e9f-0a1b2c3d4e50");

	private static final UUID FRAMEWORK_ID = UUID.fromString("8b1e4c2a-5f3d-4a7e-b9c6-1d2e3f4a5b60");

	private static final Instant CREATED_AT = Instant.parse("2026-10-01T10:15:30Z");

	private static final LocalDate PUBLISHED = LocalDate.parse("2024-07-09");

	private static final RegulatoryFileRequestDto REQUEST = new RegulatoryFileRequestDto(FRAMEWORK_ID,
			"CRR consolidated text", "https://example.org/crr.pdf", "2024-07", PUBLISHED, "admin-console");

	private static final FileEntity EXISTING = new FileEntity(ID, FRAMEWORK_ID, "CRR consolidated text",
			"https://example.org/crr.pdf", "2024-07", PUBLISHED, CREATED_AT, "admin-console");

	@Mock
	private FileRepository fileRepository;

	@Mock
	private FrameworkRepository frameworkRepository;

	@InjectMocks
	private FileServiceImpl fileService;

	@Captor
	private ArgumentCaptor<FileEntity> saved;

	@Test
	void createSavesTheFileWithTheCurrentTime() {
		given(frameworkRepository.existsById(FRAMEWORK_ID)).willReturn(true);
		given(fileRepository.save(any())).willAnswer(invocation -> {
			FileEntity entity = invocation.getArgument(0);
			return new FileEntity(ID, entity.frameworkId(), entity.title(), entity.sourceUrl(), entity.versionLabel(),
					entity.publicationDate(), entity.createdAt(), entity.createdBy());
		});
		Instant before = Instant.now();

		UUID id = fileService.create(REQUEST);

		then(fileRepository).should().save(saved.capture());
		assertThat(saved.getValue().id()).isNull();
		assertThat(saved.getValue().frameworkId()).isEqualTo(FRAMEWORK_ID);
		assertThat(saved.getValue().publicationDate()).isEqualTo(PUBLISHED);
		assertThat(saved.getValue().createdAt()).isBetween(before, Instant.now());
		assertThat(saved.getValue().createdBy()).isEqualTo("admin-console");
		assertThat(id).isEqualTo(ID);
	}

	@Test
	void createWithAnUnknownFrameworkThrowsNotFoundAndSavesNothing() {
		given(frameworkRepository.existsById(FRAMEWORK_ID)).willReturn(false);

		assertThatThrownBy(() -> fileService.create(REQUEST)).isInstanceOf(FrameworkNotFoundException.class)
			.hasMessage("Framework " + FRAMEWORK_ID + " not found");
		then(fileRepository).should(never()).save(any());
	}

	@Test
	void findAllWithoutAFrameworkReturnsEveryFile() {
		given(fileRepository.findAll()).willReturn(List.of(EXISTING));

		assertThat(fileService.findAll(null)).extracting(RegulatoryFileResponseDto::id).containsExactly(ID);
		then(fileRepository).should(never()).findByFrameworkId(any());
	}

	@Test
	void findAllWithAFrameworkReturnsItsFiles() {
		given(fileRepository.findByFrameworkId(FRAMEWORK_ID)).willReturn(List.of(EXISTING));

		assertThat(fileService.findAll(FRAMEWORK_ID)).extracting(RegulatoryFileResponseDto::id).containsExactly(ID);
		then(fileRepository).should(never()).findAll();
	}

	@Test
	void findByIdReturnsTheFile() {
		given(fileRepository.findById(ID)).willReturn(Optional.of(EXISTING));

		assertThat(fileService.findById(ID)).isEqualTo(new RegulatoryFileResponseDto(ID, FRAMEWORK_ID,
				"CRR consolidated text", "https://example.org/crr.pdf", "2024-07", PUBLISHED, CREATED_AT,
				"admin-console"));
	}

	@Test
	void findByIdWithAnUnknownIdThrowsNotFound() {
		given(fileRepository.findById(ID)).willReturn(Optional.empty());

		assertThatThrownBy(() -> fileService.findById(ID)).isInstanceOf(FileNotFoundException.class)
			.hasMessage("File " + ID + " not found");
	}

	@Test
	void updateChangesTitleSourceVersionAndPublicationDateOnly() {
		given(fileRepository.findById(ID)).willReturn(Optional.of(EXISTING));
		given(fileRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0));

		RegulatoryFileResponseDto response = fileService.update(ID,
				new RegulatoryFileUpdateRequestDto("CRR", null, "2025-01", LocalDate.parse("2025-01-01")));

		assertThat(response).isEqualTo(new RegulatoryFileResponseDto(ID, FRAMEWORK_ID, "CRR", null, "2025-01",
				LocalDate.parse("2025-01-01"), CREATED_AT, "admin-console"));
	}

	@Test
	void updateWithAnUnknownIdThrowsNotFoundAndSavesNothing() {
		given(fileRepository.findById(ID)).willReturn(Optional.empty());

		assertThatThrownBy(() -> fileService.update(ID, new RegulatoryFileUpdateRequestDto("CRR", null, null, null)))
			.isInstanceOf(FileNotFoundException.class);
		then(fileRepository).should(never()).save(any());
	}

	@Test
	void deleteRemovesAnExistingFile() {
		given(fileRepository.existsById(ID)).willReturn(true);

		fileService.delete(ID);

		then(fileRepository).should().deleteById(ID);
	}

	@Test
	void deleteWithAnUnknownIdThrowsNotFoundAndDeletesNothing() {
		given(fileRepository.existsById(ID)).willReturn(false);

		assertThatThrownBy(() -> fileService.delete(ID)).isInstanceOf(FileNotFoundException.class);
		then(fileRepository).should(never()).deleteById(any());
	}

}
