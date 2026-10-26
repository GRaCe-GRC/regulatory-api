package io.github.gracegrc.regulatoryapi.domain.model.entity;

import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.InsertOnlyProperty;
import org.springframework.data.relational.core.mapping.Table;

@Table("requirements")
public record RequirementEntity(
		@Id UUID id,
		UUID fileId,
		UUID parentRequirementId,
		String provisionRef,
		String heading,
		String text,
		@InsertOnlyProperty Instant createdAt,
		@InsertOnlyProperty String createdBy) {
}
