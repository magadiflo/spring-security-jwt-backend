package com.magadiflo.app.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.magadiflo.app.constant.SecurityConstant;
import com.magadiflo.app.domain.HttpResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Cada vez que el usuario no esté autenticado e intente
 * acceder a la aplicación, se dispara el Http403ForbiddenEntryPoint y tiene
 * una implementación predeterminada con un mensaje muy genérico. Y no queremos
 * que eso suceda, queremos tomar el control de lo que se devuelve al usuario.
 * Por lo tanto, sobreescribimos el método commence() de la clase Http403ForbiddenEntryPoint,
 * y personalizamos el mensaje de respuesta con nuestra clase personalizada (HttpResponse)
 */

@Component
public class JwtAuthenticationEntryPoint extends Http403ForbiddenEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception)
            throws IOException {
        HttpResponse httpResponse = new HttpResponse(HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN,
                HttpStatus.FORBIDDEN.getReasonPhrase().toUpperCase(), SecurityConstant.FORBIDDEN_MESSAGE);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpStatus.FORBIDDEN.value());

        OutputStream outputStream = response.getOutputStream();
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(outputStream, httpResponse);
        outputStream.flush(); //Enviamos la respuesta al usuario
    }
}
