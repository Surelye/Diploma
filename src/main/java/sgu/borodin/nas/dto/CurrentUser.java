package sgu.borodin.nas.dto;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import sgu.borodin.nas.service.FileOperationsService;

import java.util.Collection;

@Component
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
@Getter
public class CurrentUser {
    private final String username;
    private final String uploadDirectory;
    private final Collection<? extends GrantedAuthority> authorities;

    @Autowired
    public CurrentUser() {
        var userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        this.username = userDetails.getUsername();
        this.authorities = userDetails.getAuthorities();
        this.uploadDirectory = FileOperationsService.UPLOAD_DIR_TEMPLATE.formatted(getUsername());
    }

    public boolean hasAdminRole() {
        return authorities.stream().anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ADMIN"));
    }
}
