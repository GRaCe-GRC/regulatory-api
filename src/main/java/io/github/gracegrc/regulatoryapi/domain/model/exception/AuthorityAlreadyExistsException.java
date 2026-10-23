package io.github.gracegrc.regulatoryapi.domain.model.exception;

public class AuthorityAlreadyExistsException extends RuntimeException {

	public AuthorityAlreadyExistsException(String name) {
		super("An authority named '" + name + "' already exists");
	}

}
