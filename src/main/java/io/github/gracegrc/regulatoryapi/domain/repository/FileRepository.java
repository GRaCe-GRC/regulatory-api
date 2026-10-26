package io.github.gracegrc.regulatoryapi.domain.repository;

import io.github.gracegrc.regulatoryapi.domain.model.entity.FileEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.repository.ListCrudRepository;

public interface FileRepository extends ListCrudRepository<FileEntity, UUID> {

	List<FileEntity> findByFrameworkId(UUID frameworkId);

}
