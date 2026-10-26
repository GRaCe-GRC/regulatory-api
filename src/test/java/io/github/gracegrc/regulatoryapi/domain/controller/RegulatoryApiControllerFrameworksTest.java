package io.github.gracegrc.regulatoryapi.domain.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;

import io.github.gracegrc.regulatoryapi.domain.model.dto.RegulatoryFrameworkRequestDto;
import io.github.gracegrc.regulatoryapi.domain.model.dto.RegulatoryFrameworkResponseDto;
import io.github.gracegrc.regulatoryapi.domain.model.dto.RegulatoryFrameworkUpdateRequestDto;
import io.github.gracegrc.regulatoryapi.domain.model.exception.AuthorityNotFoundException;
import io.github.gracegrc.regulatoryapi.domain.model.exception.FrameworkAlreadyExistsException;
import io.github.gracegrc.regulatoryapi.domain.model.exception.FrameworkHasFilesException;
import io.github.gracegrc.regulatoryapi.domain.model.exception.FrameworkNotFoundException;
import io.github.gracegrc.regulatoryapi.domain.service.AuthorityService;
import io.github.gracegrc.regulatoryapi.domain.service.FileService;
import io.github.gracegrc.regulatoryapi.domain.service.FrameworkService;
import io.github.gracegrc.regulatoryapi.domain.service.RequirementService;
import java.time.Instant;
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
class RegulatoryApiControllerFrameworksTest {

	private static final UUID ID = UUID.fromString("8b1e4c2a-5f3d-4a7e-b9c6-1d2e3f4a5b60");

	private static final UUID AUTHORITY_ID = UUID.fromString("3f2c1a9e-7d4b-4e8a-9c1f-2b6d5e8a7c40");

	private static final String BODY = "{\"authorityId\":\"" + AUTHORITY_ID
			+ "\",\"name\":\"Capital Requirements Regulation\",\"shortName\":\"CRR\","
			+ "\"citation\":\"Regulation (EU) No 575/2013\",\"createdBy\":\"admin-console\"}";

	private static final RegulatoryFrameworkRequestDto REQUEST = new RegulatoryFrameworkRequestDto(AUTHORITY_ID,
			"Capital Requirements Regulation", "CRR", "Regulation (EU) No 575/2013", "admin-console");

	private static final RegulatoryFrameworkResponseDto FRAMEWORK = new RegulatoryFrameworkResponseDto(ID,
			AUTHORITY_ID, "Capital Requirements Regulation", "CRR", "Regulation (EU) No 575/2013", true,
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
	void postCreatesTheFrameworkAndReturnsAnEmptyBody() {
		given(frameworkService.create(REQUEST)).willReturn(ID);

		assertThat(mvc.post().uri("/api/v1/frameworks").contentType(MediaType.APPLICATION_JSON).content(BODY))
			.hasStatus(HttpStatus.CREATED)
			.hasHeader("Location", "http://localhost/api/v1/frameworks/" + ID)
			.body()
			.isEmpty();
	}

	@Test
	void postWithInvalidFieldsReturnsBadRequestWithTheErrors() {
		assertThat(mvc.post()
			.uri("/api/v1/frameworks")
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"name\":\"\",\"shortName\":\"" + "x".repeat(256) + "\",\"createdBy\":\"admin-console\"}"))
			.hasStatus(HttpStatus.BAD_REQUEST)
			.bodyJson()
			.isLenientlyEqualTo("{\"status\":400,\"errors\":{\"authorityId\":\"must not be null\","
					+ "\"name\":\"must not be blank\",\"shortName\":\"size must be between 0 and 255\"}}");
		then(frameworkService).shouldHaveNoInteractions();
	}

	@Test
	void postWithAnUnknownAuthorityReturnsNotFound() {
		given(frameworkService.create(any())).willThrow(new AuthorityNotFoundException(AUTHORITY_ID));

		assertThat(mvc.post().uri("/api/v1/frameworks").contentType(MediaType.APPLICATION_JSON).content(BODY))
			.hasStatus(HttpStatus.NOT_FOUND)
			.bodyJson()
			.isLenientlyEqualTo("{\"status\":404,\"detail\":\"Authority " + AUTHORITY_ID + " not found\"}");
	}

	@Test
	void postWithADuplicateNameReturnsConflict() {
		given(frameworkService.create(any()))
			.willThrow(new FrameworkAlreadyExistsException(AUTHORITY_ID, "Capital Requirements Regulation"));

		assertThat(mvc.post().uri("/api/v1/frameworks").contentType(MediaType.APPLICATION_JSON).content(BODY))
			.hasStatus(HttpStatus.CONFLICT)
			.bodyJson()
			.isLenientlyEqualTo("{\"status\":409,\"detail\":\"A framework named 'Capital Requirements Regulation'"
					+ " already exists for authority " + AUTHORITY_ID + "\"}");
	}

	@Test
	void getReturnsTheList() {
		given(frameworkService.findAll(null)).willReturn(List.of(FRAMEWORK));

		assertThat(mvc.get().uri("/api/v1/frameworks")).hasStatusOk()
			.bodyJson()
			.isLenientlyEqualTo("[{\"id\":\"" + ID + "\",\"shortName\":\"CRR\"}]");
	}

	@Test
	void getWithAnAuthorityIdReturnsItsFrameworks() {
		given(frameworkService.findAll(AUTHORITY_ID)).willReturn(List.of(FRAMEWORK));

		assertThat(mvc.get().uri("/api/v1/frameworks").param("authorityId", AUTHORITY_ID.toString())).hasStatusOk()
			.bodyJson()
			.isLenientlyEqualTo("[{\"id\":\"" + ID + "\",\"authorityId\":\"" + AUTHORITY_ID + "\"}]");
	}

	@Test
	void getWithAMalformedAuthorityIdReturnsBadRequest() {
		assertThat(mvc.get().uri("/api/v1/frameworks").param("authorityId", "not-a-uuid"))
			.hasStatus(HttpStatus.BAD_REQUEST);
		then(frameworkService).shouldHaveNoInteractions();
	}

	@Test
	void getByIdReturnsTheFramework() {
		given(frameworkService.findById(ID)).willReturn(FRAMEWORK);

		assertThat(mvc.get().uri("/api/v1/frameworks/{id}", ID)).hasStatusOk()
			.bodyJson()
			.isLenientlyEqualTo("{\"id\":\"" + ID + "\",\"citation\":\"Regulation (EU) No 575/2013\",\"enabled\":true,"
					+ "\"createdAt\":\"2026-10-01T10:15:30Z\",\"createdBy\":\"admin-console\"}");
	}

	@Test
	void getByIdWithAnUnknownIdReturnsNotFound() {
		given(frameworkService.findById(ID)).willThrow(new FrameworkNotFoundException(ID));

		assertThat(mvc.get().uri("/api/v1/frameworks/{id}", ID)).hasStatus(HttpStatus.NOT_FOUND)
			.bodyJson()
			.isLenientlyEqualTo("{\"status\":404,\"detail\":\"Framework " + ID + " not found\"}");
	}

	@Test
	void putUpdatesTheFramework() {
		RegulatoryFrameworkResponseDto updated = new RegulatoryFrameworkResponseDto(ID, AUTHORITY_ID, "CRR", null,
				null, false, FRAMEWORK.createdAt(), "admin-console");
		given(frameworkService.update(ID, new RegulatoryFrameworkUpdateRequestDto("CRR", null, null, false)))
			.willReturn(updated);

		assertThat(mvc.put()
			.uri("/api/v1/frameworks/{id}", ID)
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"name\":\"CRR\",\"enabled\":false}"))
			.hasStatusOk()
			.bodyJson()
			.isLenientlyEqualTo("{\"id\":\"" + ID + "\",\"name\":\"CRR\",\"enabled\":false}");
	}

	@Test
	void putWithoutEnabledReturnsBadRequest() {
		assertThat(mvc.put()
			.uri("/api/v1/frameworks/{id}", ID)
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"name\":\"CRR\"}"))
			.hasStatus(HttpStatus.BAD_REQUEST)
			.bodyJson()
			.isLenientlyEqualTo("{\"errors\":{\"enabled\":\"must not be null\"}}");
		then(frameworkService).shouldHaveNoInteractions();
	}

	@Test
	void putWithAnUnknownIdReturnsNotFound() {
		given(frameworkService.update(eq(ID), any())).willThrow(new FrameworkNotFoundException(ID));

		assertThat(mvc.put()
			.uri("/api/v1/frameworks/{id}", ID)
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"name\":\"CRR\",\"enabled\":true}"))
			.hasStatus(HttpStatus.NOT_FOUND);
	}

	@Test
	void putToADuplicateNameReturnsConflict() {
		given(frameworkService.update(eq(ID), any()))
			.willThrow(new FrameworkAlreadyExistsException(AUTHORITY_ID, "CRR"));

		assertThat(mvc.put()
			.uri("/api/v1/frameworks/{id}", ID)
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"name\":\"CRR\",\"enabled\":true}"))
			.hasStatus(HttpStatus.CONFLICT);
	}

	@Test
	void deleteReturnsNoContent() {
		assertThat(mvc.delete().uri("/api/v1/frameworks/{id}", ID)).hasStatus(HttpStatus.NO_CONTENT);
		then(frameworkService).should().delete(ID);
	}

	@Test
	void deleteWithAnUnknownIdReturnsNotFound() {
		willThrow(new FrameworkNotFoundException(ID)).given(frameworkService).delete(ID);

		assertThat(mvc.delete().uri("/api/v1/frameworks/{id}", ID)).hasStatus(HttpStatus.NOT_FOUND);
	}

	@Test
	void deleteOfAFrameworkWithFilesReturnsConflict() {
		willThrow(new FrameworkHasFilesException(ID)).given(frameworkService).delete(ID);

		assertThat(mvc.delete().uri("/api/v1/frameworks/{id}", ID)).hasStatus(HttpStatus.CONFLICT)
			.bodyJson()
			.isLenientlyEqualTo(
					"{\"status\":409,\"detail\":\"Framework " + ID + " has files. Delete them first.\"}");
	}

}
