package io.github.gracegrc.regulatoryapi.domain.model.exception;

import java.util.UUID;

public class AuthorityNotFoundException extends RuntimeException {

	public AuthorityNotFoundException(UUID id) {
		super("Authority " + id + " not found");
	}

}
