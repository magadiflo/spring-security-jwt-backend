package com.magadiflo.app.listener;

import com.magadiflo.app.domain.UserPrincipal;
import com.magadiflo.app.service.LoginAttemptService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationSuccessListener {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final LoginAttemptService loginAttemptService;

    //Inyección de dependencia por constructor
    public AuthenticationSuccessListener(LoginAttemptService loginAttemptService) {
        this.loginAttemptService = loginAttemptService;
    }

    @EventListener
    public void onAuthenticationSuccess(AuthenticationSuccessEvent event) {
        this.logger.info("Se disparó el evento AuthenticationSuccessEvent, inicio sesión con éxito!!");
        Object principal = event.getAuthentication().getPrincipal();
        if (principal instanceof UserPrincipal) {
            UserPrincipal userPrincipal = (UserPrincipal) principal;
            this.loginAttemptService.evictUserFromLoginAttemptCache(userPrincipal.getUsername());
        }
    }

}
