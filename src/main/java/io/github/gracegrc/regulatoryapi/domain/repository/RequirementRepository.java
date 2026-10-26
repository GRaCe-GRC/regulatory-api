package io.github.gracegrc.regulatoryapi.domain.repository;

import io.github.gracegrc.regulatoryapi.domain.model.entity.RequirementEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.repository.ListCrudRepository;

public interface RequirementRepository extends ListCrudRepository<RequirementEntity, UUID> {

	List<RequirementEntity> findByFileId(UUID fileId);

	List<RequirementEntity> findByParentRequirementId(UUID parentRequirementId);

	List<RequirementEntity> findByFileIdAndParentRequirementId(UUID fileId, UUID parentRequirementId);

}
