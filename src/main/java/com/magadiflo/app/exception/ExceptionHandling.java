package com.magadiflo.app.exception;

import com.magadiflo.app.domain.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @RestControllerAdvice. Permitirá que esta clase sea un controlador
 * para manejar las excepciones. Es decir, apenas se produzca una excepción
 * esta clase va a realizar el tratamiento según el tipo de excepción producida
 */

@RestControllerAdvice
public class ExceptionHandling {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final String ACCOUNT_LOCKED = "Your account has been locked. Please contact administration";
    private static final String METHOD_IS_NOT_ALLOWED = "This request method is not allowed on this endpoint. Please send a '%s' request";
    private static final String INTERNAL_SERVER_ERROR_MSG = "An error occurred while processing the request";
    private static final String INCORRECT_CREDENTIALS = "Username / password incorrect. Please try again";
    private static final String ACCOUNT_DISABLED = "Your account has been disabled. If this is an error, please contact administration";
    private static final String ERROR_PROCESSING_FILE = "Error occurred while processing file";
    private static final String NOT_ENOUGH_PERMISSION = "You do not have enough permission";

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

    private ResponseEntity<HttpResponse> createHttpResponse(HttpStatus httpStatus, String message) {
        HttpResponse httpResponse = new HttpResponse(httpStatus.value(), httpStatus, httpStatus.getReasonPhrase().toUpperCase(), message.toUpperCase());
        return new ResponseEntity<>(httpResponse, httpStatus);
    }

}
