package com.magadiflo.app.filter;

import com.magadiflo.app.constant.SecurityConstant;
import com.magadiflo.app.utility.JWTTokenProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    /**
     * ***** NOTA *****
     * Cuando se inyecta vía constructor la anotación @Autowired normalmente no es necesaria
     * (esto es así desde la versión 4.3 de spring) y la manera recomendada de inyectar por
     * la documentación oficial es de hecho la de constructor.
     */
    private final JWTTokenProvider jwtTokenProvider;

    public JwtAuthorizationFilter(JWTTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * Este método se disparará cada vez que llegue una nueva solicitud,
     * y eso solo va a suceder una vez.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        //Verificamos si el método es del tipo OPTIONS, ya que es enviada antes de cada solicitud
        //y se envía para recopilar información sobre el servidor. Solamente respondemos con un status OK
        if (request.getMethod().equalsIgnoreCase(SecurityConstant.OPTIONS_HTTP_METHOD)) {
            response.setStatus(HttpStatus.OK.value());
        } else {
            String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
            if (authorizationHeader == null || !authorizationHeader.startsWith(SecurityConstant.TOKEN_PREFIX)) {
                filterChain.doFilter(request, response);
                return;
            }
            String token = authorizationHeader.substring(SecurityConstant.TOKEN_PREFIX.length());
            String username = this.jwtTokenProvider.getSubject(token);
            /**
             * La verificación en el security context holder no es necesaria, ya que no estamos
             * usando la sesión. Esta parte se puede quitar en la condición
             */
            if (this.jwtTokenProvider.isTokenValid(username, token) &&
                    SecurityContextHolder.getContext().getAuthentication() == null) {
                List<GrantedAuthority> authorities = this.jwtTokenProvider.getAuthorities(token);
                Authentication authentication = this.jwtTokenProvider.getAuthentication(username, authorities, request);
                //Configuramos al usuario como un usuario autenticado en el context security holder
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }

}
/**
 * SecurityContext, se utiliza para almacenar los detalles del usuario autenticado actualmente,
 * también conocido como principal. Por lo tanto, si tiene que obtener el nombre de usuario o cualquier otro
 * detalle del usuario, primero debe obtener este SecurityContext.
 *
 * SecurityContextHolder, es una clase auxiliar que proporciona acceso al contexto de seguridad.
 * Para obtener el nombre de usuario actual, primero necesita un SecurityContext, que se obtiene de SecurityContextHolder.
 * Este SecurityContext mantiene los detalles del usuario en un objeto Authentication,
 * que se puede obtener llamando al método getAuthentication().
 *
 * Una vez que haya obtenido el objeto Authentication, puede convertirlo en UserDetails o usarlo tal como está.
 * El objeto UserDetails es el que Spring Security utiliza para conservar la información relacionada con el usuario.
 *
 * Read more:
 * https://javarevisited.blogspot.com/2018/02/what-is-securitycontext-and-SecurityContextHolder-Spring-security.html#ixzz7cihaaV1W
 */