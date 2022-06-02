package com.magadiflo.app.utility;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.magadiflo.app.constant.SecurityConstant;
import com.magadiflo.app.domain.UserPrincipal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

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
