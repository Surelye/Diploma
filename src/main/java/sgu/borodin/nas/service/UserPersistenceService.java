package sgu.borodin.nas.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaUpdate;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import sgu.borodin.nas.model.User;

import static sgu.borodin.nas.dto.PatchContext.UpdateField.USERNAME;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserPersistenceService {
    @PersistenceContext
    private final EntityManager entityManager;

    public void updateColumnByUsername(String columnName, String newValue, String username) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaUpdate<User> criteriaUpdate = criteriaBuilder.createCriteriaUpdate(User.class);
        Root<User> root = criteriaUpdate.from(User.class);

        try {
            Path<Object> columnPath = root.get(columnName);
            criteriaUpdate.set(columnPath, newValue);
            criteriaUpdate.where(criteriaBuilder.equal(root.get(USERNAME.getValue()), username));
            entityManager.createQuery(criteriaUpdate).executeUpdate();
        } catch (IllegalArgumentException e) {
            log.error("Failed to update column [{}] with new value [{}] for user [{}]", columnName, newValue, username);
            throw new IllegalArgumentException("Invalid column name or username: " + e.getMessage(), e);
        }
    }
}
