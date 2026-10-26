package io.github.gracegrc.regulatoryapi.domain.model.exception;

import java.util.UUID;

public class ParentRequirementInAnotherFileException extends RuntimeException {

	public ParentRequirementInAnotherFileException(UUID parentRequirementId, UUID fileId) {
		super("Parent requirement " + parentRequirementId + " is not in file " + fileId);
	}

}
