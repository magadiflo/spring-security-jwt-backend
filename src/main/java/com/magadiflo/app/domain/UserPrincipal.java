package com.magadiflo.app.domain;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Esta clase será mapeado por nuestra clase
 * User.
 *
 * Con esta clase conseguimos al implementar la
 * interfaz UserDetails los elementos básicos
 * (username, password, authorities, etc..)
 * que Spring Security requiere para la seguridad.
 *
 * Se podría implementar la interfaz UserDetails
 * en la clase User, pero como dicha clase ya cuenta
 * con muchos atributos, se hizo de esta manera, así
 * se ve más ordenado y únicamente con los elementos
 * que Spring Security requiere.
 */
public class UserPrincipal implements UserDetails {

    private final User USER;

    public UserPrincipal(User user) {
        this.USER = user;
    }

    /**
     * Devolvemos cualquier colección que extienda o implemente de GrantedAuthority
     * En nuestro caso devolveremos una colección de SimpleGrantedAuthority,
     * ya que implementa de GrantedAuthority
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Arrays.stream(this.USER.getAuthorities()).map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return this.USER.getPassword();
    }

    @Override
    public String getUsername() {
        return this.USER.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; //La cuenta no ha caducado
    }

    @Override
    public boolean isAccountNonLocked() {
        return this.USER.isNotLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; //Siempre le decimos true porque no lo estamos usando para nuestra lógica
    }

    @Override
    public boolean isEnabled() {
        return this.USER.isActive();
    }
}
