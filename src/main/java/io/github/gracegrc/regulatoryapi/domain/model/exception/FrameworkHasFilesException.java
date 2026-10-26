package io.github.gracegrc.regulatoryapi.domain.model.exception;

import java.util.UUID;

public class FrameworkHasFilesException extends RuntimeException {

	public FrameworkHasFilesException(UUID id) {
		super("Framework " + id + " has files. Delete them first.");
	}

}
