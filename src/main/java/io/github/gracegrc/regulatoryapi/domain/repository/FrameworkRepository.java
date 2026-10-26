package io.github.gracegrc.regulatoryapi.domain.repository;

import io.github.gracegrc.regulatoryapi.domain.model.entity.FrameworkEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.repository.ListCrudRepository;

public interface FrameworkRepository extends ListCrudRepository<FrameworkEntity, UUID> {

	List<FrameworkEntity> findByAuthorityId(UUID authorityId);

}
