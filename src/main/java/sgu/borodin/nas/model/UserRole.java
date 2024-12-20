package sgu.borodin.nas.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import sgu.borodin.nas.model.id.UserRoleId;

@Entity
@IdClass(UserRoleId.class)
@NoArgsConstructor
@AllArgsConstructor
public class UserRole {
    @Id
    @Column(name = "user_id")
    private Long userId;

    @Id
    @Column(name = "role_id")
    private Long roleId;
}

