package com.magadiflo.app.listener;

import com.magadiflo.app.service.LoginAttemptService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;

@Component
public class AuthenticationFailureListener {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final LoginAttemptService loginAttemptService;

    //Inyección de dependencia por constructor (Por recomendación no usamos el @Autowired)
    public AuthenticationFailureListener(LoginAttemptService loginAttemptService) {
        this.loginAttemptService = loginAttemptService;
    }

    /**
     * Este método se disparará cada vez que un usuario inicia sesión,
     * pero no proporciona las credenciales correctas
     */
    @EventListener //Escuchamos el evento cada vez que ocurra
    public void onAuthenticationFailure(AuthenticationFailureBadCredentialsEvent event) throws ExecutionException {
        logger.info("Se disparó el evento AuthenticationFailureBadCredentialsEvent por fallo de credenciales!!!");
        Object principal = event.getAuthentication().getPrincipal();
        if (principal instanceof String) {
            String username = (String) principal;
            this.loginAttemptService.addUserToLoginAttemptCache(username);
        }
    }
}
