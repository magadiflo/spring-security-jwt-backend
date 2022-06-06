package com.magadiflo.app.resource;

import com.magadiflo.app.domain.User;
import com.magadiflo.app.exception.ExceptionHandling;
import com.magadiflo.app.exception.domain.EmailExistException;
import com.magadiflo.app.exception.domain.UserNotFoundException;
import com.magadiflo.app.exception.domain.UsernameExistException;
import com.magadiflo.app.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


/**
 * Cada vez que ocurra una excepción en esta clase (UserResource),
 * buscará en la clase ExceptionHandling para ver si hay algún controlador
 * que maneje el error producido. Si lo encuentra entonces irá hacia él
 * para procesar el error
 */

@RestController
@RequestMapping(path = {"/", "/user"})
public class UserResource extends ExceptionHandling {

    private final IUserService userService;

    @Autowired
    public UserResource(IUserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody User user)
            throws UserNotFoundException, EmailExistException, UsernameExistException {
        User newUser = this.userService.register(user.getFirstName(), user.getLastName(), user.getUsername(), user.getEmail());
        return new ResponseEntity<>(newUser, HttpStatus.OK);
    }

}
