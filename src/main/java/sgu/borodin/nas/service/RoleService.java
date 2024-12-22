package sgu.borodin.nas.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgu.borodin.nas.exception.RoleNotFoundException;
import sgu.borodin.nas.model.Role;
import sgu.borodin.nas.repository.RoleRepository;

import java.time.LocalDateTime;
import java.util.Optional;

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

    public Role updateRole(String originalName, Role role) {
        Optional<Role> originalRoleOpt = roleRepository.findRoleByName(originalName);

        if (originalRoleOpt.isEmpty()) {
            throw new RoleNotFoundException("Role [" + originalName + "] not found");
        }

        return roleRepository.save(originalRoleOpt.get()
                .setName(role.getName())
                .setLastModifiedAt(LocalDateTime.now()));
    }

    public void deleteRole(String name) {
        roleRepository.deleteRoleByName(name);
    }
}
