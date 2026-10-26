package io.github.gracegrc.regulatoryapi.domain.model.exception;

import java.util.UUID;

public class RequirementNotFoundException extends RuntimeException {

	public RequirementNotFoundException(UUID id) {
		super("Requirement " + id + " not found");
	}

}
