package io.github.gracegrc.regulatoryapi.domain.model.exception;

import java.util.UUID;

public class AuthorityHasFrameworksException extends RuntimeException {

	public AuthorityHasFrameworksException(UUID id) {
		super("Authority " + id + " has frameworks. Delete them first.");
	}

}
