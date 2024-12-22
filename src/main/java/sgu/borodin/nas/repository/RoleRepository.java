package sgu.borodin.nas.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import sgu.borodin.nas.model.Role;

import java.util.Optional;

@Repository
public interface RoleRepository extends CrudRepository<Role, Long> {

    Optional<Role> findRoleByName(String name);

    void deleteRoleByName(String name);
}
