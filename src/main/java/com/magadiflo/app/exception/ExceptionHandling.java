package com.magadiflo.app.exception;

import com.auth0.jwt.exceptions.TokenExpiredException;
import com.magadiflo.app.domain.HttpResponse;
import com.magadiflo.app.exception.domain.EmailExistException;
import com.magadiflo.app.exception.domain.EmailNotFoundException;
import com.magadiflo.app.exception.domain.UserNotFoundException;
import com.magadiflo.app.exception.domain.UsernameExistException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.persistence.NoResultException;
import java.io.IOException;
import java.util.Objects;

/**
 * @RestControllerAdvice. Permitirá que esta clase sea un controlador
 * para manejar las excepciones. Es decir, apenas se produzca una excepción
 * esta clase va a realizar el tratamiento según el tipo de excepción producida
 * <p>
 * >>> Sobreescribir la página de error por defecto de Spring <<<
 *
 * 1) ErrorController, importante implementarlo para poder cambiar la página
 * mostrada por defecto al acceder a una ruta no definida. Anteriormente,
 * definía un método getErrorPath(), pero en versiones actuales de Spring Boot
 * ya está deprecated since 2.3.0.
 * 2) Se recomienda configurar la propiedad `server.error.path`,
 * en el archivo application.yml mapeándolo a una ruta, ejm. /error.
 * En nuestro caso sería:
 * server:
 *   error:
 *     path: /error
 * 3) Definir un método handler (@RequestMapping("/error")) que esté mapeado
 * al path definido en el application.yml (/error), dicho método devolverá una
 * respuesta personalizada, en nuestro caso en formato JSON
 * 4) Importante definir un @RequestMapping mapeado a un "/" ya que Spring al lanzar
 * el error mapeado al path definido en el application.yml tomará el "/" como base:
 *      http://localhost:8081/error
 * En nuestro caso, solo tenemos un Recurso UserResource donde teníamos
 * definido un @RequestMapping(value = "/users"), lo que causaba que al ejecutar e
 * ingresar por una url no definida el path /error se concatene de esta manera:
 *      http://localhost:8081/users/error
 * Esto provocaba que no se muestre nuestro método handler personalizado.
 * Para solucionarlo se agregó el "/" al request mapping quedando:
 *      @RequestMapping(path = {"/", "/users"})
 */

@RestControllerAdvice
public class ExceptionHandling implements ErrorController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final String ACCOUNT_LOCKED = "Your account has been locked. Please contact administration";
    private static final String METHOD_IS_NOT_ALLOWED = "This request method is not allowed on this endpoint. Please send a '%s' request";
    private static final String INTERNAL_SERVER_ERROR_MSG = "An error occurred while processing the request";
    private static final String INCORRECT_CREDENTIALS = "Username / password incorrect. Please try again";
    private static final String ACCOUNT_DISABLED = "Your account has been disabled. If this is an error, please contact administration";
    private static final String ERROR_PROCESSING_FILE = "Error occurred while processing file";
    private static final String NOT_ENOUGH_PERMISSION = "You do not have enough permission";
    public static final String ERROR_PATH = "/error";

    @RequestMapping(ERROR_PATH)
    public ResponseEntity<HttpResponse> notFound404() {
        return this.createHttpResponse(HttpStatus.NOT_FOUND, "There is no mapping for this URL");
    }

    /**
     * NOTA: Se puede pasar por argumento la excepción misma, esto permitirá
     * tener toda la información de la excepción generada.
     * Ejemplo:
     * @ExceptionHandler(DisabledException.class)
     * public ResponseEntity<HttpResponse> accountDisabledException(DisabledException e) {
     * .....
     */

    /**
     * DisabledException, esta excepción viene de la autenticación,
     * que es cuando la cuenta está deshabilitada.
     * Si el usuario está intentando iniciar sesión, pero
     * su cuenta está deshabilitada, entonces esta excepción
     * se disparará
     */
    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<HttpResponse> accountDisabledException() {
        return this.createHttpResponse(HttpStatus.BAD_REQUEST, ACCOUNT_DISABLED);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<HttpResponse> badCredentialsException() {
        return this.createHttpResponse(HttpStatus.BAD_REQUEST, INCORRECT_CREDENTIALS);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<HttpResponse> accessDeniedException() {
        return this.createHttpResponse(HttpStatus.FORBIDDEN, NOT_ENOUGH_PERMISSION);
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<HttpResponse> lockedException() {
        return this.createHttpResponse(HttpStatus.UNAUTHORIZED, ACCOUNT_LOCKED);
    }

    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<HttpResponse> tokenExpiredException(TokenExpiredException e) {
        return this.createHttpResponse(HttpStatus.UNAUTHORIZED, e.getMessage());
    }

    @ExceptionHandler(EmailExistException.class)
    public ResponseEntity<HttpResponse> emailExistException(EmailExistException e) {
        return this.createHttpResponse(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(UsernameExistException.class)
    public ResponseEntity<HttpResponse> UsernameExistException(UsernameExistException e) {
        return this.createHttpResponse(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(EmailNotFoundException.class)
    public ResponseEntity<HttpResponse> emailNotFoundException(EmailNotFoundException e) {
        return this.createHttpResponse(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<HttpResponse> userNotFoundException(UserNotFoundException e) {
        return this.createHttpResponse(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<HttpResponse> methodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        HttpMethod supportedMethod = Objects.requireNonNull(e.getSupportedHttpMethods()).iterator().next();
        return this.createHttpResponse(HttpStatus.METHOD_NOT_ALLOWED, String.format(METHOD_IS_NOT_ALLOWED, supportedMethod));
    }

    @ExceptionHandler(NoResultException.class)
    public ResponseEntity<HttpResponse> noResultException(NoResultException e) {
        this.logger.error(e.getMessage());
        return this.createHttpResponse(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<HttpResponse> iOException(IOException e) {
        this.logger.error(e.getMessage());
        return this.createHttpResponse(HttpStatus.INTERNAL_SERVER_ERROR, ERROR_PROCESSING_FILE);
    }

    /**
     * Si no es ninguna de las excepciones definas en todos estos métodos, entrará en
     * este método que es una excepción general
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<HttpResponse> internalServerErrorException(Exception e) {
        this.logger.error(e.getMessage());
        return this.createHttpResponse(HttpStatus.INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR_MSG);
    }

    private ResponseEntity<HttpResponse> createHttpResponse(HttpStatus httpStatus, String message) {
        HttpResponse httpResponse = new HttpResponse(httpStatus.value(), httpStatus, httpStatus.getReasonPhrase().toUpperCase(), message);
        return new ResponseEntity<>(httpResponse, httpStatus);
    }

}
