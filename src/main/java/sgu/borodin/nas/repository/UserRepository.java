package sgu.borodin.nas.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sgu.borodin.nas.model.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    void deleteByUsername(String username);

    @Query(value = """
            SELECT u.*
            FROM nas."user" u
            JOIN nas.user_role ur ON u.id = ur.user_id
            JOIN nas."role" r ON ur.role_id = r.id
            WHERE r.name IN (:roles)
            GROUP BY u.id
            HAVING COUNT(DISTINCT r.id) = :#{#roles.size()}
            LIMIT :limit;
            """,
            nativeQuery = true)
    List<User> findAllUsersHavingAllRolesFrom(@Param("roles") List<String> roles, @Param("limit") int limit);

    @Query(value = """
            SELECT u.*
            FROM nas."user" u
            WHERE EXISTS (
                SELECT 1
                FROM nas.user_role ur
                JOIN nas."role" r ON ur.role_id = r.id
                WHERE ur.user_id = u.id
                AND r.name IN (:roles)
            )
            LIMIT :limit;
            """,
            nativeQuery = true)
    List<User> findAllUsersHavingAtLeastOneRoleIn(@Param("roles") List<String> roles, @Param("limit") int limit);
}
