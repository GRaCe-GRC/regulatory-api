package io.github.gracegrc.regulatoryapi.domain.model.exception;

import java.util.UUID;

public class RequirementAlreadyExistsException extends RuntimeException {

	public RequirementAlreadyExistsException(UUID fileId, String provisionRef) {
		super("A requirement with provision reference '" + provisionRef + "' already exists in file " + fileId);
	}

}
