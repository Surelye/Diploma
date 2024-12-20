package sgu.borodin.nas.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import sgu.borodin.nas.model.UserRole;
import sgu.borodin.nas.model.id.UserRoleId;

@Repository
public interface UserRoleRepository extends CrudRepository<UserRole, UserRoleId> {
}
