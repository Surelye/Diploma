package sgu.borodin.nas.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import sgu.borodin.nas.dto.CurrentUser;
import sgu.borodin.nas.model.Role;
import sgu.borodin.nas.service.RoleService;

@RestController
@RequestMapping("/api/roles")
@Secured("ADMIN")
@Slf4j
@RequiredArgsConstructor
public class RoleController {
    private final RoleService roleService;
    private final CurrentUser currentUser;

    @GetMapping(value = "/{name}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Role get(@PathVariable String name) {
        return roleService.getRole(name);
    }

    @PostMapping(value = "/add", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Role> add(@Valid @RequestBody Role role) {
        log.info("Adding role [{}] by the user [{}]", role.getName(), currentUser.getUsername());
        Role addedRole = roleService.addRole(role);
        log.info("Role [{}] was successfully added by the user [{}]", addedRole.getName(), currentUser.getUsername());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(addedRole);
    }

    @PutMapping(value = "/update/{name}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Role> update(@PathVariable String name, @Valid @RequestBody Role role) {
        log.info("Updating role [{}] by the user [{}]", role.getName(), currentUser.getUsername());
        Role updatedRole = roleService.updateRole(name, role);
        log.info("Role [{}] was successfully updated by the user [{}]", updatedRole.getName(), currentUser.getUsername());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(updatedRole);
    }

    @DeleteMapping("/delete/{name}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String name) {
        log.info("Deleting role [{}] by the user [{}]", name, currentUser.getUsername());
        roleService.deleteRole(name);
        log.info("Role [{}] was successfully deleted by the user [{}]", name, currentUser.getUsername());
    }
}
