package com.magadiflo.app.resource;

import com.magadiflo.app.constant.FileConstant;
import com.magadiflo.app.constant.SecurityConstant;
import com.magadiflo.app.domain.HttpResponse;
import com.magadiflo.app.domain.User;
import com.magadiflo.app.domain.UserPrincipal;
import com.magadiflo.app.exception.ExceptionHandling;
import com.magadiflo.app.exception.domain.EmailExistException;
import com.magadiflo.app.exception.domain.EmailNotFoundException;
import com.magadiflo.app.exception.domain.UserNotFoundException;
import com.magadiflo.app.exception.domain.UsernameExistException;
import com.magadiflo.app.service.IUserService;
import com.magadiflo.app.utility.JWTTokenProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.MessagingException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;


/**
 * Cada vez que ocurra una excepción en esta clase (UserResource),
 * buscará en la clase ExceptionHandling para ver si hay algún controlador
 * que maneje el error producido. Si lo encuentra entonces irá hacia él
 * para procesar el error
 */

@RestController
@RequestMapping(path = {"/", "/user"})
public class UserResource extends ExceptionHandling {

    public static final String EMAIL_SENT = "An email with a new password was sent to: ";
    public static final String USER_DELETED_SUCCESSFULLY = "User deleted successfully";

    private final IUserService userService;
    private final AuthenticationManager authenticationManager;
    private final JWTTokenProvider jwtTokenProvider;

    /******* INYECCIÓN DE DEPENDENCIA POR CONSTRUCTOR *******
     * Cuando se inyecta vía constructor la anotación @Autowired normalmente no es necesaria
     * (esto es así desde la versión 4.3 de spring) y la manera recomendada de inyectar
     * por la documentación oficial es de hecho la de constructor. En nuestro caso, estábamos
     * usando la anotación @Autowired, luego enterándonos de esta manera de
     * hacer la inyección de dependencia, se decidió quitar la anotación @Autowired
     * del constructor, al menos en esta clase y al ejecutarlo funciona bien.
     * Ver documento oficial:
     * <a href="https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#beans-constructor-injection">beans-constructor-injection</a>
     * Ver documento explicativo:
     * <a href="https://www.dev-util.com/java/spring-framework/inyeccion-de-dependencias-autowired-o-por-constructor-en-spring#_inyecci%C3%B3n_por_constructor">Inyección de dependencias en Spring Framework</a>
     */
    public UserResource(IUserService userService, AuthenticationManager authenticationManager, JWTTokenProvider jwtTokenProvider) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody User user)
            throws UserNotFoundException, EmailExistException, UsernameExistException, MessagingException {
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

    @PostMapping("/add")
    public ResponseEntity<User> addNewUser(@RequestParam String firstName,
                                           @RequestParam String lastName,
                                           @RequestParam String username,
                                           @RequestParam String email,
                                           @RequestParam String role,
                                           @RequestParam String isActive,
                                           @RequestParam String isNotLocked,
                                           @RequestParam(required = false) MultipartFile profileImage)
            throws UserNotFoundException, EmailExistException, IOException, UsernameExistException {

        User newUser = this.userService.addNewUser(firstName, lastName, username, email, role,
                Boolean.parseBoolean(isNotLocked), Boolean.parseBoolean(isActive), profileImage);
        return new ResponseEntity<>(newUser, HttpStatus.OK);
    }

    @PostMapping("/update")
    public ResponseEntity<User> update(@RequestParam String currentUsername,
                                       @RequestParam String firstName,
                                       @RequestParam String lastName,
                                       @RequestParam String username,
                                       @RequestParam String email,
                                       @RequestParam String role,
                                       @RequestParam String isActive,
                                       @RequestParam String isNotLocked,
                                       @RequestParam(required = false) MultipartFile profileImage)
            throws UserNotFoundException, EmailExistException, IOException, UsernameExistException {

        User updatedUser = this.userService.updateUser(currentUsername, firstName, lastName, username, email, role,
                Boolean.parseBoolean(isNotLocked), Boolean.parseBoolean(isActive), profileImage);
        return new ResponseEntity<>(updatedUser, HttpStatus.OK);
    }

    @GetMapping("/find/{username}")
    public ResponseEntity<User> getUser(@PathVariable String username) {
        User user = this.userService.findUserByUsername(username);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @GetMapping("/list")
    public ResponseEntity<List<User>> getAllUsers() {
        return new ResponseEntity<>(this.userService.getUsers(), HttpStatus.OK);
    }

    @GetMapping("/reset-password/{email}")
    public ResponseEntity<HttpResponse> resetPassword(@PathVariable String email)
            throws EmailNotFoundException, MessagingException {
        this.userService.resetPassword(email);
        return this.response(HttpStatus.OK, EMAIL_SENT.concat(email));
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAnyAuthority('user:delete')")
    //@PreAuthorize, esta anotación es posible gracias a que tenemos configurada @EnableGlobalMethodSecurity(prePostEnabled = true) en el SecurityConfiguration
    public ResponseEntity<HttpResponse> deleteUser(@PathVariable Long id) {
        this.userService.deleteUser(id);
        return this.response(HttpStatus.NO_CONTENT, USER_DELETED_SUCCESSFULLY);
    }

    @PostMapping("/update-profile-image")
    public ResponseEntity<User> updateProfileImage(@RequestParam String username, @RequestParam MultipartFile profileImage)
            throws UserNotFoundException, EmailExistException, IOException, UsernameExistException {
        User user = this.userService.updateProfileImage(username, profileImage);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @GetMapping(path = "/image/{username}/{filename}", produces = MediaType.IMAGE_JPEG_VALUE)
    public byte[] getImageProfile(@PathVariable String username, @PathVariable String filename) throws IOException {
        return Files.readAllBytes(Paths.get(FileConstant.USER_FOLDER + username + FileConstant.FORWARD_SLASH + filename));
    }

    private HttpHeaders getJwtHeader(UserPrincipal user) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(SecurityConstant.JWT_TOKEN_HEADER, this.jwtTokenProvider.generateJwtToken(user));
        return headers;
    }

    /**
     * UsernamePasswordAuthenticationToken, lo usamos para intentar iniciar sesión.
     * En la clase AuthenticationFailureListener del evento se obtiene el getPrincipal();
     * ese método getPrincipal devuelve precisamente ese "username" pasado como argumento
     * que es del tipo String
     */
    private void authenticate(String username, String password) {
        this.authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
    }

    private ResponseEntity<HttpResponse> response(HttpStatus httpStatus, String message) {
        HttpResponse httpResponse = new HttpResponse(httpStatus.value(), httpStatus, httpStatus.getReasonPhrase(), message);
        return new ResponseEntity<>(httpResponse, httpStatus);
    }
}
