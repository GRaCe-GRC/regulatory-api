package io.github.gracegrc.regulatoryapi.domain.model.exception;

import java.util.UUID;

public class FrameworkAlreadyExistsException extends RuntimeException {

	public FrameworkAlreadyExistsException(UUID authorityId, String name) {
		super("A framework named '" + name + "' already exists for authority " + authorityId);
	}

}
