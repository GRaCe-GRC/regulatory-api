package io.github.gracegrc.regulatoryapi.domain.model.entity;

import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.InsertOnlyProperty;
import org.springframework.data.relational.core.mapping.Table;

@Table("frameworks")
public record FrameworkEntity(
		@Id UUID id,
		UUID authorityId,
		String name,
		String shortName,
		String citation,
		boolean enabled,
		@InsertOnlyProperty Instant createdAt,
		@InsertOnlyProperty String createdBy) {
}
