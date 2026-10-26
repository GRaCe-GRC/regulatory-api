package io.github.gracegrc.regulatoryapi.domain.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;

import io.github.gracegrc.regulatoryapi.domain.model.dto.RegulatoryFileRequestDto;
import io.github.gracegrc.regulatoryapi.domain.model.dto.RegulatoryFileResponseDto;
import io.github.gracegrc.regulatoryapi.domain.model.dto.RegulatoryFileUpdateRequestDto;
import io.github.gracegrc.regulatoryapi.domain.model.exception.FileHasRequirementsException;
import io.github.gracegrc.regulatoryapi.domain.model.exception.FileNotFoundException;
import io.github.gracegrc.regulatoryapi.domain.model.exception.FrameworkNotFoundException;
import io.github.gracegrc.regulatoryapi.domain.service.AuthorityService;
import io.github.gracegrc.regulatoryapi.domain.service.FileService;
import io.github.gracegrc.regulatoryapi.domain.service.FrameworkService;
import io.github.gracegrc.regulatoryapi.domain.service.RequirementService;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@WebMvcTest(RegulatoryApiController.class)
class RegulatoryApiControllerFilesTest {

	private static final UUID ID = UUID.fromString("c4d5e6f7-1a2b-4c3d-8e9f-0a1b2c3d4e50");

	private static final UUID FRAMEWORK_ID = UUID.fromString("8b1e4c2a-5f3d-4a7e-b9c6-1d2e3f4a5b60");

	private static final String BODY = "{\"frameworkId\":\"" + FRAMEWORK_ID
			+ "\",\"title\":\"CRR consolidated text\",\"sourceUrl\":\"https://example.org/crr.pdf\","
			+ "\"versionLabel\":\"2024-07\",\"publicationDate\":\"2024-07-09\",\"createdBy\":\"admin-console\"}";

	private static final RegulatoryFileRequestDto REQUEST = new RegulatoryFileRequestDto(FRAMEWORK_ID,
			"CRR consolidated text", "https://example.org/crr.pdf", "2024-07", LocalDate.parse("2024-07-09"),
			"admin-console");

	private static final RegulatoryFileResponseDto FILE = new RegulatoryFileResponseDto(ID, FRAMEWORK_ID,
			"CRR consolidated text", "https://example.org/crr.pdf", "2024-07", LocalDate.parse("2024-07-09"),
			Instant.parse("2026-10-01T10:15:30Z"), "admin-console");

	@Autowired
	private MockMvcTester mvc;

	@MockitoBean
	private AuthorityService authorityService;

	@MockitoBean
	private FrameworkService frameworkService;

	@MockitoBean
	private FileService fileService;

	@MockitoBean
	private RequirementService requirementService;

	@Test
	void postCreatesTheFileAndReturnsAnEmptyBody() {
		given(fileService.create(REQUEST)).willReturn(ID);

		assertThat(mvc.post().uri("/api/v1/files").contentType(MediaType.APPLICATION_JSON).content(BODY))
			.hasStatus(HttpStatus.CREATED)
			.hasHeader("Location", "http://localhost/api/v1/files/" + ID)
			.body()
			.isEmpty();
	}

	@Test
	void postWithInvalidFieldsReturnsBadRequestWithTheErrors() {
		assertThat(mvc.post()
			.uri("/api/v1/files")
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"title\":\" \",\"versionLabel\":\"" + "x".repeat(256) + "\"}"))
			.hasStatus(HttpStatus.BAD_REQUEST)
			.bodyJson()
			.isLenientlyEqualTo("{\"status\":400,\"errors\":{\"frameworkId\":\"must not be null\","
					+ "\"title\":\"must not be blank\",\"versionLabel\":\"size must be between 0 and 255\","
					+ "\"createdBy\":\"must not be blank\"}}");
		then(fileService).shouldHaveNoInteractions();
	}

	@Test
	void postWithAMalformedPublicationDateReturnsBadRequest() {
		assertThat(mvc.post()
			.uri("/api/v1/files")
			.contentType(MediaType.APPLICATION_JSON)
			.content(BODY.replace("2024-07-09", "09/07/2024")))
			.hasStatus(HttpStatus.BAD_REQUEST);
		then(fileService).shouldHaveNoInteractions();
	}

	@Test
	void postWithAnUnknownFrameworkReturnsNotFound() {
		given(fileService.create(any())).willThrow(new FrameworkNotFoundException(FRAMEWORK_ID));

		assertThat(mvc.post().uri("/api/v1/files").contentType(MediaType.APPLICATION_JSON).content(BODY))
			.hasStatus(HttpStatus.NOT_FOUND)
			.bodyJson()
			.isLenientlyEqualTo("{\"status\":404,\"detail\":\"Framework " + FRAMEWORK_ID + " not found\"}");
	}

	@Test
	void getReturnsTheList() {
		given(fileService.findAll(null)).willReturn(List.of(FILE));

		assertThat(mvc.get().uri("/api/v1/files")).hasStatusOk()
			.bodyJson()
			.isLenientlyEqualTo("[{\"id\":\"" + ID + "\",\"title\":\"CRR consolidated text\"}]");
	}

	@Test
	void getWithAFrameworkIdReturnsItsFiles() {
		given(fileService.findAll(FRAMEWORK_ID)).willReturn(List.of(FILE));

		assertThat(mvc.get().uri("/api/v1/files").param("frameworkId", FRAMEWORK_ID.toString())).hasStatusOk()
			.bodyJson()
			.isLenientlyEqualTo("[{\"id\":\"" + ID + "\",\"frameworkId\":\"" + FRAMEWORK_ID + "\"}]");
	}

	@Test
	void getByIdReturnsTheFile() {
		given(fileService.findById(ID)).willReturn(FILE);

		assertThat(mvc.get().uri("/api/v1/files/{id}", ID)).hasStatusOk()
			.bodyJson()
			.isLenientlyEqualTo("{\"id\":\"" + ID + "\",\"sourceUrl\":\"https://example.org/crr.pdf\","
					+ "\"versionLabel\":\"2024-07\",\"publicationDate\":\"2024-07-09\","
					+ "\"createdAt\":\"2026-10-01T10:15:30Z\",\"createdBy\":\"admin-console\"}");
	}

	@Test
	void getByIdWithAnUnknownIdReturnsNotFound() {
		given(fileService.findById(ID)).willThrow(new FileNotFoundException(ID));

		assertThat(mvc.get().uri("/api/v1/files/{id}", ID)).hasStatus(HttpStatus.NOT_FOUND)
			.bodyJson()
			.isLenientlyEqualTo("{\"status\":404,\"detail\":\"File " + ID + " not found\"}");
	}

	@Test
	void putUpdatesTheFile() {
		RegulatoryFileResponseDto updated = new RegulatoryFileResponseDto(ID, FRAMEWORK_ID, "CRR", null, null, null,
				FILE.createdAt(), "admin-console");
		given(fileService.update(ID, new RegulatoryFileUpdateRequestDto("CRR", null, null, null))).willReturn(updated);

		assertThat(mvc.put()
			.uri("/api/v1/files/{id}", ID)
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"title\":\"CRR\"}"))
			.hasStatusOk()
			.bodyJson()
			.isLenientlyEqualTo("{\"id\":\"" + ID + "\",\"title\":\"CRR\",\"publicationDate\":null}");
	}

	@Test
	void putWithoutATitleReturnsBadRequest() {
		assertThat(mvc.put()
			.uri("/api/v1/files/{id}", ID)
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"versionLabel\":\"2024-07\"}"))
			.hasStatus(HttpStatus.BAD_REQUEST)
			.bodyJson()
			.isLenientlyEqualTo("{\"errors\":{\"title\":\"must not be blank\"}}");
		then(fileService).shouldHaveNoInteractions();
	}

	@Test
	void putWithAnUnknownIdReturnsNotFound() {
		given(fileService.update(eq(ID), any())).willThrow(new FileNotFoundException(ID));

		assertThat(mvc.put()
			.uri("/api/v1/files/{id}", ID)
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"title\":\"CRR\"}"))
			.hasStatus(HttpStatus.NOT_FOUND);
	}

	@Test
	void deleteReturnsNoContent() {
		assertThat(mvc.delete().uri("/api/v1/files/{id}", ID)).hasStatus(HttpStatus.NO_CONTENT);
		then(fileService).should().delete(ID);
	}

	@Test
	void deleteWithAnUnknownIdReturnsNotFound() {
		willThrow(new FileNotFoundException(ID)).given(fileService).delete(ID);

		assertThat(mvc.delete().uri("/api/v1/files/{id}", ID)).hasStatus(HttpStatus.NOT_FOUND);
	}

	@Test
	void deleteOfAFileWithRequirementsReturnsConflict() {
		willThrow(new FileHasRequirementsException(ID)).given(fileService).delete(ID);

		assertThat(mvc.delete().uri("/api/v1/files/{id}", ID)).hasStatus(HttpStatus.CONFLICT)
			.bodyJson()
			.isLenientlyEqualTo(
					"{\"status\":409,\"detail\":\"File " + ID + " has requirements. Delete them first.\"}");
	}

}
