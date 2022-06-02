package com.magadiflo.app.utility;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.magadiflo.app.constant.SecurityConstant;
import com.magadiflo.app.domain.UserPrincipal;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JWTTokenProvider {

    @Value("${jwt.secret}")
    private String secret;

    public String generateJwtToken(UserPrincipal userPrincipal) {
        String[] claims = this.getClaimsFromUser(userPrincipal);
        return JWT.create()
                .withIssuer(SecurityConstant.MAGADIFLO_LLC) //emisor del token
                .withAudience(SecurityConstant.MAGADIFLO_ADMINISTRATION)
                .withIssuedAt(new Date()) //Fecha en que se emitió el token
                .withSubject(userPrincipal.getUsername()) //Será el usuario real, nombre de usuario o alguna identificación única que lo pueda identificar en la BD
                .withArrayClaim(SecurityConstant.AUTHORITIES, claims) //Permisos
                .withExpiresAt(new Date(System.currentTimeMillis() + SecurityConstant.EXPIRATION_TIME)) //Vigencia del token
                .sign(Algorithm.HMAC512(this.secret.getBytes())); //Firmando token con clave secreta
    }

    public List<GrantedAuthority> getAuthorities(String token) {
        String[] claims = this.getClaimsFromToken(token);
        return Arrays.stream(claims).map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }

    public Authentication getAuthentication(String username, List<GrantedAuthority> authorities, HttpServletRequest request) {
        //No necesitamos credenciales en este momento porque ya la verificamos con el Token por eso le pasamos null
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                new UsernamePasswordAuthenticationToken(username, null, authorities);
        //Técnicamente, lo que hace es configurar información del usuario y eso significa contexto de seguridad
        usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        return usernamePasswordAuthenticationToken;
    }

    public boolean isTokenValid(String username, String token) {
        JWTVerifier verifier = this.getJWTVerifier();
        return StringUtils.isNotEmpty(username) && !this.isTokenExpired(verifier, token);
    }

    public String getSubject(String token) {
        JWTVerifier verifier = this.getJWTVerifier();
        return verifier.verify(token).getSubject();
    }

    private boolean isTokenExpired(JWTVerifier verifier, String token) {
        Date expiration = verifier.verify(token).getExpiresAt();
        return expiration.before(new Date());
    }

    private String[] getClaimsFromToken(String token) {
        JWTVerifier verifier = this.getJWTVerifier();
        return verifier.verify(token).getClaim(SecurityConstant.AUTHORITIES).asArray(String.class);
    }

    private JWTVerifier getJWTVerifier() {
        JWTVerifier verifier;
        try {
            Algorithm algorithm = Algorithm.HMAC512(this.secret);
            verifier = JWT.require(algorithm)
                    .withIssuer(SecurityConstant.MAGADIFLO_LLC)
                    .build();
        } catch (JWTVerificationException e) {
            throw new JWTVerificationException(SecurityConstant.TOKEN_CANNOT_BE_VERIFIED);
        }
        return verifier;
    }

    private String[] getClaimsFromUser(UserPrincipal userPrincipal) {
        //toArray, Convierte la lista en un arreglo. Si la lista es del tamaño del arreglo especificado, se devuelve allí.
        //De lo contrario, en tiempo de ejecución, se asigna un nuevo arreglo con el tipo especificado y el tamaño de esta lista.
        return userPrincipal.getAuthorities().stream().map(GrantedAuthority::getAuthority).toArray(String[]::new);
    }

}
