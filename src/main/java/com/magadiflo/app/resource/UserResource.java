package com.magadiflo.app.resource;

import com.magadiflo.app.domain.User;
import com.magadiflo.app.exception.ExceptionHandling;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Cada vez que ocurra una excepción en esta clase (UserResource),
 * buscará en la clase ExceptionHandling para ver si hay algún controlador
 * que maneje el error producido. Si lo encuentra entonces irá hacia él
 * para procesar el error
 */

@RestController
@RequestMapping(value = "/users")
public class UserResource extends ExceptionHandling {

    @GetMapping("/home")
    public String home() {
        return "Application works";
    }

}
