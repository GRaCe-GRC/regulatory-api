package io.github.gracegrc.regulatoryapi.domain.model.entity;

import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.InsertOnlyProperty;
import org.springframework.data.relational.core.mapping.Table;

@Table("authorities")
public record AuthorityEntity(
		@Id UUID id,
		String name,
		String jurisdiction,
		@InsertOnlyProperty Instant createdAt,
		boolean enabled,
		@InsertOnlyProperty String createdBy) {
}
