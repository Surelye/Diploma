package sgu.borodin.nas.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sgu.borodin.nas.service.PasswordService;

@Profile("dev")
@RestController
@RequestMapping("/dev")
@RequiredArgsConstructor
public class PasswordController {
    private final PasswordService passwordService;

    @GetMapping("/encode/{password}")
    public String encode(@PathVariable String password) {
        return passwordService.encodePassword(password);
    }
}
