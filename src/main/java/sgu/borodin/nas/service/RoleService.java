package sgu.borodin.nas.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgu.borodin.nas.exception.RoleNotFoundException;
import sgu.borodin.nas.model.Role;
import sgu.borodin.nas.repository.RoleRepository;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class RoleService {
    private final RoleRepository roleRepository;

    @Transactional(readOnly = true)
    public Role getRole(String name) {
        return roleRepository.findRoleByName(name)
                .orElseThrow(() -> new RoleNotFoundException("Role [" + name + "] not found"));
    }

    public Role addRole(Role role) {
        return roleRepository.save(role
                .setCreatedAt(LocalDateTime.now())
                .setLastModifiedAt(LocalDateTime.now()));
    }
}
