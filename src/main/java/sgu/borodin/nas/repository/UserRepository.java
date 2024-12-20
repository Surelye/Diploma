package sgu.borodin.nas.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sgu.borodin.nas.model.User;

import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {

    Optional<User> findByUsername(String username);

    @Modifying
    @Query(value = """
            INSERT INTO "user" (
                username, password, created_at, last_modified_at
            )
            VALUES (
                :#{#u.username},
                :#{#u.password},
                :#{#u.createdAt},
                :#{#u.lastModifiedAt}
            )
            ON CONFLICT (username)
            DO UPDATE SET
                password         = :#{#u.password},
                last_modified_at = now()
            """,
            nativeQuery = true)
    void upsert(@Param("u") User user);
}
