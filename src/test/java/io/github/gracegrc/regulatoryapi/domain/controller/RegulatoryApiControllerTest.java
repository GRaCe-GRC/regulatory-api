package io.github.gracegrc.regulatoryapi.domain.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;

import io.github.gracegrc.regulatoryapi.domain.model.dto.RegulatoryAuthorityRequestDto;
import io.github.gracegrc.regulatoryapi.domain.model.dto.RegulatoryAuthorityResponseDto;
import io.github.gracegrc.regulatoryapi.domain.model.dto.RegulatoryAuthorityUpdateRequestDto;
import io.github.gracegrc.regulatoryapi.domain.model.exception.AuthorityAlreadyExistsException;
import io.github.gracegrc.regulatoryapi.domain.model.exception.AuthorityHasFrameworksException;
import io.github.gracegrc.regulatoryapi.domain.model.exception.AuthorityNotFoundException;
import io.github.gracegrc.regulatoryapi.domain.service.AuthorityService;
import io.github.gracegrc.regulatoryapi.domain.service.FrameworkService;
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
class RegulatoryApiControllerTest {

	private static final UUID ID = UUID.fromString("3f2c1a9e-7d4b-4e8a-9c1f-2b6d5e8a7c40");

	private static final RegulatoryAuthorityResponseDto AUTHORITY = new RegulatoryAuthorityResponseDto(ID, "EBA", "EU",
			Instant.parse("2026-10-01T10:15:30Z"), true, "admin-console");

	@Autowired
	private MockMvcTester mvc;

	@MockitoBean
	private AuthorityService authorityService;

	@MockitoBean
	private FrameworkService frameworkService;

	@Test
	void postCreatesTheAuthorityAndReturnsAnEmptyBody() {
		given(authorityService.create(new RegulatoryAuthorityRequestDto("EBA", "EU", "admin-console"))).willReturn(ID);

		assertThat(mvc.post()
			.uri("/api/v1/authorities")
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"name\":\"EBA\",\"jurisdiction\":\"EU\",\"createdBy\":\"admin-console\"}"))
			.hasStatus(HttpStatus.CREATED)
			.hasHeader("Location", "http://localhost/api/v1/authorities/" + ID)
			.body()
			.isEmpty();
	}

	@Test
	void postWithInvalidFieldsReturnsBadRequestWithTheErrors() {
		assertThat(mvc.post()
			.uri("/api/v1/authorities")
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"name\":\"\",\"jurisdiction\":\"EU\"}"))
			.hasStatus(HttpStatus.BAD_REQUEST)
			.bodyJson()
			.isLenientlyEqualTo("{\"status\":400,\"errors\":{\"name\":\"must not be blank\","
					+ "\"createdBy\":\"must not be blank\"}}");
		then(authorityService).shouldHaveNoInteractions();
	}

	@Test
	void postWithADuplicateNameReturnsConflict() {
		given(authorityService.create(any())).willThrow(new AuthorityAlreadyExistsException("EBA"));

		assertThat(mvc.post()
			.uri("/api/v1/authorities")
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"name\":\"EBA\",\"jurisdiction\":\"EU\",\"createdBy\":\"admin-console\"}"))
			.hasStatus(HttpStatus.CONFLICT)
			.bodyJson()
			.isLenientlyEqualTo("{\"status\":409,\"detail\":\"An authority named 'EBA' already exists\"}");
	}

	@Test
	void getReturnsTheList() {
		given(authorityService.findAll()).willReturn(List.of(AUTHORITY));

		assertThat(mvc.get().uri("/api/v1/authorities")).hasStatusOk()
			.bodyJson()
			.isLenientlyEqualTo("[{\"id\":\"" + ID + "\",\"name\":\"EBA\"}]");
	}

	@Test
	void getByIdReturnsTheAuthority() {
		given(authorityService.findById(ID)).willReturn(AUTHORITY);

		assertThat(mvc.get().uri("/api/v1/authorities/{id}", ID)).hasStatusOk()
			.bodyJson()
			.isLenientlyEqualTo("{\"id\":\"" + ID + "\",\"name\":\"EBA\",\"enabled\":true}");
	}

	@Test
	void getByIdWithAnUnknownIdReturnsNotFound() {
		given(authorityService.findById(ID)).willThrow(new AuthorityNotFoundException(ID));

		assertThat(mvc.get().uri("/api/v1/authorities/{id}", ID)).hasStatus(HttpStatus.NOT_FOUND)
			.bodyJson()
			.isLenientlyEqualTo("{\"status\":404,\"detail\":\"Authority " + ID + " not found\"}");
	}

	@Test
	void getByIdWithAMalformedIdReturnsBadRequest() {
		assertThat(mvc.get().uri("/api/v1/authorities/not-a-uuid")).hasStatus(HttpStatus.BAD_REQUEST);
		then(authorityService).shouldHaveNoInteractions();
	}

	@Test
	void putUpdatesTheAuthority() {
		RegulatoryAuthorityResponseDto updated = new RegulatoryAuthorityResponseDto(ID, "EBA", "EU",
				AUTHORITY.createdAt(), false, "admin-console");
		given(authorityService.update(ID, new RegulatoryAuthorityUpdateRequestDto("EBA", "EU", false)))
			.willReturn(updated);

		assertThat(mvc.put()
			.uri("/api/v1/authorities/{id}", ID)
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"name\":\"EBA\",\"jurisdiction\":\"EU\",\"enabled\":false}"))
			.hasStatusOk()
			.bodyJson()
			.isLenientlyEqualTo("{\"id\":\"" + ID + "\",\"enabled\":false}");
	}

	@Test
	void putWithoutEnabledReturnsBadRequest() {
		assertThat(mvc.put()
			.uri("/api/v1/authorities/{id}", ID)
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"name\":\"EBA\",\"jurisdiction\":\"EU\"}"))
			.hasStatus(HttpStatus.BAD_REQUEST)
			.bodyJson()
			.isLenientlyEqualTo("{\"errors\":{\"enabled\":\"must not be null\"}}");
		then(authorityService).shouldHaveNoInteractions();
	}

	@Test
	void putWithAnUnknownIdReturnsNotFound() {
		given(authorityService.update(eq(ID), any())).willThrow(new AuthorityNotFoundException(ID));

		assertThat(mvc.put()
			.uri("/api/v1/authorities/{id}", ID)
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"name\":\"EBA\",\"jurisdiction\":\"EU\",\"enabled\":true}"))
			.hasStatus(HttpStatus.NOT_FOUND);
	}

	@Test
	void deleteReturnsNoContent() {
		assertThat(mvc.delete().uri("/api/v1/authorities/{id}", ID)).hasStatus(HttpStatus.NO_CONTENT);
		then(authorityService).should().delete(ID);
	}

	@Test
	void deleteWithAnUnknownIdReturnsNotFound() {
		willThrow(new AuthorityNotFoundException(ID)).given(authorityService).delete(ID);

		assertThat(mvc.delete().uri("/api/v1/authorities/{id}", ID)).hasStatus(HttpStatus.NOT_FOUND);
	}

	@Test
	void deleteOfAnAuthorityWithFrameworksReturnsConflict() {
		willThrow(new AuthorityHasFrameworksException(ID)).given(authorityService).delete(ID);

		assertThat(mvc.delete().uri("/api/v1/authorities/{id}", ID)).hasStatus(HttpStatus.CONFLICT)
			.bodyJson()
			.isLenientlyEqualTo(
					"{\"status\":409,\"detail\":\"Authority " + ID + " has frameworks. Delete them first.\"}");
	}

}
