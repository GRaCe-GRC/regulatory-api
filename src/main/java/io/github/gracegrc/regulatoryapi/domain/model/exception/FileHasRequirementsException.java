package io.github.gracegrc.regulatoryapi.domain.model.exception;

import java.util.UUID;

public class FileHasRequirementsException extends RuntimeException {

	public FileHasRequirementsException(UUID id) {
		super("File " + id + " has requirements. Delete them first.");
	}

}
