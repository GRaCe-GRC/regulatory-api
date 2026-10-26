package io.github.gracegrc.regulatoryapi.domain.model.exception;

import java.util.UUID;

public class FileNotFoundException extends RuntimeException {

	public FileNotFoundException(UUID id) {
		super("File " + id + " not found");
	}

}
