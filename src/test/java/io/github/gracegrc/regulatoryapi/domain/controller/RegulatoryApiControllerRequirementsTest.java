package io.github.gracegrc.regulatoryapi.domain.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;

import io.github.gracegrc.regulatoryapi.domain.model.dto.RegulatoryRequirementRequestDto;
import io.github.gracegrc.regulatoryapi.domain.model.dto.RegulatoryRequirementResponseDto;
import io.github.gracegrc.regulatoryapi.domain.model.dto.RegulatoryRequirementUpdateRequestDto;
import io.github.gracegrc.regulatoryapi.domain.model.exception.FileNotFoundException;
import io.github.gracegrc.regulatoryapi.domain.model.exception.ParentRequirementInAnotherFileException;
import io.github.gracegrc.regulatoryapi.domain.model.exception.RequirementAlreadyExistsException;
import io.github.gracegrc.regulatoryapi.domain.model.exception.RequirementHasChildrenException;
import io.github.gracegrc.regulatoryapi.domain.model.exception.RequirementNotFoundException;
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
class RegulatoryApiControllerRequirementsTest {

	private static final UUID ID = UUID.fromString("e1f2a3b4-c5d6-4e7f-8a9b-0c1d2e3f4a50");

	private static final UUID FILE_ID = UUID.fromString("c4d5e6f7-1a2b-4c3d-8e9f-0a1b2c3d4e50");

	private static final UUID PARENT_ID = UUID.fromString("a9b8c7d6-e5f4-4a3b-9c2d-1e0f9a8b7c60");

	private static final String BODY = "{\"fileId\":\"" + FILE_ID + "\",\"parentRequirementId\":\"" + PARENT_ID
			+ "\",\"provisionRef\":\"164.308(a)(4)(i)\",\"heading\":\"Isolating health care clearinghouse functions\","
			+ "\"text\":\"Implement policies and procedures.\",\"createdBy\":\"admin-console\"}";

	private static final RegulatoryRequirementRequestDto REQUEST = new RegulatoryRequirementRequestDto(FILE_ID,
			PARENT_ID, "164.308(a)(4)(i)", "Isolating health care clearinghouse functions",
			"Implement policies and procedures.", "admin-console");

	private static final RegulatoryRequirementResponseDto REQUIREMENT = new RegulatoryRequirementResponseDto(ID,
			FILE_ID, PARENT_ID, "164.308(a)(4)(i)", "Isolating health care clearinghouse functions",
			"Implement policies and procedures.", Instant.parse("2026-10-01T10:15:30Z"), "admin-console");

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
	void postCreatesTheRequirementAndReturnsAnEmptyBody() {
		given(requirementService.create(REQUEST)).willReturn(ID);

		assertThat(mvc.post().uri("/api/v1/requirements").contentType(MediaType.APPLICATION_JSON).content(BODY))
			.hasStatus(HttpStatus.CREATED)
			.hasHeader("Location", "http://localhost/api/v1/requirements/" + ID)
			.body()
			.isEmpty();
	}

	@Test
	void postWithInvalidFieldsReturnsBadRequestWithTheErrors() {
		assertThat(mvc.post()
			.uri("/api/v1/requirements")
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"provisionRef\":\"\",\"heading\":\"" + "x".repeat(256) + "\",\"createdBy\":\"admin-console\"}"))
			.hasStatus(HttpStatus.BAD_REQUEST)
			.bodyJson()
			.isLenientlyEqualTo("{\"status\":400,\"errors\":{\"fileId\":\"must not be null\","
					+ "\"provisionRef\":\"must not be blank\",\"heading\":\"size must be between 0 and 255\"}}");
		then(requirementService).shouldHaveNoInteractions();
	}

	@Test
	void postWithAnUnknownFileReturnsNotFound() {
		given(requirementService.create(any())).willThrow(new FileNotFoundException(FILE_ID));

		assertThat(mvc.post().uri("/api/v1/requirements").contentType(MediaType.APPLICATION_JSON).content(BODY))
			.hasStatus(HttpStatus.NOT_FOUND)
			.bodyJson()
			.isLenientlyEqualTo("{\"status\":404,\"detail\":\"File " + FILE_ID + " not found\"}");
	}

	@Test
	void postWithAParentInAnotherFileReturnsBadRequest() {
		given(requirementService.create(any()))
			.willThrow(new ParentRequirementInAnotherFileException(PARENT_ID, FILE_ID));

		assertThat(mvc.post().uri("/api/v1/requirements").contentType(MediaType.APPLICATION_JSON).content(BODY))
			.hasStatus(HttpStatus.BAD_REQUEST)
			.bodyJson()
			.isLenientlyEqualTo("{\"status\":400,\"detail\":\"Parent requirement " + PARENT_ID + " is not in file "
					+ FILE_ID + "\"}");
	}

	@Test
	void postWithADuplicateProvisionRefReturnsConflict() {
		given(requirementService.create(any()))
			.willThrow(new RequirementAlreadyExistsException(FILE_ID, "164.308(a)(4)(i)"));

		assertThat(mvc.post().uri("/api/v1/requirements").contentType(MediaType.APPLICATION_JSON).content(BODY))
			.hasStatus(HttpStatus.CONFLICT)
			.bodyJson()
			.isLenientlyEqualTo("{\"status\":409,\"detail\":\"A requirement with provision reference "
					+ "'164.308(a)(4)(i)' already exists in file " + FILE_ID + "\"}");
	}

	@Test
	void getReturnsTheList() {
		given(requirementService.findAll(null, null)).willReturn(List.of(REQUIREMENT));

		assertThat(mvc.get().uri("/api/v1/requirements")).hasStatusOk()
			.bodyJson()
			.isLenientlyEqualTo("[{\"id\":\"" + ID + "\",\"provisionRef\":\"164.308(a)(4)(i)\"}]");
	}

	@Test
	void getWithAFileIdAndAParentIdPassesBothFilters() {
		given(requirementService.findAll(FILE_ID, PARENT_ID)).willReturn(List.of(REQUIREMENT));

		assertThat(mvc.get()
			.uri("/api/v1/requirements")
			.param("fileId", FILE_ID.toString())
			.param("parentId", PARENT_ID.toString())).hasStatusOk()
			.bodyJson()
			.isLenientlyEqualTo("[{\"id\":\"" + ID + "\",\"fileId\":\"" + FILE_ID + "\",\"parentRequirementId\":\""
					+ PARENT_ID + "\"}]");
	}

	@Test
	void getByIdReturnsTheRequirement() {
		given(requirementService.findById(ID)).willReturn(REQUIREMENT);

		assertThat(mvc.get().uri("/api/v1/requirements/{id}", ID)).hasStatusOk()
			.bodyJson()
			.isLenientlyEqualTo("{\"id\":\"" + ID + "\",\"heading\":\"Isolating health care clearinghouse functions\","
					+ "\"text\":\"Implement policies and procedures.\",\"createdAt\":\"2026-10-01T10:15:30Z\","
					+ "\"createdBy\":\"admin-console\"}");
	}

	@Test
	void getByIdWithAnUnknownIdReturnsNotFound() {
		given(requirementService.findById(ID)).willThrow(new RequirementNotFoundException(ID));

		assertThat(mvc.get().uri("/api/v1/requirements/{id}", ID)).hasStatus(HttpStatus.NOT_FOUND)
			.bodyJson()
			.isLenientlyEqualTo("{\"status\":404,\"detail\":\"Requirement " + ID + " not found\"}");
	}

	@Test
	void putUpdatesTheRequirement() {
		RegulatoryRequirementResponseDto updated = new RegulatoryRequirementResponseDto(ID, FILE_ID, PARENT_ID,
				"164.308(a)(4)(ii)", null, null, REQUIREMENT.createdAt(), "admin-console");
		given(requirementService.update(ID, new RegulatoryRequirementUpdateRequestDto("164.308(a)(4)(ii)", null, null)))
			.willReturn(updated);

		assertThat(mvc.put()
			.uri("/api/v1/requirements/{id}", ID)
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"provisionRef\":\"164.308(a)(4)(ii)\"}"))
			.hasStatusOk()
			.bodyJson()
			.isLenientlyEqualTo("{\"id\":\"" + ID + "\",\"provisionRef\":\"164.308(a)(4)(ii)\",\"heading\":null}");
	}

	@Test
	void putWithoutAProvisionRefReturnsBadRequest() {
		assertThat(mvc.put()
			.uri("/api/v1/requirements/{id}", ID)
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"heading\":\"Heading\"}"))
			.hasStatus(HttpStatus.BAD_REQUEST)
			.bodyJson()
			.isLenientlyEqualTo("{\"errors\":{\"provisionRef\":\"must not be blank\"}}");
		then(requirementService).shouldHaveNoInteractions();
	}

	@Test
	void putWithAnUnknownIdReturnsNotFound() {
		given(requirementService.update(eq(ID), any())).willThrow(new RequirementNotFoundException(ID));

		assertThat(mvc.put()
			.uri("/api/v1/requirements/{id}", ID)
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"provisionRef\":\"164.308(a)(4)(ii)\"}"))
			.hasStatus(HttpStatus.NOT_FOUND);
	}

	@Test
	void deleteReturnsNoContent() {
		assertThat(mvc.delete().uri("/api/v1/requirements/{id}", ID)).hasStatus(HttpStatus.NO_CONTENT);
		then(requirementService).should().delete(ID);
	}

	@Test
	void deleteWithAnUnknownIdReturnsNotFound() {
		willThrow(new RequirementNotFoundException(ID)).given(requirementService).delete(ID);

		assertThat(mvc.delete().uri("/api/v1/requirements/{id}", ID)).hasStatus(HttpStatus.NOT_FOUND);
	}

	@Test
	void deleteOfARequirementWithChildrenReturnsConflict() {
		willThrow(new RequirementHasChildrenException(ID)).given(requirementService).delete(ID);

		assertThat(mvc.delete().uri("/api/v1/requirements/{id}", ID)).hasStatus(HttpStatus.CONFLICT)
			.bodyJson()
			.isLenientlyEqualTo("{\"status\":409,\"detail\":\"Requirement " + ID
					+ " has child requirements. Delete them first.\"}");
	}

}
