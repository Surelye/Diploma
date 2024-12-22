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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import sgu.borodin.nas.dto.CurrentUser;
import sgu.borodin.nas.dto.PatchContext;
import sgu.borodin.nas.dto.UserDto;
import sgu.borodin.nas.enums.LogicalOperator;
import sgu.borodin.nas.exception.PermissionDeniedException;
import sgu.borodin.nas.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@Slf4j
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final CurrentUser currentUser;

    @GetMapping(value = "/{username}", produces = MediaType.APPLICATION_JSON_VALUE)
    public UserDto get(@PathVariable String username) {
        if (!currentUser.getUsername().equals(username) && !currentUser.hasAdminRole()) {
            throw new PermissionDeniedException("Regular user cannot access other user's information");
        }
        log.info("Fetching user with name [{}]", username);
        UserDto userDto = userService.getUser(username);
        log.info("User with name [{}] fetched successfully", username);
        return userDto;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Secured("ADMIN")
    public List<UserDto> getUsersHavingRoles(
            @RequestParam List<String> roles,
            @RequestParam LogicalOperator logicalOperator,
            @RequestParam(defaultValue = "100") int limit
    ) {
        log.info("Fetching the list of users having roles [{}] with operator [{}] by the user [{}]",
                roles, logicalOperator.name(), currentUser.getUsername());
        List<UserDto> usersHavingRole = userService.getUsersHavingRoles(roles, logicalOperator, limit);
        log.info("User [{}] fetched [{}] with operator [{}] having roles [{}]",
                currentUser.getUsername(), usersHavingRole, logicalOperator.name(), roles);
        return usersHavingRole;
    }

    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@Valid @RequestBody UserDto userDto) {
        log.info("Registering user [{}]", userDto.getUsername());
        UserDto createdUserDto = userService.registerUser(userDto);
        log.info("User [{}] was successfully registered", userDto.getUsername());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdUserDto);
    }

    @PatchMapping("/update")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(@Valid @RequestBody List<PatchContext> patches) {
        String username = currentUser.getUsername();
        List<PatchContext.UpdateField> updateFields = patches.stream().map(PatchContext::getField).toList();
        log.info("Updating fields {} for user [{}]", updateFields, username);
        userService.updateUserField(currentUser, patches);
        log.info("User's [{}] fields {} were successfully updated", username, updateFields);
    }

    @DeleteMapping("/delete/{username}")
    @Secured("ADMIN")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String username) {
        if (currentUser.getUsername().equals(username)) {
            throw new IllegalArgumentException("You cannot delete yourself");
        }
        log.info("Deleting user [{}] by the user [{}]", username, currentUser.getUsername());
        userService.deleteUser(username);
        log.info("User [{}] was successfully deleted by [{}]", username, currentUser.getUsername());
    }
}
