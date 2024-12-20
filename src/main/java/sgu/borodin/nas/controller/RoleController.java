package sgu.borodin.nas.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sgu.borodin.nas.model.Role;
import sgu.borodin.nas.service.RoleService;

@RestController
@RequestMapping("/api/roles")
@Secured("ADMIN")
@Slf4j
@RequiredArgsConstructor
public class RoleController {
    private final RoleService roleService;

    @GetMapping("/{name}")
    public Role get(@PathVariable String name) {
        return roleService.getRole(name);
    }

    @PostMapping("/add")
    public ResponseEntity<Role> add(@Valid @RequestBody Role role) {
        log.info("Adding role {}", role.getName());
        Role addedRole = roleService.addRole(role);
        log.info("Role {} was successfully added", role.getName());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(addedRole);
    }
}
