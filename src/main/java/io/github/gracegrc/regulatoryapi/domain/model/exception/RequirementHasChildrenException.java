package io.github.gracegrc.regulatoryapi.domain.model.exception;

import java.util.UUID;

public class RequirementHasChildrenException extends RuntimeException {

	public RequirementHasChildrenException(UUID id) {
		super("Requirement " + id + " has child requirements. Delete them first.");
	}

}
