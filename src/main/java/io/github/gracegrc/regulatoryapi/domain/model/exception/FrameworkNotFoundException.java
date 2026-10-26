package io.github.gracegrc.regulatoryapi.domain.model.exception;

import java.util.UUID;

public class FrameworkNotFoundException extends RuntimeException {

	public FrameworkNotFoundException(UUID id) {
		super("Framework " + id + " not found");
	}

}
