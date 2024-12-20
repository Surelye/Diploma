package sgu.borodin.nas.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sgu.borodin.nas.dto.UserDto;
import sgu.borodin.nas.service.UserService;

@RestController
@RequestMapping("/api/users")
@Slf4j
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/{username}")
    public UserDto getUser(@PathVariable String username) {
        log.info("Fetching user with name {}", username);
        UserDto userDto = userService.getUser(username);
        log.info("User with name {} fetched successfully", username);
        return userDto;
    }

    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@Valid @RequestBody UserDto userDto) {
        log.info("Registering user {}", userDto.getUsername());
        UserDto createdUserDto = userService.registerUser(userDto);
        log.info("User {} was successfully registered", userDto.getUsername());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdUserDto);
    }
}
