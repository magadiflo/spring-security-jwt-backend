package com.magadiflo.app.resource;

import com.magadiflo.app.constant.SecurityConstant;
import com.magadiflo.app.domain.User;
import com.magadiflo.app.domain.UserPrincipal;
import com.magadiflo.app.exception.ExceptionHandling;
import com.magadiflo.app.exception.domain.EmailExistException;
import com.magadiflo.app.exception.domain.UserNotFoundException;
import com.magadiflo.app.exception.domain.UsernameExistException;
import com.magadiflo.app.service.IUserService;
import com.magadiflo.app.utility.JWTTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
    private final AuthenticationManager authenticationManager;
    private final JWTTokenProvider jwtTokenProvider;

    @Autowired
    public UserResource(IUserService userService, AuthenticationManager authenticationManager, JWTTokenProvider jwtTokenProvider) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody User user)
            throws UserNotFoundException, EmailExistException, UsernameExistException {
        User newUser = this.userService.register(user.getFirstName(), user.getLastName(), user.getUsername(), user.getEmail());
        return new ResponseEntity<>(newUser, HttpStatus.OK);
    }

    @PostMapping("/login")
    public ResponseEntity<User> login(@RequestBody User user) {
        this.authenticate(user.getUsername(), user.getPassword());
        User loginUser = this.userService.findUserByUsername(user.getUsername());
        UserPrincipal userPrincipal = new UserPrincipal(loginUser);
        HttpHeaders jwtHeader = this.getJwtHeader(userPrincipal);

        return new ResponseEntity<>(loginUser, jwtHeader, HttpStatus.OK);
    }

    private HttpHeaders getJwtHeader(UserPrincipal user) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(SecurityConstant.JWT_TOKEN_HEADER, this.jwtTokenProvider.generateJwtToken(user));
        return headers;
    }

    private void authenticate(String username, String password) {
        this.authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
    }
}
