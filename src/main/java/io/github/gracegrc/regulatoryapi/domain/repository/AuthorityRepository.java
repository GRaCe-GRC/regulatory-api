package io.github.gracegrc.regulatoryapi.domain.repository;

import io.github.gracegrc.regulatoryapi.domain.model.entity.AuthorityEntity;
import java.util.UUID;
import org.springframework.data.repository.ListCrudRepository;

public interface AuthorityRepository extends ListCrudRepository<AuthorityEntity, UUID> {

}
