package com.magadiflo.app.utility;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.magadiflo.app.constant.SecurityConstant;
import com.magadiflo.app.domain.UserPrincipal;
import org.springframework.beans.factory.annotation.Value;

import java.util.Date;

public class JWTTokenProvider {

    @Value("jwt.secret")
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

    private String[] getClaimsFromUser(UserPrincipal userPrincipal) {
        return null;
    }

}
