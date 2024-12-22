package sgu.borodin.nas.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgu.borodin.nas.dto.CurrentUser;
import sgu.borodin.nas.dto.PatchContext;
import sgu.borodin.nas.dto.UserDto;
import sgu.borodin.nas.enums.LogicalOperator;
import sgu.borodin.nas.exception.UserAlreadyExistsException;
import sgu.borodin.nas.exception.UserNotFoundException;
import sgu.borodin.nas.model.User;
import sgu.borodin.nas.repository.UserRepository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static sgu.borodin.nas.dto.PatchContext.UpdateField.NOTES;
import static sgu.borodin.nas.dto.PatchContext.UpdateField.PASSWORD;
import static sgu.borodin.nas.dto.PatchContext.UpdateField.USERNAME;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class UserService {
    private final PasswordService passwordService;
    private final UserPersistenceService userPersistenceService;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserDto getUser(String username) {
        return userRepository.findByUsername(username)
                .map(UserDto::new)
                .orElseThrow(() -> new UserNotFoundException("User with name " + username + " not found"));
    }

    @Transactional(readOnly = true)
    public List<UserDto> getUsersHavingRoles(List<String> roleNames, LogicalOperator logicalOperator, int limit) {
        List<String> uppercaseRoleNames = roleNames.stream().map(String::toUpperCase).toList();

        List<User> foundUsers = switch (logicalOperator) {
            case AND -> userRepository.findAllUsersHavingAllRolesFrom(roleNames, limit);
            case OR -> userRepository.findAllUsersHavingAtLeastOneRoleIn(uppercaseRoleNames, limit);
        };

        return foundUsers.stream()
                .map(UserDto::new)
                .toList();
    }

    public UserDto registerUser(UserDto userDto) {
        String username = userDto.getUsername();

        if (userRepository.existsByUsername(username)) {
            throw new UserAlreadyExistsException("User [" + username + "] already exists");
        }

        try {
            Files.createDirectory(Path.of(FileOperationsService.UPLOAD_DIR_TEMPLATE.formatted(username)));
        } catch (IOException e) {
            log.error("Failed to create base directory for user {}", username);
            throw new IllegalStateException(e);
        }

        User user = userDto.getUser();

        User createdUser = userRepository.save(user
                .setPassword(passwordService.encodePassword(user.getPassword()))
                .setCreatedAt(LocalDateTime.now())
                .setLastModifiedAt(LocalDateTime.now()));

        return new UserDto(createdUser);
    }

    public void updateUserField(CurrentUser currentUser, List<PatchContext> patches) {
        String username = currentUser.getUsername();

        if (!userRepository.existsByUsername(username)) {
            throw new UserNotFoundException("User [" + username + "] not found");
        }

        patches.forEach(patchContext -> applyPatch(currentUser, patchContext));
    }

    public void deleteUser(String username) {
        Path userBaseUploadDirPath = Path.of(FileOperationsService.UPLOAD_DIR_TEMPLATE.formatted(username));

        try {
            if (Files.deleteIfExists(userBaseUploadDirPath)) {
                log.info("Base directory for user [{}] was deleted", username);
            } else {
                log.warn("Failed to delete base directory for user [{}] because it doesn't exist", username);
            }
        } catch (IOException e) {
            log.error("Failed to delete base directory for user [{}] because of: {}", username, e.getMessage(), e);
            throw new IllegalStateException(e);
        }

        userRepository.deleteByUsername(username);
    }

    private void applyPatch(CurrentUser currentUser, PatchContext patchContext) {
        String username = currentUser.getUsername();
        switch (patchContext.getField()) {
            case USERNAME -> updateUsername(currentUser, patchContext);
            case PASSWORD -> updatePassword(username, patchContext);
            case NOTES -> updateNotes(username, patchContext);
        }
    }

    private void updateUsername(CurrentUser currentUser, PatchContext patchContext) {
        String oldUsername = currentUser.getUsername();

        if (patchContext.getOperation() == PatchContext.Action.RESET) {
            throw new IllegalArgumentException("User's [" + oldUsername + "] username cannot be set to null");
        }

        File oldUploadDirectory = new File(currentUser.getUploadDirectory());
        String newUsername = patchContext.getValue();
        String newUploadDirectoryPath = FileOperationsService.UPLOAD_DIR_TEMPLATE.formatted(newUsername);

        if (oldUploadDirectory.exists()) {
            File newUploadDirectory = new File(newUploadDirectoryPath);

            if (oldUploadDirectory.renameTo(newUploadDirectory)) {
                log.info("Default directory for user [{}] was renamed to [{}]", oldUsername, newUsername);
            } else {
                log.error("Failed to rename user's [{}] default directory to [{}]", oldUsername, newUsername);
                throw new IllegalStateException("Failed to rename default directory to [%s] for user [%s]".formatted(
                        newUsername, oldUsername
                ));
            }
        } else {
            try {
                Files.createDirectory(Path.of(newUploadDirectoryPath));
            } catch (IOException e) {
                log.error("Failed to create base directory [{}] for user [{}]", newUsername, oldUsername);
                throw new IllegalStateException(e);
            }
        }

        userPersistenceService.updateColumnByUsername(USERNAME.getValue(), newUsername, oldUsername);
    }

    private void updatePassword(String username, PatchContext patchContext) {
        if (patchContext.getOperation() == PatchContext.Action.RESET) {
            throw new IllegalArgumentException("User's [" + username + "] password cannot be set to null");
        }

        userPersistenceService.updateColumnByUsername(
                PASSWORD.getValue(),
                passwordService.encodePassword(patchContext.getValue()),
                username
        );
    }

    private void updateNotes(String username, PatchContext patchContext) {
        userPersistenceService.updateColumnByUsername(
                NOTES.getValue(),
                patchContext.getOperation() == PatchContext.Action.RESET
                        ? null
                        : patchContext.getValue(),
                username
        );
    }
}
