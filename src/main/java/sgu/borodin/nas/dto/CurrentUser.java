package sgu.borodin.nas.dto;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import sgu.borodin.nas.service.FileOperationsService;

@Component
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
@Getter
public class CurrentUser {
    private final String username;
    private final String uploadDirectory;

    @Autowired
    public CurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        this.username = ((UserDetails) authentication.getPrincipal()).getUsername();
        this.uploadDirectory = FileOperationsService.UPLOAD_DIR_TEMPLATE.formatted(getUsername());
    }
}
