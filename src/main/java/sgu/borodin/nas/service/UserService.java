package sgu.borodin.nas.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgu.borodin.nas.dto.UserDto;
import sgu.borodin.nas.exception.UserAlreadyExistsException;
import sgu.borodin.nas.exception.UserNotFoundException;
import sgu.borodin.nas.model.User;
import sgu.borodin.nas.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final PasswordService passwordService;
    private final UserRepository userRepository;

    public UserDto getUser(String username) {
        Optional<User> optUser = userRepository.findByUsername(username);
        return optUser
                .map(UserDto::new)
                .orElseThrow(() -> new UserNotFoundException("User with name " + username + " not found"));
    }

    @Transactional
    public UserDto registerUser(UserDto userDto) {
        String username = userDto.getUsername();

        if (userRepository.findByUsername(username).isPresent()) {
            throw new UserAlreadyExistsException("User " + username + " already exists");
        }

        User user = userDto.getUser();

        User createdUser = userRepository.save(user
                .setPassword(passwordService.encodePassword(user.getPassword()))
                .setCreatedAt(LocalDateTime.now())
                .setLastModifiedAt(LocalDateTime.now()));

        return new UserDto(createdUser);
    }
}
